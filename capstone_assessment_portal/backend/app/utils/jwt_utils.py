import os
from datetime import datetime, timedelta, timezone

from dotenv import load_dotenv
from jose import jwt, JWTError, ExpiredSignatureError

from app.exceptions.auth_exceptions import (
    TokenExpiredException,
    InvalidTokenException
)

load_dotenv()

JWT_SECRET_KEY = os.getenv("JWT_SECRET_KEY", "fallback-secret-change-this")
JWT_ALGORITHM = os.getenv("JWT_ALGORITHM", "HS256")
ACCESS_TOKEN_EXPIRE_MINUTES = int(os.getenv("ACCESS_TOKEN_EXPIRE_MINUTES", "30"))

# refresh token lives much longer than access token
# access token  = 30 minutes  (short lived, used for API calls)
# refresh token = 7 days      (long lived, used only to get new access token)
REFRESH_TOKEN_EXPIRE_DAYS = int(os.getenv("REFRESH_TOKEN_EXPIRE_DAYS", "7"))


def create_access_token(data: dict) -> str:
    """
    Create a short-lived JWT access token.

    Used for authenticating API requests.
    Expires in 30 minutes by default.
    Client sends this in Authorization header for every request.
    """
    payload = data.copy()
    expire = datetime.now(timezone.utc) + timedelta(
        minutes=ACCESS_TOKEN_EXPIRE_MINUTES
    )
    payload.update({
        "exp": expire,
        "type": "access"    # mark token type so we can distinguish later
    })
    return jwt.encode(payload, JWT_SECRET_KEY, algorithm=JWT_ALGORITHM)


def create_refresh_token(data: dict) -> str:
    """
    Create a long-lived JWT refresh token.

    Used ONLY to generate a new access token when the old one expires.
    Client stores this securely and sends it to /auth/refresh endpoint.
    Expires in 7 days by default.

    Why two tokens?
    Access token is short lived (30 min) — limits damage if stolen.
    Refresh token is long lived (7 days) — user doesn't have to login
    every 30 minutes. When access token expires, client silently uses
    refresh token to get a new access token without user noticing.
    """
    payload = data.copy()
    expire = datetime.now(timezone.utc) + timedelta(
        days=REFRESH_TOKEN_EXPIRE_DAYS
    )
    payload.update({
        "exp": expire,
        "type": "refresh"   # mark as refresh so it can't be used as access token
    })
    return jwt.encode(payload, JWT_SECRET_KEY, algorithm=JWT_ALGORITHM)


def decode_access_token(token: str) -> dict:
    """
    Decode and verify a JWT access token.
    Raises exception if expired, invalid, or wrong token type.
    """
    try:
        payload = jwt.decode(
            token,
            JWT_SECRET_KEY,
            algorithms=[JWT_ALGORITHM]
        )

        # make sure this is an access token, not a refresh token
        # prevents refresh tokens from being used as access tokens
        if payload.get("type") != "access":
            raise InvalidTokenException()

        return payload

    except ExpiredSignatureError:
        raise TokenExpiredException()

    except JWTError:
        raise InvalidTokenException()


def decode_refresh_token(token: str) -> dict:
    """
    Decode and verify a JWT refresh token.
    Called only on the /auth/refresh endpoint.
    Raises exception if expired, invalid, or wrong token type.
    """
    try:
        payload = jwt.decode(
            token,
            JWT_SECRET_KEY,
            algorithms=[JWT_ALGORITHM]
        )

        # make sure this is a refresh token, not an access token
        if payload.get("type") != "refresh":
            raise InvalidTokenException()

        return payload

    except ExpiredSignatureError:
        raise TokenExpiredException()

    except JWTError:
        raise InvalidTokenException()