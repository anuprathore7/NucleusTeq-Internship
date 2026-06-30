from datetime import datetime
from pydantic import BaseModel


class QuestionResponseSchema(BaseModel):
    """
    Full response schema — ADMIN only. Includes correct_answer.
    """

    id: str
    quiz_id: str
    question_text: str
    question_type: str
    options: list[str]
    correct_answer: str
    difficulty: str
    tags: list[str]
    marks: int
    is_active: bool
    created_at: datetime
    updated_at: datetime


class QuestionListResponseSchema(BaseModel):
    """
    List response wrapper — ADMIN only.
    """
    total: int
    questions: list[QuestionResponseSchema]


class QuestionStudentResponseSchema(BaseModel):
    """
    Single question response — STUDENT only.
    correct_answer is deliberately not a field here,
    so it can never be included in the response.
    """

    id: str
    quiz_id: str
    question_text: str
    question_type: str
    options: list[str]
    difficulty: str
    tags: list[str]
    marks: int


class QuestionStudentListResponseSchema(BaseModel):
    """
    List response wrapper — STUDENT only.
    Used when returning multiple questions (by quiz, or by quiz+difficulty)
    with correct_answer hidden from every question in the list.
    """
    total: int
    questions: list[QuestionStudentResponseSchema]