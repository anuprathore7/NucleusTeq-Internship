from bson import ObjectId
from bson.errors import InvalidId
from datetime import datetime, timezone

from app.config.database import database
from app.exceptions.question_exceptions import QuestionInvalidIdException


class QuestionRepository:
    """
    Handles ALL direct MongoDB operations for the questions collection.
    """

    def __init__(self):
        # questions collection — MongoDB creates it automatically on first insert
        self.collection = database["questions"]

    def _to_object_id(self, id: str) -> ObjectId:
        """
        Safely converts a string ID to MongoDB ObjectId type.
        """
        try:
            return ObjectId(id)
        except (InvalidId, Exception):
            raise QuestionInvalidIdException()

    async def find_by_id(self, id: str) -> dict | None:
        """
        Fetch a single question by its MongoDB _id.
        """
        object_id = self._to_object_id(id)
        return await self.collection.find_one({"_id": object_id})

    async def find_by_text_and_quiz(
        self,
        question_text: str,
        quiz_id: str
    ) -> dict | None:
        """
        Check if a question with the same text already exists in the same quiz.
        """
        return await self.collection.find_one({
            # $regex pattern:
            # ^ means start of string
            # $ means end of string
            # Together ^text$ means exact match
            # $options: "i" means case insensitive
            "question_text": {
                "$regex": f"^{question_text}$",
                "$options": "i"
            },
            "quiz_id": quiz_id,         # same quiz check
            "is_active": True           # only check active questions
        })

    async def find_by_quiz_id(self, quiz_id: str) -> list[dict]:
        """
        Fetch all active questions belonging to a specific quiz.    
        """
        cursor = self.collection.find({
            "quiz_id": quiz_id,
            "is_active": True
        }).sort("created_at", 1)    # 1 = ascending (oldest first)

        # to_list(None) = fetch ALL results with no limit
        return await cursor.to_list(None)

    async def find_by_quiz_and_difficulty(
        self,
        quiz_id: str,
        difficulty: str
    ) -> list[dict]:
        """
        Fetch questions filtered by quiz AND difficulty level.
        """
        cursor = self.collection.find({
            "quiz_id": quiz_id,
            "difficulty": difficulty,
            "is_active": True
        }).sort("created_at", 1)

        return await cursor.to_list(None)

    async def find_all(self) -> list[dict]:
        """
        Fetch ALL active questions across all quizzes.
        """
        cursor = self.collection.find(
            {"is_active": True}
        ).sort("created_at", -1)    # -1 = descending (newest first)

        return await cursor.to_list(None)

    async def create(self, question_data: dict) -> dict:
        """
        Insert a new question document into the questions collection.
        """
        result = await self.collection.insert_one(question_data)

        # fetch and return the complete saved document
        return await self.collection.find_one({"_id": result.inserted_id})

    async def update(self, id: str, update_data: dict) -> dict | None:
        """
        Update specific fields of an existing question.

        $set updates ONLY the fields in update_data.
        All other fields remain exactly as they were.

        return_document=True means we get the NEW version
        after update, not the old version before update.

        Returns None if no question with that ID exists.
        """
        object_id = self._to_object_id(id)

        return await self.collection.find_one_and_update(
            {"_id": object_id},
            {"$set": update_data},
            return_document=True
        )

    async def delete(self, id: str) -> dict | None:
        """
        Soft delete a question by setting is_active = False.
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

    async def count_by_quiz(self, quiz_id: str) -> int:
        """
        Count total active questions in a quiz.
        """
        return await self.collection.count_documents({
            "quiz_id": quiz_id,
            "is_active": True
        })