from fastapi import HTTPException, status

from app.constants.message import (
    CATEGORY_NOT_FOUND,
    CATEGORY_ALREADY_EXISTS,
    CATEGORY_INVALID_ID
)


class CategoryNotFoundException(HTTPException):
    """Raised when a category with the given ID does not exist."""
    def __init__(self):
        super().__init__(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=CATEGORY_NOT_FOUND
        )


class CategoryAlreadyExistsException(HTTPException):
    """Raised when a category with the same name already exists."""
    def __init__(self):
        super().__init__(
            status_code=status.HTTP_409_CONFLICT,
            detail=CATEGORY_ALREADY_EXISTS
        )


class CategoryInvalidIdException(HTTPException):
    """Raised when the provided ID is not a valid MongoDB ObjectId format."""
    def __init__(self):
        super().__init__(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=CATEGORY_INVALID_ID
        )