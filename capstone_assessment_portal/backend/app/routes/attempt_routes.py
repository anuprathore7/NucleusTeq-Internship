

from fastapi import APIRouter, Depends, status

from app.schemas.request.attempt_schema import (
    StartAttemptSchema,
    SaveAnswerSchema,
    SubmitAttemptSchema
)
from app.schemas.response.attempt_response_schema import (
    AttemptResponseSchema,
    AttemptListResponseSchema
)
from app.schemas.response.message_response_schema import MessageResponseSchema
from app.services.attempt_service import AttemptService
from app.utils.auth_dependencies import require_student

router = APIRouter(
    prefix="/attempts",
    tags=["Attempts"]
)

attempt_service = AttemptService()


@router.post(
    "/start",
    status_code=status.HTTP_201_CREATED,
    response_model=AttemptResponseSchema,
    summary="Start a quiz attempt",
    description="Student only. Starts a new attempt and locks a question snapshot."
)
async def start_attempt(
    data: StartAttemptSchema,
    current_user: dict = Depends(require_student)
):
    """
    Student sends quiz_id to start an attempt.
    System copies quiz and all questions into a snapshot.
    Returns attempt with questions — no correct answers.
    Max 2 attempts per quiz per student.
    """
    result = await attempt_service.start_attempt(
        data,
        current_user["id"]
    )
    return result


@router.get(
    "/my",
    status_code=status.HTTP_200_OK,
    response_model=AttemptListResponseSchema,
    summary="Get my attempts",
    description="Student only. Returns all attempts for the current student."
)
async def get_my_attempts(
    current_user: dict = Depends(require_student)
):
    """
    Returns all attempts for the currently logged in student.
    Placed BEFORE /{attempt_id} so FastAPI does not
    try to match 'my' as an attempt_id.
    """
    result = await attempt_service.get_my_attempts(current_user["id"])
    return result


@router.get(
    "/{attempt_id}",
    status_code=status.HTTP_200_OK,
    summary="Resume attempt",
    description="Student only. Resume attempt and see already saved answers."
)
async def get_attempt(
    attempt_id: str,
    current_user: dict = Depends(require_student)
):
    """
    Returns questions and already saved answers.
    Frontend uses saved answers to tick the options student already selected.

    If time has expired:
    → auto submits with saved answers
    → returns auto submitted message instead of attempt
    """
    result = await attempt_service.get_attempt(
        attempt_id,
        current_user["id"]
    )
    return result


@router.post(
    "/{attempt_id}/answer",
    status_code=status.HTTP_200_OK,
    response_model=MessageResponseSchema,
    summary="Save a single answer",
    description="Student only. Frontend calls this automatically when student selects an option."
)
async def save_answer(
    attempt_id: str,
    data: SaveAnswerSchema,
    current_user: dict = Depends(require_student)
):
    """
    Saves one answer for one question.
    Frontend calls this every time student selects or changes an option.
    Student does NOT manually click save — it is automatic.

    If student changes answer → previous answer is overwritten.
    Only one answer per question stored at any time.

    If time expired when this is called:
    → auto submits with all saved answers so far
    → returns auto submitted message
    """
    result = await attempt_service.save_answer(
        attempt_id,
        data,
        current_user["id"]
    )
    return result


@router.post(
    "/{attempt_id}/submit",
    status_code=status.HTTP_200_OK,
    response_model=MessageResponseSchema,
    summary="Submit quiz attempt",
    description="Student only. Manually submit quiz. Evaluates all saved answers."
)
async def submit_attempt(
    attempt_id: str,
    current_user: dict = Depends(require_student)
):
    """
    Student manually clicks submit.
    No body needed — all answers already saved via /answer endpoint.
    System evaluates whatever is saved in attempt.answers.

    Unanswered questions get 0 marks.
    Returns success message.
    Result details available through Result module later.
    """
    result = await attempt_service.submit_attempt(
        attempt_id,
        current_user["id"]
    )
    return result