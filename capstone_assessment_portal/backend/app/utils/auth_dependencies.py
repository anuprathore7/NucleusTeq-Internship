from fastapi import Depends, HTTPException, status
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
# HTTPBearer = reads the Authorization: Bearer <token> header
# HTTPAuthorizationCredentials = the parsed result (scheme + token)
# This gives us the clean "Authorize" popup in Swagger that asks for a token string

from app.utils.jwt_utils import decode_access_token
from app.repository.auth_repository import AuthRepository
from app.exceptions.auth_exceptions import (
    InvalidTokenException,
    InsufficientPermissionsException
)
from app.constants.roles import ADMIN_ROLE, STUDENT_ROLE

# HTTPBearer tells FastAPI:
# "This route needs an Authorization: Bearer <token> header"
# auto_error=True means FastAPI returns 403 automatically if header is missing
http_bearer = HTTPBearer(auto_error=True)


async def get_current_user(
    credentials: HTTPAuthorizationCredentials = Depends(http_bearer)
) -> dict:
    """
    Runs before any protected route.

    credentials.scheme      = "Bearer"
    credentials.credentials = the actual token string

    FastAPI extracts these from the header:
        Authorization: Bearer eyJhbGci...
    """

    # Extract just the token string from the credentials object
    token = credentials.credentials

    # Decode and verify the JWT — raises 401 if expired or invalid
    payload = decode_access_token(token)

    # "sub" is the user ID we stored inside the token during login
    user_id = payload.get("sub")
    if not user_id:
        raise InvalidTokenException()

    # Confirm user still exists in DB
    # (handles case: token valid but account was deleted)
    repo = AuthRepository()
    user = await repo.find_user_by_id(user_id)

    if not user:
        raise InvalidTokenException()

    # Return clean user dict — injected into the route as current_user
    return {
        "id": str(user["_id"]),
        "username": user["username"],
        "email": user["email"],
        "role": user["role"],
        "is_active": user["is_active"]
    }


async def require_admin(
    current_user: dict = Depends(get_current_user)
) -> dict:
    """
    Only lets ADMIN users through.
    Student hitting an admin route gets 403 Forbidden.
    """
    if current_user["role"] != ADMIN_ROLE:
        raise InsufficientPermissionsException()
    return current_user


async def require_student(
    current_user: dict = Depends(get_current_user)
) -> dict:
    """
    Only lets STUDENT users through.
    """
    if current_user["role"] != STUDENT_ROLE:
        raise InsufficientPermissionsException()
    return current_user