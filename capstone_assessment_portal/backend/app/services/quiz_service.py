from datetime import datetime, timezone

from app.repository.quiz_repository import QuizRepository
from app.repository.category_repository import CategoryRepository
from app.schemas.request.quiz_schema import CreateQuizSchema, UpdateQuizSchema
from app.schemas.response.quiz_response_schema import (
    QuizResponseSchema,
    QuizListResponseSchema
)
from app.exceptions.quiz_exceptions import (
    QuizNotFoundException,
    QuizAlreadyExistsException,
    QuizCategoryNotFoundException
)
from app.utils.quiz_mapper import quiz_to_response, quizzes_to_response


class QuizService:
    """
    Contains all business logic for quiz operations.

    It uses two repositories:
    - QuizRepository → for quiz DB operations
    - CategoryRepository → to verify category exists when creating/updating quiz
    """

    def __init__(self):
        self.quiz_repo = QuizRepository()
        # We need category repo to validate that category_id actually exists
        # before linking a quiz to it
        self.category_repo = CategoryRepository()

    async def create_quiz(
        self,
        data: CreateQuizSchema
    ) -> QuizResponseSchema:
        """
        Create a new quiz under a category.
        """

        # verify category exists
        # We use category_repo here because quiz service should not
        # contain category DB code — it just asks category repo
        category = await self.category_repo.find_by_id(data.category_id)
        if not category:
            raise QuizCategoryNotFoundException()

        # check for duplicate title in same category
        existing = await self.quiz_repo.find_by_title_and_category(
            data.title,
            data.category_id
        )
        if existing:
            raise QuizAlreadyExistsException()

        # build the MongoDB document
        new_quiz = {
            "title": data.title,
            "description": data.description,
            "category_id": data.category_id,    # stored as string reference
            "time_limit": data.time_limit,
            "pass_percentage": data.pass_percentage,
            "is_active": True,
            "created_at": datetime.now(timezone.utc),
            "updated_at": datetime.now(timezone.utc)
        }

        # save to database
        saved_quiz = await self.quiz_repo.create(new_quiz)

        # convert to clean response and return
        return quiz_to_response(saved_quiz)

    async def get_all_quizzes(self) -> QuizListResponseSchema:
        """
        Fetch all active quizzes.

        Available to any logged in user — both admin and student.
        Students need this to see what quizzes are available to attempt.
        """
        quizzes = await self.quiz_repo.find_all()
        return quizzes_to_response(quizzes)

    async def get_quiz_by_id(
        self,
        quiz_id: str
    ) -> QuizResponseSchema:
        """
        Fetch a single quiz by its ID.
        """
        quiz = await self.quiz_repo.find_by_id(quiz_id)

        if not quiz:
            raise QuizNotFoundException()

        return quiz_to_response(quiz)

    async def get_quizzes_by_category(
        self,
        category_id: str
    ) -> QuizListResponseSchema:
        """
        Fetch all quizzes belonging to a specific category.

        Why verify category first?
        If someone sends a wrong category_id, we return a clear
        404 instead of returning an empty list that looks like
        "category exists but has no quizzes".
        """

        # verify category actually exists
        category = await self.category_repo.find_by_id(category_id)
        if not category:
            raise QuizCategoryNotFoundException()

        # fetch quizzes for this category
        quizzes = await self.quiz_repo.find_by_category(category_id)

        # return structured response
        return quizzes_to_response(quizzes)

    async def update_quiz(
        self,
        quiz_id: str,
        data: UpdateQuizSchema
    ) -> QuizResponseSchema:
        """
        Update an existing quiz.
        """

        # confirm quiz exists
        existing_quiz = await self.quiz_repo.find_by_id(quiz_id)
        if not existing_quiz:
            raise QuizNotFoundException()

        # if category is being changed, validate new category exists
        if data.category_id:
            category = await self.category_repo.find_by_id(data.category_id)
            if not category:
                raise QuizCategoryNotFoundException()

        # if title is changing, check for duplicate in target category
        # target category = new category if provided, else existing category
        if data.title:
            target_category_id = data.category_id or existing_quiz["category_id"]
            duplicate = await self.quiz_repo.find_by_title_and_category(
                data.title,
                target_category_id
            )
            # make sure the duplicate found is not the quiz we are updating
            if duplicate and str(duplicate["_id"]) != quiz_id:
                raise QuizAlreadyExistsException()

        # build update dict, skip fields that were not provided
        # exclude_none=True means None fields are not included
        # so we don't overwrite existing DB values with None
        update_data = data.model_dump(exclude_none=True)
        update_data["updated_at"] = datetime.now(timezone.utc)

        # save and return
        updated_quiz = await self.quiz_repo.update(quiz_id, update_data)
        return quiz_to_response(updated_quiz)

    async def delete_quiz(self, quiz_id: str) -> dict:
        """
        Soft delete a quiz.

        Soft delete means is_active = False.
        The quiz stays in MongoDB but disappears from all listings.
        Student attempt history linked to this quiz remains intact.
        """

        # confirm it exists before trying to delete
        existing_quiz = await self.quiz_repo.find_by_id(quiz_id)
        if not existing_quiz:
            raise QuizNotFoundException()

        # soft delete
        await self.quiz_repo.delete(quiz_id)

        # return confirmation
        return {
            "message": f"Quiz '{existing_quiz['title']}' deleted successfully"
        }