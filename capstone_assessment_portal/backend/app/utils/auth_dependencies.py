from fastapi import Depends
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials

from app.utils.jwt_utils import decode_access_token
from app.repository.auth_repository import AuthRepository
from app.exceptions.auth_exceptions import (
    InvalidTokenException,
    InsufficientPermissionsException
)
from app.constants.roles import STUDENT_ROLE
# ADMIN_ROLE is removed from roles.py so we hardcode the string here
# Admin role is internal only — not part of public constants anymore
ADMIN_ROLE = "admin"

http_bearer = HTTPBearer(auto_error=True)


async def get_current_user(
    credentials: HTTPAuthorizationCredentials = Depends(http_bearer)
) -> dict:
    """
    Runs before any protected route.
    Extracts JWT from Authorization header, verifies it,
    and returns the current logged in user as a dict.
    """
    token = credentials.credentials
    payload = decode_access_token(token)

    user_id = payload.get("sub")
    if not user_id:
        raise InvalidTokenException()

    repo = AuthRepository()
    user = await repo.find_user_by_id(user_id)

    if not user:
        raise InvalidTokenException()

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
    """Only allows ADMIN users. Returns 403 for everyone else."""
    if current_user["role"] != ADMIN_ROLE:
        raise InsufficientPermissionsException()
    return current_user


async def require_student(
    current_user: dict = Depends(get_current_user)
) -> dict:
    """Only allows STUDENT users. Returns 403 for everyone else."""
    if current_user["role"] != STUDENT_ROLE:
        raise InsufficientPermissionsException()
    return current_user