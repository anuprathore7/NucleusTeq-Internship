from fastapi import HTTPException, status

from app.constants.message import (
    QUESTION_NOT_FOUND,
    QUESTION_ALREADY_EXISTS,
    QUESTION_INVALID_ID,
    QUESTION_QUIZ_NOT_FOUND,
    QUESTION_INVALID_OPTIONS,
    QUESTION_INVALID_TRUE_FALSE,
    QUESTION_INVALID_CORRECT_ANSWER
)


class QuestionNotFoundException(HTTPException):
    """
    Raised when a question with the given ID does not exist.
    """
    def __init__(self):
        super().__init__(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=QUESTION_NOT_FOUND
        )


class QuestionAlreadyExistsException(HTTPException):
    """
    Raised when a question with the same text already exists in the same quiz.
    Prevents duplicate questions inside one quiz.
    """
    def __init__(self):
        super().__init__(
            status_code=status.HTTP_409_CONFLICT,
            detail=QUESTION_ALREADY_EXISTS
        )


class QuestionInvalidIdException(HTTPException):
    """
    Raised when the provided question ID is not a valid MongoDB ObjectId.
    Prevents MongoDB from crashing with internal errors on bad IDs.
    """
    def __init__(self):
        super().__init__(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=QUESTION_INVALID_ID
        )


class QuestionQuizNotFoundException(HTTPException):
    """
    Raised when the quiz_id provided for a question does not exist.
    Prevents orphan questions that belong to no quiz.
    """
    def __init__(self):
        super().__init__(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=QUESTION_QUIZ_NOT_FOUND
        )


class QuestionInvalidOptionsException(HTTPException):
    """
    Raised when MCQ question does not have exactly 4 options.
    """
    def __init__(self):
        super().__init__(
            status_code=status.HTTP_422_UNPROCESSABLE_ENTITY,
            detail=QUESTION_INVALID_OPTIONS
        )


class QuestionInvalidTrueFalseException(HTTPException):
    """
    Raised when True/False question options are not exactly ["True", "False"].
    """
    def __init__(self):
        super().__init__(
            status_code=status.HTTP_422_UNPROCESSABLE_ENTITY,
            detail=QUESTION_INVALID_TRUE_FALSE
        )


class QuestionInvalidCorrectAnswerException(HTTPException):
    """
    Raised when correct_answer does not match any of the provided options.
    """
    def __init__(self):
        super().__init__(
            status_code=status.HTTP_422_UNPROCESSABLE_ENTITY,
            detail=QUESTION_INVALID_CORRECT_ANSWER
        )