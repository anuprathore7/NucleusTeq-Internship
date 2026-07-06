from fastapi import APIRouter, Depends, status

from app.schemas.request.question_schema import (
    CreateQuestionSchema,
    UpdateQuestionSchema
)
from app.schemas.response.question_response_schema import (
    QuestionResponseSchema,
    QuestionListResponseSchema,
    QuestionStudentResponseSchema,
    QuestionStudentListResponseSchema
)
from app.services.question_service import QuestionService
from app.utils.auth_dependencies import require_admin, get_current_user , require_student
from app.constants.question_constants import EASY, MEDIUM, HARD

router = APIRouter(
    prefix="/questions",
    tags=["Questions"]
)

question_service = QuestionService()


@router.post(
    "/",
    status_code=status.HTTP_201_CREATED,
    response_model=QuestionResponseSchema,
    summary="Add a question to a quiz",
    description="Admin only. Adds a new MCQ or True/False question to an existing quiz."
)
async def create_question(
    data: CreateQuestionSchema,
    current_user: dict = Depends(require_admin)
):
    """
    Pydantic validates the body AND runs model_validator
    to check cross-field rules before this function runs.
    """
    result = await question_service.create_question(data)
    return result


@router.get(
    "/quiz/{quiz_id}",
    status_code=status.HTTP_200_OK,
    response_model=QuestionListResponseSchema,
    summary="Get all questions for a quiz",
    description="Returns all active questions in a specific quiz."
)
async def get_questions_by_quiz(
    quiz_id: str,
    current_user: dict = Depends(require_admin)
):
    """
    Both admin and student can access this.
    Admin uses it to manage questions in a quiz.
    Student uses it when starting a quiz attempt.
    """
    result = await question_service.get_questions_by_quiz(quiz_id)
    return result


@router.get(
    "/quiz/{quiz_id}/difficulty/{difficulty}",
    status_code=status.HTTP_200_OK,
    response_model=QuestionListResponseSchema,
    summary="Get questions by difficulty",
    description="Filter questions in a quiz by difficulty level: easy, medium, or hard."
)
async def get_questions_by_difficulty(
    quiz_id: str,
    difficulty: str,
    current_user: dict = Depends(require_admin)
):
    """
    Filter questions by difficulty.
    Useful for admin to review question distribution.
    """
    result = await question_service.get_questions_by_difficulty(quiz_id, difficulty)
    return result


@router.get(
    "/quiz/{quiz_id}/count",
    status_code=status.HTTP_200_OK,
    summary="Get question count for a quiz",
    description="Returns total number of active questions in a quiz."
)
async def get_question_count(
    quiz_id: str,
    current_user: dict = Depends(require_admin)
):
    """
    Admin uses this to check how many questions a quiz has.
    Helps ensure quiz has enough questions before publishing.
    """
    result = await question_service.get_question_count_by_quiz(quiz_id)
    return result


@router.get(
    "/{question_id}",
    status_code=status.HTTP_200_OK,
    response_model=QuestionResponseSchema,
    summary="Get a single question",
    description="Fetch details of one question by its ID."
)
async def get_question(
    question_id: str,
    current_user: dict = Depends(require_admin)
):
    """
    Fetch one question by ID.
    Used by admin to view question details before editing.
    """
    result = await question_service.get_question_by_id(question_id)
    return result

# ─────────────────────────────────────────────────────────────────────────
# STUDENT ROUTES — correct_answer is never exposed in any of these
# ─────────────────────────────────────────────────────────────────────────

@router.get(
    "/student/quiz/{quiz_id}",
    status_code=status.HTTP_200_OK,
    response_model=QuestionStudentListResponseSchema,
    summary="Get all questions for a quiz (student view)",
    description="Returns all questions in a quiz without the correct answer."
)
async def get_questions_by_quiz_for_student(
    quiz_id: str,
    current_user: dict = Depends(require_student)
):
    """
    Student-safe version of get_questions_by_quiz.
    Placed before /student/{question_id} so FastAPI does not
    try to match 'quiz' as a question_id.
    """
    result = await question_service.get_questions_by_quiz_for_student(quiz_id)
    return result


@router.get(
    "/student/quiz/{quiz_id}/difficulty/{difficulty}",
    status_code=status.HTTP_200_OK,
    response_model=QuestionStudentListResponseSchema,
    summary="Get questions by difficulty (student view)",
    description="Returns questions filtered by difficulty without the correct answer."
)
async def get_questions_by_difficulty_for_student(
    quiz_id: str,
    difficulty: str,
    current_user: dict = Depends(require_student)
):
    """
    Student-safe version of get_questions_by_difficulty.
    """
    result = await question_service.get_questions_by_difficulty_for_student(
        quiz_id,
        difficulty
    )
    return result


@router.get(
    "/student/{question_id}",
    status_code=status.HTTP_200_OK,
    response_model=QuestionStudentResponseSchema,
    summary="Get a single question (student view)",
    description="Returns one question without the correct answer."
)
async def get_question_for_student(
    question_id: str,
    current_user: dict = Depends(require_student)
):
    """
    Student-safe version of get_question.
    """
    result = await question_service.get_question_by_id_for_student(question_id)
    return result


@router.put(
    "/{question_id}",
    status_code=status.HTTP_200_OK,
    response_model=QuestionResponseSchema,
    summary="Update a question",
    description="Admin only. Update any field of an existing question."
)
async def update_question(
    question_id: str,
    data: UpdateQuestionSchema,
    current_user: dict = Depends(require_admin)
):
    """
    Admin can update question text, options, correct answer,
    difficulty, tags, or marks. All fields are optional.
    Only provided fields are updated.
    """
    result = await question_service.update_question(question_id, data)
    return result


@router.delete(
    "/{question_id}",
    status_code=status.HTTP_200_OK,
    summary="Delete a question",
    description="Admin only. Hard deletes a question "
)
async def delete_question(
    question_id: str,
    current_user: dict = Depends(require_admin)
):
    """
    Hard delete — question is permanently removed.
    """
    result = await question_service.delete_question(question_id)
    return result