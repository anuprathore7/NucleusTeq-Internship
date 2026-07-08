from fastapi import HTTPException, status

from app.constants.message import (
    RESULT_NOT_FOUND,
    RESULT_ATTEMPT_NOT_SUBMITTED
)


class ResultNotFoundException(HTTPException):
    """
    Raised when result is requested for an attempt
    that does not exist in the database.
    """
    def __init__(self):
        super().__init__(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=RESULT_NOT_FOUND
        )


class ResultAttemptNotSubmittedException(HTTPException):
    """
    Raised when student tries to view result
    for an attempt that is still in_progress.
    Student must submit first before viewing result.
    """
    def __init__(self):
        super().__init__(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=RESULT_ATTEMPT_NOT_SUBMITTED
        )