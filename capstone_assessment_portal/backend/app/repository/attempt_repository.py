

from bson import ObjectId
from bson.errors import InvalidId
from datetime import datetime, timezone

from app.config.database import database
from app.exceptions.attempt_exceptions import AttemptInvalidIdException


class AttemptRepository:
    """
    Handles all direct MongoDB operations for the attempts collection.
    No business logic here — only database reads and writes.
    """

    def __init__(self):
        # attempts collection — MongoDB creates it on first insert
        self.collection = database["attempts"]

    def _to_object_id(self, id: str) -> ObjectId:
        """
        Safely converts a string ID to MongoDB ObjectId.
        Raises 400 if the format is invalid.
        """
        try:
            return ObjectId(id)
        except (InvalidId, Exception):
            raise AttemptInvalidIdException()

    async def create(self, attempt_data: dict) -> dict:
        """
        Insert a new attempt document into the collection.
        Returns the complete saved document including generated _id.
        """
        inserted = await self.collection.insert_one(attempt_data)
        result = await self.collection.find_one({"_id": inserted.inserted_id})
        return result

    async def find_by_id(self, attempt_id: str) -> dict | None:
        """
        Fetch a single attempt by its MongoDB ObjectId.
        Returns the full attempt document or None if not found.
        """
        object_id = self._to_object_id(attempt_id)
        result = await self.collection.find_one({"_id": object_id})
        return result

    async def find_by_student(self, student_id: str) -> list[dict]:
        """
        Fetch all attempts belonging to a specific student.
        Sorted by started_at descending — most recent first.
        """
        cursor = self.collection.find(
            {"student_id": student_id}
        ).sort("started_at", -1)

        result = await cursor.to_list(None)
        return result

    async def count_attempts_by_student_and_quiz(
        self,
        student_id: str,
        quiz_id: str
    ) -> int:
        """
        Count how many attempts a student has made for a specific quiz.
        Counts ALL attempts — both in_progress and submitted.
        Used to enforce max 2 attempts per quiz.
        """
        result = await self.collection.count_documents({
            "student_id": student_id,
            "quiz_id": quiz_id
        })
        return result

    async def save_answer(
        self,
        attempt_id: str,
        question_id: str,
        selected_answer: str
    ) -> dict | None:
        """
        Save or update a single answer in the attempt document.
        """
        object_id = self._to_object_id(attempt_id)

        # Try to update existing answer for this question
        update_result = await self.collection.update_one(
            {
                "_id": object_id,
                "answers.question_id": question_id   # find existing answer
            },
            {
                "$set": {
                    "answers.$.selected_answer": selected_answer
                    # $ = the matched element in answers array
                }
            }
        )

        # if no existing answer found, add new one
        # update_result.modified_count = 0 means no answer existed for this question
        if update_result.modified_count == 0:
            await self.collection.update_one(
                {"_id": object_id},
                {
                    "$push": {
                        "answers": {
                            "question_id": question_id,
                            "selected_answer": selected_answer
                        }
                    }
                }
            )

        # return updated attempt document
        result = await self.collection.find_one({"_id": object_id})
        return result

    async def submit_attempt(
        self,
        attempt_id: str,
        update_data: dict
    ) -> dict | None:
        """
        Update the attempt document on submission.

        $set updates only the provided fields.
        return_document=True returns the updated document.
        """
        object_id = self._to_object_id(attempt_id)

        result = await self.collection.find_one_and_update(
            {"_id": object_id},
            {"$set": update_data},
            return_document=True
        )
        return result