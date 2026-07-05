from fastapi import APIRouter, Depends, status

from app.schemas.request.quiz_schema import CreateQuizSchema, UpdateQuizSchema
from app.schemas.response.quiz_response_schema import (
    QuizResponseSchema,
    QuizListResponseSchema
)
from app.services.quiz_service import QuizService
from app.utils.auth_dependencies import require_admin, get_current_user

# all routes in this file start with /quizzes
router = APIRouter(
    prefix="/quizzes",
    tags=["Quizzes"]        # groups these routes in Swagger UI
)

quiz_service = QuizService()


@router.post(
    "/",
    status_code=status.HTTP_201_CREATED,
    response_model=QuizResponseSchema,
    summary="Create a new quiz",
    description="Admin only. Creates a new quiz under an existing category."
)
async def create_quiz(
    data: CreateQuizSchema,
    current_user: dict = Depends(require_admin)     # blocks non-admins
):
    """
    Admin creates a quiz by providing title, description,
    category_id, time_limit and pass_percentage.

    require_admin runs BEFORE this function.
    If the user is not admin, 403 is returned immediately.
    Pydantic validates the request body automatically.
    """
    return await quiz_service.create_quiz(data)


@router.get(
    "/",
    status_code=status.HTTP_200_OK,
    response_model=QuizListResponseSchema,
    summary="Get all quizzes",
    description="Returns all active quizzes. Accessible by any logged in user."
)
async def get_all_quizzes(
    current_user: dict = Depends(get_current_user)  # any logged in user
):
    """
    Returns all active quizzes with total count.
    Students use this to browse available quizzes.
    """
    return await quiz_service.get_all_quizzes()


@router.get(
    "/category/{category_id}",
    status_code=status.HTTP_200_OK,
    response_model=QuizListResponseSchema,
    summary="Get quizzes by category",
    description="Returns all quizzes under a specific category."
)
async def get_quizzes_by_category(
    category_id: str,
    current_user: dict = Depends(get_current_user)
):
    """
    Students and admins can filter quizzes by category.
    Useful for the category detail page on the frontend.

    This route is placed BEFORE /{quiz_id} intentionally.
    If we put /{quiz_id} first, FastAPI might try to match
    'category' as a quiz_id — always put specific paths before dynamic ones.
    """
    return await quiz_service.get_quizzes_by_category(category_id)


@router.get(
    "/{quiz_id}",
    status_code=status.HTTP_200_OK,
    response_model=QuizResponseSchema,
    summary="Get a single quiz",
    description="Returns details of one specific quiz by its ID."
)
async def get_quiz(
    quiz_id: str,
    current_user: dict = Depends(get_current_user)
):
    """
    Fetch one quiz by ID.
    Used when student clicks on a quiz to see its details.
    Also used by admin to view quiz before editing.
    """
    return await quiz_service.get_quiz_by_id(quiz_id)


@router.put(
    "/{quiz_id}",
    status_code=status.HTTP_200_OK,
    response_model=QuizResponseSchema,
    summary="Update a quiz",
    description="Admin only. Update any field of an existing quiz."
)
async def update_quiz(
    quiz_id: str,
    data: UpdateQuizSchema,
    current_user: dict = Depends(require_admin)
):
    """
    Admin can update title, description, category, time limit
    or pass percentage. All fields are optional — send only what changed.
    """
    return await quiz_service.update_quiz(quiz_id, data)


@router.delete(
    "/{quiz_id}",
    status_code=status.HTTP_200_OK,
    summary="Delete a quiz",
    description="Admin only. Soft deletes a quiz by marking it inactive."
)
async def delete_quiz(
    quiz_id: str,
    current_user: dict = Depends(require_admin)
):
    """
    Soft delete — quiz is marked inactive, not permanently removed.
    Student attempt history linked to this quiz stays intact.
    """
    return await quiz_service.delete_quiz(quiz_id)