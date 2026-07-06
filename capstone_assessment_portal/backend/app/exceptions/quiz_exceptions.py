from fastapi import HTTPException, status

from app.constants.message import (
    QUIZ_NOT_FOUND,
    QUIZ_ALREADY_EXISTS,
    QUIZ_INVALID_ID,
    QUIZ_CATEGORY_NOT_FOUND,
    QUIZ_HAS_QUESTIONS             
)


class QuizNotFoundException(HTTPException):
    """Raised when a quiz with the given ID does not exist."""
    def __init__(self):
        super().__init__(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=QUIZ_NOT_FOUND
        )


class QuizAlreadyExistsException(HTTPException):
    """Raised when a quiz with same title exists in same category."""
    def __init__(self):
        super().__init__(
            status_code=status.HTTP_409_CONFLICT,
            detail=QUIZ_ALREADY_EXISTS
        )


class QuizInvalidIdException(HTTPException):
    """Raised when the provided ID is not a valid MongoDB ObjectId format."""
    def __init__(self):
        super().__init__(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=QUIZ_INVALID_ID
        )


class QuizCategoryNotFoundException(HTTPException):
    """Raised when the category_id provided for a quiz does not exist."""
    def __init__(self):
        super().__init__(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=QUIZ_CATEGORY_NOT_FOUND
        )


class QuizHasQuestionsException(HTTPException):
    """
    Raised when admin tries to delete a quiz that still
    has questions linked to it.
    Admin must delete all questions under this quiz first
    before the quiz itself can be deleted.
    """
    def __init__(self):
        super().__init__(
            status_code=status.HTTP_409_CONFLICT,
            detail=QUIZ_HAS_QUESTIONS
        )