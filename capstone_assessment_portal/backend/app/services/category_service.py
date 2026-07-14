from datetime import datetime, timezone

from app.config.logger import get_logger
from app.repository.category_repository import CategoryRepository
from app.repository.quiz_repository import QuizRepository
from app.repository.question_repository import QuestionRepository
from app.repository.attempt_repository import AttemptRepository
from app.schemas.request.category_schema import (
    CreateCategorySchema,
    UpdateCategorySchema
)
from app.schemas.response.category_response_schema import (
    CategoryResponseSchema,
    CategoryListResponseSchema
)
from app.exceptions.category_exceptions import (
    CategoryNotFoundException,
    CategoryAlreadyExistsException,
    CategoryHasActiveAttemptsException
)
from app.utils.category_mapper import (
    category_to_response,
    categories_to_response
)
from app.schemas.response.message_response_schema import MessageResponseSchema

logger = get_logger(__name__)


class CategoryService:
    """
    Contains all business logic for category operations.
    """

    def __init__(self):
        self.repo = CategoryRepository()
        # needed for cascade delete — find and remove quizzes under this category
        self.quiz_repo = QuizRepository()
        # needed for cascade delete — find and remove questions under those quizzes
        self.question_repo = QuestionRepository()
        # needed to check if any student is mid-attempt before deleting
        self.attempt_repo = AttemptRepository()

    async def create_category(
        self,
        data: CreateCategorySchema
    ) -> CategoryResponseSchema:
        """
        Create a new category after checking for duplicate names.
        """
        logger.info(f"Create category attempt: {data.name}")

        existing = await self.repo.find_by_name(data.name)
        if existing:
            logger.warning(f"Create category failed — already exists: {data.name}")
            raise CategoryAlreadyExistsException()

        new_category = {
            **data.model_dump(),
            "is_active": True,
            "created_at": datetime.now(timezone.utc),
            "updated_at": datetime.now(timezone.utc)
        }

        saved = await self.repo.create(new_category)
        logger.info(f"Category created successfully: {data.name}")

        result = category_to_response(saved)
        return result

    async def get_all_categories(self) -> CategoryListResponseSchema:
        """
        Fetch all categories for any logged in user.
        """
        logger.info("Fetching all categories")

        categories = await self.repo.find_all()
        logger.info(f"Returned {len(categories)} categories")

        result = categories_to_response(categories)
        return result

    async def get_a_category(
        self,
        category_id: str
    ) -> CategoryResponseSchema:
        """
        Fetch a single category by its ID.
        """
        logger.info(f"Fetching category by id: {category_id}")

        existing = await self.repo.find_by_id(category_id)
        if not existing:
            logger.warning(f"Category not found: {category_id}")
            raise CategoryNotFoundException()

        logger.info(f"Category found: {existing['name']}")

        result = category_to_response(existing)
        return result

    async def update_category(
        self,
        category_id: str,
        data: UpdateCategorySchema
    ) -> CategoryResponseSchema:
        """
        Update an existing category's name or description.
        """
        logger.info(f"Update category attempt for id: {category_id}")

        existing = await self.repo.find_by_id(category_id)
        if not existing:
            logger.warning(f"Update failed — category not found: {category_id}")
            raise CategoryNotFoundException()

        if data.name and data.name.lower() != existing["name"].lower():
            name_conflict = await self.repo.find_by_name(data.name)
            if name_conflict:
                logger.warning(f"Update failed — name already exists: {data.name}")
                raise CategoryAlreadyExistsException()

        update_data = data.model_dump(exclude_none=True)
        update_data["updated_at"] = datetime.now(timezone.utc)

        updated = await self.repo.update(category_id, update_data)
        logger.info(f"Category updated successfully: {category_id}")

        result = category_to_response(updated)
        return result

    async def delete_category(
        self,
        category_id: str,
        force: bool = False
    ) -> MessageResponseSchema:
        """
        Hard delete a category with cascade.

        Flow:
        1. Verify category exists
        2. Find all quiz IDs under this category
        3. If quizzes exist, check if any student has an in_progress
           attempt on any of them — block with 409 unless force=True
        4. Delete all questions under those quizzes
        5. Delete all quizzes under this category
        6. Delete the category itself

        If the category has no quizzes at all, steps 3-5 are skipped
        and only the category document is removed.
        """
        logger.info(f"Delete category attempt for id: {category_id} (force={force})")

        existing = await self.repo.find_by_id(category_id)
        if not existing:
            logger.warning(f"Delete failed — category not found: {category_id}")
            raise CategoryNotFoundException()

        # find every quiz that belongs to this category, so we know
        # exactly what to cascade delete before touching the category
        quiz_ids = await self.quiz_repo.find_ids_by_category(category_id)

        deleted_questions = 0
        deleted_quizzes = 0

        # only run the cascade steps if this category actually has quizzes
        if quiz_ids:

            # safety check — don't wipe a quiz out from under a student
            # who is actively answering it, unless admin explicitly forces it
            active_count = await self.attempt_repo.count_in_progress_by_quiz_ids(quiz_ids)
            if active_count and not force:
                logger.warning(
                    f"Delete blocked — {active_count} active attempt(s) "
                    f"under category: {category_id}"
                )
                raise CategoryHasActiveAttemptsException(active_count)

            # step 1 of cascade — remove every question under every quiz
            # in this category, in a single bulk delete
            deleted_questions = await self.question_repo.delete_by_quiz_ids(quiz_ids)

            # step 2 of cascade — remove every quiz under this category
            deleted_quizzes = await self.quiz_repo.delete_by_category(category_id)

        # step 3 — finally remove the category document itself
        await self.repo.delete(category_id)

        logger.info(
            f"Category hard deleted: {existing['name']} "
            f"({deleted_quizzes} quizzes, {deleted_questions} questions removed)"
        )

        # build a clear success message so admin knows exactly what was removed
        result = MessageResponseSchema(
            message=(
                f"Category '{existing['name']}' and its {deleted_quizzes} quiz(zes) "
                f"and {deleted_questions} question(s) deleted successfully"
            )
        )
        return result