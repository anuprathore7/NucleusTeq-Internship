from fastapi import HTTPException, status

from app.constants.message import (
    ATTEMPT_NOT_FOUND,
    ATTEMPT_INVALID_ID,
    ATTEMPT_QUIZ_NOT_FOUND,
    ATTEMPT_ALREADY_SUBMITTED,
    ATTEMPT_NOT_IN_PROGRESS,
    ATTEMPT_UNAUTHORIZED,
    ATTEMPT_INVALID_QUESTION,
    ATTEMPT_MAX_REACHED 
)


class AttemptNotFoundException(HTTPException):
    """
    Raised when an attempt with the given ID does not exist.
    Example: student tries to resume an attempt that was deleted.
    """
    def __init__(self):
        super().__init__(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=ATTEMPT_NOT_FOUND
        )


class AttemptInvalidIdException(HTTPException):
    """
    Raised when the provided attempt ID is not a valid MongoDB ObjectId.
    Prevents MongoDB from crashing with internal errors on bad IDs.
    """
    def __init__(self):
        super().__init__(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=ATTEMPT_INVALID_ID
        )


class AttemptQuizNotFoundException(HTTPException):
    """
    Raised when student tries to start an attempt for a quiz
    that does not exist in the database.
    """
    def __init__(self):
        super().__init__(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=ATTEMPT_QUIZ_NOT_FOUND
        )


class AttemptAlreadySubmittedException(HTTPException):
    """
    Raised when student tries to submit an attempt
    that has already been submitted.
    Prevents double submission.
    """
    def __init__(self):
        super().__init__(
            status_code=status.HTTP_409_CONFLICT,
            detail=ATTEMPT_ALREADY_SUBMITTED
        )


class AttemptNotInProgressException(HTTPException):
    """
    Raised when student tries to submit an attempt
    that is not currently in progress.
    """
    def __init__(self):
        super().__init__(
            status_code=status.HTTP_409_CONFLICT,
            detail=ATTEMPT_NOT_IN_PROGRESS
        )


class AttemptUnauthorizedException(HTTPException):
    """
    Raised when a student tries to access an attempt
    that belongs to a different student.
    Every student can only see their own attempts.
    """
    def __init__(self):
        super().__init__(
            status_code=status.HTTP_403_FORBIDDEN,
            detail=ATTEMPT_UNAUTHORIZED
        )


class AttemptInvalidQuestionException(HTTPException):
    """
    Raised when student submits answers containing question IDs
    that do not belong to the quiz in this attempt.
    Prevents students from submitting answers for wrong questions.
    """
    def __init__(self):
        super().__init__(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=ATTEMPT_INVALID_QUESTION
        )
class AttemptMaxReachedException(HTTPException):
    """
    Raised when student tries to start a new attempt
    but has already reached the maximum allowed attempts
    for this quiz. Maximum is 2 attempts per quiz.
    """
    def __init__(self):
        super().__init__(
            status_code=status.HTTP_409_CONFLICT,
            detail=ATTEMPT_MAX_REACHED
        )