from fastapi import HTTPException, status

"""
We define all auth-related HTTP errors in one place.
This way, if we want to change an error message,
we change it here and it updates everywhere automatically.
"""

# import all messages from the central constants file
# no hardcoded strings here anymore
from app.constants.message import (
    USER_ALREADY_EXISTS,
    INVALID_CREDENTIALS,
    USER_NOT_FOUND,
    TOKEN_EXPIRED,
    INVALID_TOKEN,
    INSUFFICIENT_PERMISSIONS
)


class UserAlreadyExistsException(HTTPException):
    """Raised when email or username is already registered."""
    def __init__(self):
        super().__init__(
            status_code=status.HTTP_409_CONFLICT,
            detail=USER_ALREADY_EXISTS      
        )


class InvalidCredentialsException(HTTPException):
    """Raised when email or password is wrong during login."""
    def __init__(self):
        super().__init__(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail=INVALID_CREDENTIALS,
            headers={"WWW-Authenticate": "Bearer"}
        )


class UserNotFoundException(HTTPException):
    """Raised when a user lookup by ID returns nothing."""
    def __init__(self):
        super().__init__(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=USER_NOT_FOUND
        )


class TokenExpiredException(HTTPException):
    """Raised when a JWT token is past its expiry time."""
    def __init__(self):
        super().__init__(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail=TOKEN_EXPIRED,
            headers={"WWW-Authenticate": "Bearer"}
        )


class InvalidTokenException(HTTPException):
    """Raised when a JWT token is malformed or signature is wrong."""
    def __init__(self):
        super().__init__(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail=INVALID_TOKEN,
            headers={"WWW-Authenticate": "Bearer"}
        )


class InsufficientPermissionsException(HTTPException):
    """Raised when a user is logged in but not allowed to do this action."""
    def __init__(self):
        super().__init__(
            status_code=status.HTTP_403_FORBIDDEN,
            detail=INSUFFICIENT_PERMISSIONS
        )