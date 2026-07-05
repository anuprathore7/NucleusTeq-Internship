from datetime import datetime, timezone

from app.config.logger import get_logger
from app.repository.category_repository import CategoryRepository
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
    CategoryHasQuizzesException 
)
from app.utils.category_mapper import (
    category_to_response,
    categories_to_response
)
from app.schemas.response.message_response_schema import MessageResponseSchema
from app.constants.message import CATEGORY_DELETED

logger = get_logger(__name__)


class CategoryService:
    """
    Contains all business logic for category operations.
    """

    def __init__(self):
        self.repo = CategoryRepository()

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
        Fetch all active categories for any logged in user.
        """
        logger.info("Fetching all active categories")

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

    async def delete_category(self, category_id: str) -> MessageResponseSchema:
        """
        Hard delete a category after verifying no quizzes are linked to it.
        """
        logger.info(f"Delete category attempt for id: {category_id}")

        existing = await self.repo.find_by_id(category_id)
        if not existing:
            logger.warning(f"Delete failed — category not found: {category_id}")
            raise CategoryNotFoundException()

        quiz_count = await self.repo.count_quizzes_by_category(category_id)
        if quiz_count > 0:
            logger.warning(
                f"Delete failed — category {category_id} "
                f"has {quiz_count} linked quizzes"
            )
            raise CategoryHasQuizzesException()

        await self.repo.delete(category_id)
        logger.info(f"Category hard deleted successfully: {existing['name']}")

        result = MessageResponseSchema(message=CATEGORY_DELETED)
        return result