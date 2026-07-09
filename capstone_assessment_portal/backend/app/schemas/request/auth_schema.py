import re
from typing import Literal

from pydantic import BaseModel
from pydantic import EmailStr
from pydantic import Field
from pydantic import field_validator


COMMON_PASSWORDS = {
    "password",
    "password123",
    "admin123",
    "qwerty",
    "qwerty123",
    "welcome",
    "letmein",
    "12345678",
    "123456789",
    "abcdefg",
}


class RegisterUserSchema(BaseModel):
    """
    Schema used when a new user registers.

    FastAPI validates incoming request data
    before it reaches the service layer.
    """

    username: str = Field(
        ...,
        min_length=3,
        max_length=50,
        description="Unique username for the account",
        example="anup_rathore"
    )

    email: EmailStr = Field(
        ...,
        description="User email address",
        example="anup@gmail.com"
    )

    password: str = Field(
        ...,
        description="User account password",
        example="Anup@123"
    )

    @field_validator("username")
    @classmethod
    def validate_username(cls, username: str) -> str:
        username = username.strip()

        if not re.fullmatch(r"[A-Za-z][A-Za-z0-9_]*", username):
            raise ValueError(
                "Username must start with a letter and contain only letters, numbers and underscores."
            )

        if username.isdigit():
            raise ValueError("Username cannot contain only numbers.")

        if "__" in username:
            raise ValueError("Username cannot contain consecutive underscores.")

        if len(set(username.lower())) == 1:
            raise ValueError("Username cannot contain repeated characters only.")

        reserved = {
            "admin",
            "administrator",
            "root",
            "test",
            "user",
            "guest",
        }

        if username.lower() in reserved:
            raise ValueError("This username is not allowed.")

        return username

    @field_validator("email")
    @classmethod
    def validate_email(cls, email: EmailStr) -> EmailStr:
        email = email.strip().lower()

        local_part = email.split("@")[0]

        if local_part.isdigit():
            raise ValueError("Email username cannot contain only numbers.")

        if len(set(local_part)) == 1:
            raise ValueError("Email username cannot contain repeated characters only.")

        if len(email) > 254:
            raise ValueError("Email address is too long.")

        return email

    @field_validator("password")
    @classmethod
    def validate_password(cls, password: str) -> str:

        if " " in password:
            raise ValueError("Password cannot contain spaces.")

        if not re.search(r"[A-Z]", password):
            raise ValueError("Password must contain at least one uppercase letter.")

        if not re.search(r"[a-z]", password):
            raise ValueError("Password must contain at least one lowercase letter.")

        if not re.search(r"\d", password):
            raise ValueError("Password must contain at least one digit.")

        if not re.search(r"[!@#$%^&*()_+\-=\[\]{};':\"\\|,.<>/?]", password):
            raise ValueError("Password must contain at least one special character.")

        if password.lower() in COMMON_PASSWORDS:
            raise ValueError("Password is too common.")

        if re.fullmatch(r"(.)\1+", password):
            raise ValueError("Password cannot contain repeated characters only.")

        sequences = [
            "123456789",
            "987654321",
            "abcdefghijklmnopqrstuvwxyz",
            "qwerty",
        ]

        lower_password = password.lower()

        for sequence in sequences:
            if lower_password in sequence:
                raise ValueError("Password is too predictable.")

        return password


class LoginSchema(BaseModel):
    """
    Schema used during user login.
    """

    email: EmailStr = Field(
        ...,
        example="anup@gmail.com"
    )

    password: str = Field(
        ...,
        example="Anup@123"
    )


class RefreshTokenSchema(BaseModel):
    """
    Request body for the refresh token endpoint.
    Client sends the refresh token to get a new access token.
    """

    refresh_token: str = Field(
        ...,
        description="The refresh token received during login",
        example="eyJhbGciOiJIUzI1NiJ9..."
    )