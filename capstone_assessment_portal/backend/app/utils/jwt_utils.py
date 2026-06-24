import os
from datetime import datetime, timedelta, timezone

from dotenv import load_dotenv

# python-jose is our JWT library
# JWT = JSON Web Token — a signed string the server issues after login
# The client sends it back on every request to prove who they are
from jose import jwt, JWTError, ExpiredSignatureError

from app.exceptions.auth_exceptions import (
    TokenExpiredException,
    InvalidTokenException
)

load_dotenv()

JWT_SECRET_KEY = os.getenv("JWT_SECRET_KEY", "fallback-secret-change-this")
JWT_ALGORITHM = os.getenv("JWT_ALGORITHM", "HS256")
ACCESS_TOKEN_EXPIRE_MINUTES = int(os.getenv("ACCESS_TOKEN_EXPIRE_MINUTES", "30"))


def create_access_token(data: dict) -> str:
    """
    Build and sign a JWT token.
    """
    payload = data.copy()

    # Set expiry time — always use UTC
    expire = datetime.now(timezone.utc) + timedelta(minutes=ACCESS_TOKEN_EXPIRE_MINUTES)
    payload.update({"exp": expire})

    # Sign the token — returns a string
    token = jwt.encode(payload, JWT_SECRET_KEY, algorithm=JWT_ALGORITHM)
    return token


def decode_access_token(token: str) -> dict:
    """
    Verify and decode a JWT token.
    """
    try:
        payload = jwt.decode(
            token,
            JWT_SECRET_KEY,
            algorithms=[JWT_ALGORITHM]  # list, not string
        )
        return payload

    except ExpiredSignatureError:
        raise TokenExpiredException()

    except JWTError:
        raise InvalidTokenException()