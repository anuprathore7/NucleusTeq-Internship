from datetime import datetime
from pydantic import BaseModel


class QuizResponseSchema(BaseModel):
    """
    Response schema for a single quiz.
    """

    id: str                     # MongoDB _id → string
    title: str                  # quiz title
    description: str            # quiz description
    category_id: str            # which category this belongs to
    time_limit: int             # time in minutes
    pass_percentage: float      # minimum % to pass
    is_active: bool             # soft delete flag
    created_at: datetime        # creation timestamp
    updated_at: datetime        # last update timestamp


class QuizListResponseSchema(BaseModel):
    """
    Response schema when returning multiple quizzes.
    """

    total: int                          # how many quizzes in the list
    quizzes: list[QuizResponseSchema]   # the actual quiz objects