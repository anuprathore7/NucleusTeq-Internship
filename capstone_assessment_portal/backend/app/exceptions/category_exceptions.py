from fastapi import HTTPException, status


class CategoryNotFoundException(HTTPException):
    """
    Raised when a category with the given ID does not exist in the database.
    Example: admin tries to update a category that was already deleted.
    """
    def __init__(self):
        super().__init__(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Category not found"
        )


class CategoryAlreadyExistsException(HTTPException):
    """
    Raised when admin tries to create a category
    with a name that already exists.
    Example: 'Python' category already exists, admin tries to create it again.
    """
    def __init__(self):
        super().__init__(
            status_code=status.HTTP_409_CONFLICT,
            detail="Category with this name already exists"
        )


class CategoryInvalidIdException(HTTPException):
    """
    Raised when the provided category ID is not a valid MongoDB ObjectId.
    Example: someone sends 'abc' as an ID instead of a proper ObjectId string.
    """
    def __init__(self):
        super().__init__(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Invalid category ID format"
        )