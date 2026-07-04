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
        result = await self.collection.find_one(
            {"name": {"$regex": f"^{name}$", "$options": "i"}}
            # $regex = pattern match
            # ^ = start, $ = end means exact match
            # $options: "i" = case insensitive
        )
        return result

    async def find_by_id(self, id: str) -> dict | None:
        """
        Fetch a single category by its MongoDB ObjectId string.
        Returns the document or None if not found.
        """
        object_id = self._to_object_id(id)
        result = await self.collection.find_one({
            "_id": object_id,
            "is_active": True          # ignore soft-deleted docs
        })
        return result

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
        result = await cursor.to_list(None)
        return result

    async def create(self, category_data: dict) -> dict:
        """
        Insert a new category document into the collection.
        Returns the complete saved document including the generated _id.
        """
        inserted = await self.collection.insert_one(category_data)

        # fetch and return the saved document so caller gets complete data
        result = await self.collection.find_one({"_id": inserted.inserted_id})
        return result

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

    async def delete(self, id: str) -> None:
        """
        Hard delete a category — permanently removes it from MongoDB.
        No recovery possible after this operation.

        delete_one() removes the document permanently.
        Returns None — service already has the document
        fetched before calling this method.
        """
        object_id = self._to_object_id(id)
        await self.collection.delete_one({"_id": object_id})

    async def count_quizzes_by_category(self, category_id: str) -> int:
        """
        Count how many active quizzes are linked to this category.

        Used before deleting a category to check if it is safe to delete.
        If count > 0, the delete is blocked — admin must remove
        all quizzes under this category first.

        count_documents() is efficient — it only returns a number,
        not the actual documents. Much faster than fetching all quizzes
        just to check if any exist.
        """
        result = await database["quizzes"].count_documents({
            "category_id": category_id,
            "is_active": True
        })
        return result