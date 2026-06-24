from fastapi import HTTPException, status

"""
We define all auth-related HTTP errors in one place.
This way, if we want to change an error message,
we change it here and it updates everywhere automatically.
"""


class UserAlreadyExistsException(HTTPException):
    """Raised when email or username is already registered."""
    def __init__(self):
        super().__init__(
            # 409 Conflict = resource already exists
            status_code=status.HTTP_409_CONFLICT,
            detail="User with this email or username already exists"
        )


class InvalidCredentialsException(HTTPException):
    """Raised when email or password is wrong during login."""
    def __init__(self):
        super().__init__(
            # 401 = not authenticated
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid email or password",
            # HTTP spec requires this header when returning 401
            headers={"WWW-Authenticate": "Bearer"}
        )


class UserNotFoundException(HTTPException):
    """Raised when a user lookup by ID returns nothing."""
    def __init__(self):
        super().__init__(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="User not found"
        )


class TokenExpiredException(HTTPException):
    """Raised when a JWT token is past its expiry time."""
    def __init__(self):
        super().__init__(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Token has expired, please login again",
            headers={"WWW-Authenticate": "Bearer"}
        )


class InvalidTokenException(HTTPException):
    """Raised when a JWT token is malformed or signature is wrong."""
    def __init__(self):
        super().__init__(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Could not validate token",
            headers={"WWW-Authenticate": "Bearer"}
        )


class InsufficientPermissionsException(HTTPException):
    """Raised when a user is logged in but not allowed to do this action."""
    def __init__(self):
        super().__init__(
            # 403 Forbidden = authenticated but not authorized
            status_code=status.HTTP_403_FORBIDDEN,
            detail="You do not have permission to perform this action"
        )