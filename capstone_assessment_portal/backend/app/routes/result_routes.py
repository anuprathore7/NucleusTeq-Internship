from fastapi import APIRouter, Depends, status

from app.schemas.response.result_response_schema import (
    ResultResponseSchema,
    ResultSummaryListResponseSchema
)
from app.services.result_service import ResultService
from app.utils.auth_dependencies import require_student, require_admin

router = APIRouter(
    prefix="/results",
    tags=["Results"]
)

result_service = ResultService()


@router.get(
    "/my",
    status_code=status.HTTP_200_OK,
    response_model=ResultSummaryListResponseSchema,
    summary="Get my results",
    description="Student only. Returns all submitted quiz results for the current student."
)
async def get_my_results(
    current_user: dict = Depends(require_student)
):
    """
    Returns all submitted quiz results for the logged in student.
    Shows score, percentage and pass/fail for each attempt.
    No answer breakdown here — use get_result_by_attempt for full details.

    Placed BEFORE /{attempt_id} so FastAPI does not
    match 'my' as an attempt_id.
    """
    result = await result_service.get_my_results(current_user["id"])
    return result


@router.get(
    "/admin/all",
    status_code=status.HTTP_200_OK,
    response_model=ResultSummaryListResponseSchema,
    summary="Admin — get all results",
    description="Admin only. Returns all submitted quiz results across all students."
)
async def get_all_results(
    current_user: dict = Depends(require_admin)
):
    """
    Admin dashboard — overview of all students quiz results.
    Returns score summary for every submitted attempt.

    Placed BEFORE /{attempt_id} so FastAPI does not
    match 'admin' as an attempt_id.
    """
    result = await result_service.get_all_results_admin()
    return result


@router.get(
    "/admin/quiz/{quiz_id}",
    status_code=status.HTTP_200_OK,
    response_model=ResultSummaryListResponseSchema,
    summary="Admin — get results by quiz",
    description="Admin only. Returns all results for a specific quiz."
)
async def get_results_by_quiz(
    quiz_id: str,
    current_user: dict = Depends(require_admin)
):
    """
    Admin can see how all students performed on one specific quiz.
    Useful for identifying if a quiz is too hard or too easy.
    """
    result = await result_service.get_results_by_quiz_admin(quiz_id)
    return result


@router.get(
    "/{attempt_id}",
    status_code=status.HTTP_200_OK,
    response_model=ResultResponseSchema,
    summary="Get result by attempt",
    description="Student only. Returns full result with answer breakdown for one attempt."
)
async def get_result(
    attempt_id: str,
    current_user: dict = Depends(require_student)
):
    """
    Student views their full result for one specific attempt.
    It Shows:
    overall score, percentage, pass/fail
    per question: what they answered, correct answer, marks

    correct_answer is shown here because attempt is already submitted
    """
    result = await result_service.get_result_by_attempt(
        attempt_id,
        current_user["id"]
    )
    return result