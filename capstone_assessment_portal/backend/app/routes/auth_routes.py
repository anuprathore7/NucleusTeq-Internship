# Routes = HTTP layer only.
# Each function here maps one URL + HTTP method to one service call.

from fastapi import APIRouter, Depends, status

from app.schemas.request.auth_schema import RegisterUserSchema, LoginSchema
from app.services.auth_service import AuthService
from app.utils.auth_dependencies import get_current_user
from app.constants.url_prefix import AUTH_PREFIX
from app.schemas.request.auth_schema import (
    RegisterUserSchema,
    LoginSchema,
    RefreshTokenSchema      # ADD THIS
)

router = APIRouter(
    prefix=AUTH_PREFIX,   # every route here starts with /auth
    tags=["Authentication"]  # groups these routes in Swagger UI
)

# one shared service instance for this router
auth_service = AuthService()


@router.post(
    "/register",
    status_code=status.HTTP_201_CREATED,  # 201 = something was created (not 200)
    summary="Register a new user"
)
async def register(user_data: RegisterUserSchema):
    """
    Create a new admin or student account.
    FastAPI validates user_data against RegisterUserSchema automatically.
    If validation fails (e.g. password too short), 422 is returned before
    this function even runs.
    """
    return await auth_service.register_user(user_data)


@router.post(
    "/login",
    status_code=status.HTTP_200_OK,
    summary="Login and receive JWT token"
)
async def login(login_data: LoginSchema):
    """
    Verify credentials and return a JWT access token.
    Include the token in future requests as:
        Authorization: Bearer <token>
    """
    return await auth_service.login_user(login_data)


@router.get(
    "/me",
    status_code=status.HTTP_200_OK, 
    summary="Get my profile"
)
async def get_me(current_user: dict = Depends(get_current_user)):
    """
    Protected route — requires a valid JWT token.
    Depends(get_current_user) runs first and injects the user.
    If token is missing or invalid, FastAPI returns 401 before reaching here.
    """
    return current_user

@router.post(
    "/refresh",
    status_code=status.HTTP_200_OK,
    summary="Refresh access token",
    description="Send a valid refresh token to get a new access token."
)
async def refresh_token(data: RefreshTokenSchema):
    """
    When the access token expires (after 30 minutes),
    the client sends the refresh token here to get a new access token
    without making the user login again.
    """
    return await auth_service.refresh_access_token(data.refresh_token)