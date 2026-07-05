from pydantic import BaseModel


class MessageResponseSchema(BaseModel):
    """
    Standard response schema for operations that return
    a success message instead of a data object.

    Consistent response shape across all delete endpoints
    """

    message: str