from datetime import datetime, timezone

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
    CategoryAlreadyExistsException
)
from app.utils.category_mapper import (
    category_to_response,
    categories_to_response
)


class CategoryService:
    """
    Contains all business logic for category operations.
    Sits between routes (HTTP layer) and repository (DB layer).
    Routes call services. Services call repositories.
    """

    def __init__(self):
        self.repo = CategoryRepository()

    async def create_category(
        self,
        data: CreateCategorySchema
    ) -> CategoryResponseSchema:
        """
        Create a new category.
        """

        # duplicate name check (case-insensitive)
        existing = await self.repo.find_by_name(data.name)
        if existing:
            raise CategoryAlreadyExistsException()

        # build the MongoDB document
        new_category = {
            **data.model_dump(),
            "is_active": True,
            "created_at": datetime.now(timezone.utc),
            "updated_at": datetime.now(timezone.utc)    # same as created on first save
        }

        # save to DB
        saved = await self.repo.create(new_category)

        # convert to response schema and return
        return category_to_response(saved)

    async def get_all_categories(self) -> CategoryListResponseSchema:
        """
        Fetch all active categories.
        Available to any logged in user — both admin and student.
        """
        categories = await self.repo.find_all()
        return categories_to_response(categories)
    
    async def get_a_category(
            self ,
            category_id : str
            ) -> CategoryResponseSchema:
        """
        Fetch one category for logged in user 
        """
        existing = await self.repo.find_by_id(category_id)
        if not existing:
            raise CategoryNotFoundException()
        return category_to_response(existing)

    async def update_category(
        self,
        category_id: str,
        data: UpdateCategorySchema
    ) -> CategoryResponseSchema:
        """
        Update an existing category.
        """

        #  confirm category exists
        existing = await self.repo.find_by_id(category_id)
        if not existing:
            raise CategoryNotFoundException()

        # if name is being changed, check it's not already taken
        if data.name and data.name.lower() != existing["name"].lower():
            name_conflict = await self.repo.find_by_name(data.name)
            if name_conflict:
                raise CategoryAlreadyExistsException()

        # build update dict with only the fields that were provided
        # exclude_none=True means fields the admin didn't send won't be included
        # so we don't accidentally overwrite existing data with None
        update_data = data.model_dump(exclude_none=True)

        # always update the updated_at timestamp
        update_data["updated_at"] = datetime.now(timezone.utc)

        # save and return
        updated = await self.repo.update(category_id, update_data)
        return category_to_response(updated)

    async def delete_category(self, category_id: str) -> dict:
        """
        Soft delete a category.
        """

        # Step 1 — confirm it exists before trying to delete
        existing = await self.repo.find_by_id(category_id)
        if not existing:
            raise CategoryNotFoundException()

        # soft delete (sets is_active = False)
        await self.repo.delete(category_id)

        #  return a confirmation message
        return {"message": f"Category '{existing['name']}' deleted successfully"}