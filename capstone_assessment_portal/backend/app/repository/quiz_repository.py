from bson import ObjectId
from bson.errors import InvalidId
from datetime import datetime, timezone

from app.config.database import database
from app.exceptions.quiz_exceptions import QuizInvalidIdException


class QuizRepository:
    """
    Handles all direct MongoDB operations for the quizzes collection.
    """

    def __init__(self):
        # quizzes collection in MongoDB
        # MongoDB creates this collection automatically on first insert
        self.collection = database["quizzes"]

    def _to_object_id(self, id: str) -> ObjectId:
        """
        Safely convert a string ID to MongoDB ObjectId.

        Valid ObjectId format: 24 hexadecimal characters
        Example: "64abc123def456789012abcd"
        """
        try:
            return ObjectId(id)
        except (InvalidId, Exception):
            raise QuizInvalidIdException()

    async def find_by_title_and_category(
        self,
        title: str,
        category_id: str
    ) -> dict | None:
        """
        Check if a quiz with the same title already exists
        in the same category.

        Case-insensitive search using regex so
        "python basics" matches "Python Basics".
        """
        return await self.collection.find_one({
            "title": {"$regex": f"^{title}$", "$options": "i"},
            "category_id": category_id,     # same category check
            "is_active": True               # only check active quizzes
        })

    async def find_by_id(self, id: str) -> dict | None:
        """
        Fetch a single quiz by its MongoDB ObjectId string.

        Returns the full quiz document if found.
        Returns None if no quiz with that ID exists.
        """
        object_id = self._to_object_id(id)
        return await self.collection.find_one({"_id": object_id})

    async def find_all(self) -> list[dict]:
        """
        Fetch all active quizzes from the database.

        We filter by is_active=True so soft-deleted quizzes
        don't appear in the listing.

        Sorted by created_at descending (-1) means
        newest quizzes appear first in the list.

        to_list(None) — None means fetch ALL results with no limit.
        """
        cursor = self.collection.find(
            {"is_active": True}
        ).sort("created_at", -1)

        return await cursor.to_list(None)

    async def find_by_category(self, category_id: str) -> list[dict]:
        """
        Fetch all active quizzes that belong to a specific category.

        Used when:
        - Student selects a category and wants to see its quizzes
        - Admin views quizzes under a specific category

        Example: fetch all quizzes under "Python Programming" category
        """
        cursor = self.collection.find({
            "category_id": category_id,
            "is_active": True
        }).sort("created_at", -1)

        return await cursor.to_list(None)

    async def create(self, quiz_data: dict) -> dict:
        """
        Insert a new quiz document into the quizzes collection.
        """
        result = await self.collection.insert_one(quiz_data)

        # fetch the complete saved document to return to the service
        return await self.collection.find_one({"_id": result.inserted_id})

    async def update(self, id: str, update_data: dict) -> dict | None:
        """
        Update specific fields of an existing quiz.

        $set operator updates ONLY the fields provided in update_data.
        Other fields remain untouched.

        return_document=True means we get back the NEW version of the document.

        Returns None if no quiz with that ID exists.
        """
        object_id = self._to_object_id(id)

        return await self.collection.find_one_and_update(
            {"_id": object_id},
            {"$set": update_data},
            return_document=True
        )

    async def delete(self, id: str) -> dict | None:
        """
        Soft delete a quiz by setting is_active to False.

        Returns the updated document or None if not found.
        """
        object_id = self._to_object_id(id)

        return await self.collection.find_one_and_update(
            {"_id": object_id},
            {
                "$set": {
                    "is_active": False,
                    "updated_at": datetime.now(timezone.utc)
                }
            },
            return_document=True
        )