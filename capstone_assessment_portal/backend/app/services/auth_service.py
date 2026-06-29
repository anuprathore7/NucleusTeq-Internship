# Service layer = all business logic lives here.
# Routes are thin (just HTTP in/out).

from datetime import datetime, timezone

from app.repository.auth_repository import AuthRepository
from app.schemas.request.auth_schema import RegisterUserSchema, LoginSchema
from app.utils.password_utils import hash_password, verify_password
from app.utils.jwt_utils import create_access_token
from app.exceptions.auth_exceptions import (UserAlreadyExistsException,InvalidCredentialsException)
from app.utils.user_mapper import user_to_response
from app.constants.roles import STUDENT_ROLE

from app.utils.jwt_utils import (
    create_access_token,
    create_refresh_token,       
    decode_refresh_token        
)
from app.exceptions.auth_exceptions import (
    UserAlreadyExistsException,
    InvalidCredentialsException,
    InvalidTokenException      
)


class AuthService:

    def __init__(self):
        # Service owns a repository instance — it never touches DB directly
        self.repo = AuthRepository()

    async def register_user(self, data: RegisterUserSchema) -> dict:
        """
        Register a new user account.

        Check email not already taken
        Check username not already taken
        
        """

        # duplicate email check
        if await self.repo.find_user_by_email(data.email):
            raise UserAlreadyExistsException()

        # duplicate username check
        if await self.repo.find_user_by_username(data.username):
            raise UserAlreadyExistsException()

        # never store plain text passwords
        hashed_pw = hash_password(data.password)

        # build the MongoDB document
        new_user = {
            **data.model_dump(),
            "password": hashed_pw,
            "role": STUDENT_ROLE,
            "is_active": True,
            "created_at": datetime.now(timezone.utc)  # always store UTC time
        }

        # persist to database
        saved_user = await self.repo.create_user(new_user)

        # used a mapper function and return only safe fields without password
        return user_to_response(saved_user)

    async def login_user(self, data: LoginSchema) -> dict:
        """
        Authenticate user and return both access and refresh tokens.

        Access token  → short lived (30 min), used for API calls
        Refresh token → long lived (7 days), used to get new access token
        """
        user = await self.repo.find_user_by_email(data.email)
        if not user:
            raise InvalidCredentialsException()

        if not verify_password(data.password, user["password"]):
            raise InvalidCredentialsException()

        token_payload = {
            "sub": str(user["_id"]),
            "role": user["role"],
            "email": user["email"]
        }

        # generate both tokens from same payload
        access_token = create_access_token(token_payload)
        refresh_token = create_refresh_token(token_payload)     

        return {
            "access_token": access_token,
            "refresh_token": refresh_token,                     
            "token_type": "bearer",
            "user": {
                "id": str(user["_id"]),
                "username": user["username"],
                "email": user["email"],
                "role": user["role"]
        }
    }
