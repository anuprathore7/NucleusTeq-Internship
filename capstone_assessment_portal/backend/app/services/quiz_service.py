from datetime import datetime, timezone

from app.config.logger import get_logger
from app.repository.quiz_repository import QuizRepository
from app.repository.category_repository import CategoryRepository
from app.repository.question_repository import QuestionRepository
from app.repository.attempt_repository import AttemptRepository
from app.schemas.request.quiz_schema import CreateQuizSchema, UpdateQuizSchema
from app.schemas.response.quiz_response_schema import (
    QuizResponseSchema,
    QuizListResponseSchema
)
from app.exceptions.quiz_exceptions import (
    QuizNotFoundException,
    QuizAlreadyExistsException,
    QuizCategoryNotFoundException,
    QuizHasActiveAttemptsException
)
from app.utils.quiz_mapper import quiz_to_response, quizzes_to_response
from app.schemas.response.message_response_schema import MessageResponseSchema

logger = get_logger(__name__)


class QuizService:
    """
    Contains all business logic for quiz operations.
    Uses QuizRepository, CategoryRepository, QuestionRepository and AttemptRepository.
    """

    def __init__(self):
        self.quiz_repo = QuizRepository()
        self.category_repo = CategoryRepository()
        # needed for cascade delete — remove questions when a quiz is deleted
        self.question_repo = QuestionRepository()
        # needed to check if any student is mid-attempt before deleting
        self.attempt_repo = AttemptRepository()

    async def create_quiz(
        self,
        data: CreateQuizSchema
    ) -> QuizResponseSchema:
        """
        Create a new quiz after verifying category exists
        and no duplicate title exists in the same category.
        """
        logger.info(f"Create quiz attempt: '{data.title}' under category: {data.category_id}")

        category = await self.category_repo.find_by_id(data.category_id)
        if not category:
            logger.warning(f"Create quiz failed — category not found: {data.category_id}")
            raise QuizCategoryNotFoundException()

        existing = await self.quiz_repo.find_by_title_and_category(
            data.title,
            data.category_id
        )
        if existing:
            logger.warning(f"Create quiz failed — duplicate title: '{data.title}'")
            raise QuizAlreadyExistsException()

        new_quiz = {
            "title": data.title,
            "description": data.description,
            "category_id": data.category_id,
            "time_limit": data.time_limit,
            "pass_percentage": data.pass_percentage,
            "is_active": True,
            "created_at": datetime.now(timezone.utc),
            "updated_at": datetime.now(timezone.utc)
        }

        saved_quiz = await self.quiz_repo.create(new_quiz)
        logger.info(f"Quiz created successfully: '{data.title}'")

        result = quiz_to_response(saved_quiz)
        return result

    async def get_all_quizzes(self) -> QuizListResponseSchema:
        """
        Fetch all quizzes for any logged in user.
        """
        logger.info("Fetching all quizzes")

        quizzes = await self.quiz_repo.find_all()
        logger.info(f"Returned {len(quizzes)} quizzes")

        result = quizzes_to_response(quizzes)
        return result

    async def get_quiz_by_id(
        self,
        quiz_id: str
    ) -> QuizResponseSchema:
        """
        Fetch a single quiz by its ID.
        """
        logger.info(f"Fetching quiz by id: {quiz_id}")

        quiz = await self.quiz_repo.find_by_id(quiz_id)
        if not quiz:
            logger.warning(f"Quiz not found: {quiz_id}")
            raise QuizNotFoundException()

        logger.info(f"Quiz found: '{quiz['title']}'")

        result = quiz_to_response(quiz)
        return result

    async def get_quizzes_by_category(
        self,
        category_id: str
    ) -> QuizListResponseSchema:
        """
        Fetch all quizzes under a specific category.
        Verifies category exists before querying quizzes.
        """
        logger.info(f"Fetching quizzes for category: {category_id}")

        category = await self.category_repo.find_by_id(category_id)
        if not category:
            logger.warning(f"Get quizzes by category failed — category not found: {category_id}")
            raise QuizCategoryNotFoundException()

        quizzes = await self.quiz_repo.find_by_category(category_id)
        logger.info(f"Returned {len(quizzes)} quizzes for category: {category_id}")

        result = quizzes_to_response(quizzes)
        return result

    async def update_quiz(
        self,
        quiz_id: str,
        data: UpdateQuizSchema
    ) -> QuizResponseSchema:
        """
        Update an existing quiz's fields.
        Validates category and duplicate title if those fields are changing.
        """
        logger.info(f"Update quiz attempt for id: {quiz_id}")

        existing_quiz = await self.quiz_repo.find_by_id(quiz_id)
        if not existing_quiz:
            logger.warning(f"Update failed — quiz not found: {quiz_id}")
            raise QuizNotFoundException()

        if data.category_id:
            category = await self.category_repo.find_by_id(data.category_id)
            if not category:
                logger.warning(f"Update failed — new category not found: {data.category_id}")
                raise QuizCategoryNotFoundException()

        if data.title:
            target_category_id = data.category_id or existing_quiz["category_id"]
            duplicate = await self.quiz_repo.find_by_title_and_category(
                data.title,
                target_category_id
            )
            if duplicate and str(duplicate["_id"]) != quiz_id:
                logger.warning(f"Update failed — duplicate title: '{data.title}'")
                raise QuizAlreadyExistsException()

        update_data = data.model_dump(exclude_none=True)
        update_data["updated_at"] = datetime.now(timezone.utc)

        updated_quiz = await self.quiz_repo.update(quiz_id, update_data)
        logger.info(f"Quiz updated successfully: {quiz_id}")

        result = quiz_to_response(updated_quiz)
        return result

    async def delete_quiz(
        self,
        quiz_id: str,
        force: bool = False
    ) -> MessageResponseSchema:
        """
        Hard delete a quiz with cascade.
        """
        logger.info(f"Delete quiz attempt for id: {quiz_id} (force={force})")

        existing_quiz = await self.quiz_repo.find_by_id(quiz_id)
        if not existing_quiz:
            logger.warning(f"Delete failed — quiz not found: {quiz_id}")
            raise QuizNotFoundException()

        # safety check — don't wipe out a quiz a student is actively
        # attempting, unless admin explicitly forces it
        active_count = await self.attempt_repo.count_in_progress_by_quiz_ids([quiz_id])
        if active_count and not force:
            logger.warning(
                f"Delete blocked — {active_count} active attempt(s) on quiz: {quiz_id}"
            )
            raise QuizHasActiveAttemptsException(active_count)

        # cascade — remove every question that belongs to this quiz first
        deleted_questions = await self.question_repo.delete_by_quiz(quiz_id)

        # then remove the quiz itself
        await self.quiz_repo.delete(quiz_id)

        logger.info(
            f"Quiz hard deleted: '{existing_quiz['title']}' "
            f"({deleted_questions} questions removed)"
        )

        result = MessageResponseSchema(
            message=(
                f"Quiz '{existing_quiz['title']}' and its {deleted_questions} "
                f"question(s) deleted successfully"
            )
        )
        return result