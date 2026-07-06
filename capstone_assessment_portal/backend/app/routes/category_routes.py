from fastapi import APIRouter, Depends, status

from app.schemas.request.category_schema import (
    CreateCategorySchema,
    UpdateCategorySchema
)
from app.schemas.response.category_response_schema import (
    CategoryResponseSchema,
    CategoryListResponseSchema
)
from app.services.category_service import CategoryService
from app.utils.auth_dependencies import require_admin, get_current_user
from app.constants.url_prefix import CATEGORY_PREFIX

# all routes in this file start with /categories
router = APIRouter(
    prefix=CATEGORY_PREFIX,
    tags=["Categories"]
)

category_service = CategoryService()


@router.post(
    "/",
    status_code=status.HTTP_201_CREATED,
    response_model=CategoryResponseSchema,
    summary="Create a new category",
    description="Admin only. Creates a new quiz category like 'Python' or 'DSA'."
)
async def create_category(
    data: CreateCategorySchema,
    current_user: dict = Depends(require_admin)  # only admins can create
):
    """
    Admin sends category name and description.
    Pydantic validates the body automatically.
    require_admin dependency blocks non-admin users before this runs.
    """
    result = await category_service.create_category(data)
    return result


@router.get(
    "/",
    status_code=status.HTTP_200_OK,
    response_model=CategoryListResponseSchema,
    summary="Get all categories",
    description="Returns all active categories. Accessible by any logged in user."
)
async def get_categories(
    current_user: dict = Depends(get_current_user)  # any logged in user
):
    """
    Returns list of all active categories with total count.
    Both admin and student can access this.
    """
    result = await category_service.get_all_categories()
    return result

@router.get(""
    "/{category_id}" , 
    status_code=status.HTTP_200_OK,
    summary="Get a category",
    description="Return a single category accessible by only logged in user",
    response_model= CategoryResponseSchema
      )
async def get_a_category(
    category_id : str , 
    current_user : dict = Depends(get_current_user)
    ):
    result = await category_service.get_a_category(category_id)
    return result


@router.put(
    "/{category_id}",
    status_code=status.HTTP_200_OK,
    response_model=CategoryResponseSchema,
    summary="Update a category",
    description="Admin only. Update name or description of an existing category."
)
async def update_category(
    category_id: str,               # extracted from URL path automatically
    data: UpdateCategorySchema,
    current_user: dict = Depends(require_admin)
):
    """
    category_id comes from the URL: /categories/64abc123...
    Both name and description are optional in the request body.
    Only provided fields are updated.
    """
    result = await category_service.update_category(category_id, data)
    return result


@router.delete(
    "/{category_id}",
    status_code=status.HTTP_200_OK,
    summary="Delete a category",
    description="Admin only. Hard deletes a category "
)
async def delete_category(
    category_id: str,
    current_user: dict = Depends(require_admin)
):
    """
    Hard delete — category is permanently removed.
    """
    result = await category_service.delete_category(category_id)
    return result