from datetime import datetime, timezone

from app.config.logger import get_logger
from app.repository.auth_repository import AuthRepository
from app.schemas.request.auth_schema import RegisterUserSchema, LoginSchema
from app.utils.password_utils import hash_password, verify_password
from app.utils.jwt_utils import (
    create_access_token,
    create_refresh_token,
    decode_refresh_token
)
from app.utils.user_mapper import user_to_response
from app.constants.roles import STUDENT_ROLE
from app.exceptions.auth_exceptions import (
    UserAlreadyExistsException,
    InvalidCredentialsException,
    InvalidTokenException,
    UserNotFoundException
)
from app.utils.rsa_utils import PUBLIC_KEY_PATH, decrypt_password

logger = get_logger(__name__)


class AuthService:
    """
    Contains all business logic for authentication operations.
    """

    def __init__(self):
        self.repo = AuthRepository()

    async def register_user(self, data: RegisterUserSchema) -> dict:
        """
        Register a new user account as a student.
        Checks for duplicate email and username before saving.
        """
        logger.info(f"Registration attempt for email: {data.email}")

        if await self.repo.find_user_by_email(data.email):
            logger.warning(f"Registration failed — email already exists: {data.email}")
            raise UserAlreadyExistsException()

        if await self.repo.find_user_by_username(data.username):
            logger.warning(f"Registration failed — username already exists: {data.username}")
            raise UserAlreadyExistsException()

        plain_password = decrypt_password(data.password)

        hashed_pw = hash_password(plain_password)

        new_user = {
            **data.model_dump(),
            "password": hashed_pw,
            "role": STUDENT_ROLE,
            "is_active": True,
            "created_at": datetime.now(timezone.utc)
        }

        saved_user = await self.repo.create_user(new_user)
        logger.info(f"User registered successfully with email: {data.email}")

        result = user_to_response(saved_user)
        return result

    async def login_user(self, data: LoginSchema) -> dict:
        """
        Authenticate user and return both access and refresh tokens.
        """
        logger.info(f"Login attempt for email: {data.email}")

        user = await self.repo.find_user_by_email(data.email)
        if not user:
            logger.warning(f"Login failed — email not found: {data.email}")
            raise UserNotFoundException()
        
        plain_password = decrypt_password(data.password)
        
        if not verify_password(plain_password, user["password"]):
            logger.warning(f"Login failed — invalid password for email: {data.email}")
            raise InvalidCredentialsException()

        token_payload = {
            "sub": str(user["_id"]),
            "role": user["role"],
            "email": user["email"]
        }

        access_token = create_access_token(token_payload)
        refresh_token = create_refresh_token(token_payload)

        logger.info(f"Login successful for email: {data.email} | role: {user['role']}")

        result = {
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
        return result

    async def refresh_access_token(self, refresh_token: str) -> dict:
        """
        Generate a new access token using a valid refresh token.
        """
        logger.info("Refresh token request received")

        payload = decode_refresh_token(refresh_token)

        user_id = payload.get("sub")
        if not user_id:
            logger.warning("Refresh failed — sub missing from token payload")
            raise InvalidTokenException()

        user = await self.repo.find_user_by_id(user_id)
        if not user:
            logger.warning(f"Refresh failed — user not found for id: {user_id}")
            raise InvalidTokenException()

        token_payload = {
            "sub": str(user["_id"]),
            "role": user["role"],
            "email": user["email"]
        }

        new_access_token = create_access_token(token_payload)
        logger.info(f"Access token refreshed for email: {user['email']}")

        result = {
            "access_token": new_access_token,
            "token_type": "bearer"
        }
        return result
    
    async def get_public_key(self) -> dict:
        """
        Return public key in PEM format.
        """

        with open(PUBLIC_KEY_PATH, "r") as file:
            public_key = file.read()
        response  = {"public_Key": public_key}

        return response