from app.config.database import database
from app.constants.attempt_constants import ATTEMPT_STATUS_SUBMITTED


class ResultRepository:
    """
    Handles all direct MongoDB operations needed for result retrieval.
    """

    def __init__(self):
        # reads from attempts collection — not a separate results collection
        self.collection = database["attempts"]

    async def find_submitted_by_attempt_id(
        self,
        attempt_id: str,
        student_id: str
    ) -> dict | None:
        """
        Fetch a single submitted attempt by its ID.
        """
        from bson import ObjectId
        from bson.errors import InvalidId

        try:
            object_id = ObjectId(attempt_id)
        except (InvalidId, Exception):
            return None

        result = await self.collection.find_one({
            "_id": object_id,
            "student_id": student_id,
            "status": ATTEMPT_STATUS_SUBMITTED
        })
        return result

    async def find_all_submitted_by_student(
        self,
        student_id: str
    ) -> list[dict]:
        """
        Fetch all submitted attempts for a specific student.
        Used for student result history page.

        Only returns submitted attempts — in_progress ones
        don't have a result yet so they are excluded.
        Sorted by submitted_at descending — most recent first.
        """
        cursor = self.collection.find({
            "student_id": student_id,
            "status": ATTEMPT_STATUS_SUBMITTED
        }).sort("submitted_at", -1)

        result = await cursor.to_list(None)
        return result

    async def find_all_submitted(self) -> list[dict]:
        """
        Fetch ALL submitted attempts across ALL students.
        Used by admin for the result dashboard.
        Sorted by submitted_at descending — most recent first.
        """
        cursor = self.collection.find(
            {"status": ATTEMPT_STATUS_SUBMITTED}
        ).sort("submitted_at", -1)

        result = await cursor.to_list(None)
        return result

    async def find_all_submitted_by_quiz(
        self,
        quiz_id: str
    ) -> list[dict]:
        """
        Fetch all submitted attempts for a specific quiz.
        Used by admin to see how all students performed
        on a particular quiz.
        Sorted by submitted_at descending.
        """
        cursor = self.collection.find({
            "quiz_id": quiz_id,
            "status": ATTEMPT_STATUS_SUBMITTED
        }).sort("submitted_at", -1)

        result = await cursor.to_list(None)
        return result