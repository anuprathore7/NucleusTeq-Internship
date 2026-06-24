from typing import Literal

from pydantic import BaseModel
from pydantic import EmailStr
from pydantic import Field

from app.constants.roles import (
    ADMIN_ROLE,
    STUDENT_ROLE
)


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
        min_length=8,
        max_length=72,
        description="User account password",
        example="Anup@123"
    )

    role: Literal[
        ADMIN_ROLE,
        STUDENT_ROLE
    ] = Field(
        ...,
        description="Role assigned to the user"
    )


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