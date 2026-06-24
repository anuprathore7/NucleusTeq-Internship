"""This is response schema which converts mongo document to json for frontend"""
from app.schemas.response.user_response_schema import UserResponseSchema
def user_to_response(user: dict) -> UserResponseSchema:
    return UserResponseSchema(
        id=str(user["_id"]),
        username=user["username"],
        email=user["email"],
        role=user["role"],
        is_active=user["is_active"],
        created_at=user["created_at"],
    )
    