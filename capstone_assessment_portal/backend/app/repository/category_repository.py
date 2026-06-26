from bson import ObjectId
from bson.errors import InvalidId
from datetime import datetime, timezone

from app.config.database import database
from app.exceptions.category_exceptions import CategoryInvalidIdException


class CategoryRepository:
    """
    Handles all direct MongoDB operations for the categories collection.
    No business logic here — only database reads and writes.
    """

    def __init__(self):
        # categories collection — MongoDB creates it on first insert
        self.collection = database["categories"]

    def _to_object_id(self, id: str) -> ObjectId:
        """
        Safely convert a string ID to MongoDB ObjectId.
        If the string is not a valid ObjectId format, raise a clean 400 error.
        """
        try:
            return ObjectId(id)
        except (InvalidId, Exception):
            raise CategoryInvalidIdException()

    async def find_by_name(self, name: str) -> dict | None:
        """
        Check if a category with this name already exists.
        Used to prevent duplicate categories.
        Case-insensitive search using regex — 'python' matches 'Python'.
        """
        return await self.collection.find_one(
            {"name": {"$regex": f"^{name}$", "$options": "i"}}
            # $regex = pattern match
            # ^ = start, $ = end means exact match
            # $options: "i" = case insensitive
        )

    async def find_by_id(self, id: str) -> dict | None:
        """
        Fetch a single category by its MongoDB ObjectId string.
        Returns the document or None if not found.
        """
        object_id = self._to_object_id(id)
        return await self.collection.find_one({"_id": object_id})

    async def find_all(self) -> list[dict]:
        """
        Fetch all active categories from the database.
        We filter by is_active=True so soft-deleted categories
        don't appear in the list.
        """
        cursor = self.collection.find(
            {"is_active": True}             # only active categories
        ).sort("created_at", -1)            # -1 = descending (newest first)

        # cursor is lazy — it doesn't fetch data until we iterate
        # to_list(None) fetches all results at once
        # None means no limit on number of results
        return await cursor.to_list(None)

    async def create(self, category_data: dict) -> dict:
        """
        Insert a new category document into the collection.
        Returns the complete saved document including the generated _id.
        """
        result = await self.collection.insert_one(category_data)

        # fetch and return the saved document so caller gets complete data
        return await self.collection.find_one({"_id": result.inserted_id})

    async def update(self, id: str, update_data: dict) -> dict | None:
        """
        Update an existing category by ID.
        Only updates fields that are provided — does not overwrite the whole document.
        Returns the updated document or None if not found.
        """
        object_id = self._to_object_id(id)

        # $set only updates the specified fields
        # without $set, MongoDB would REPLACE the entire document
        result = await self.collection.find_one_and_update(
            {"_id": object_id},             # find category with this ID
            {"$set": update_data},          # only update provided fields
            return_document=True            # return the updated document, not the old one
        )
        return result

    async def delete(self, id: str) -> dict | None:
        """
        Soft delete a category by setting is_active to False.
        We never hard-delete data — soft delete means we just
        hide it from listings but keep it in the DB for audit purposes.
        If quizzes reference this category, the data stays intact.
        Returns the updated document or None if not found.
        """
        object_id = self._to_object_id(id)

        result = await self.collection.find_one_and_update(
            {"_id": object_id},
            {
                "$set": {
                    "is_active": False,                     # hide from listings
                    "updated_at": datetime.now(timezone.utc) # track when deleted
                }
            },
            return_document=True
        )
        return result