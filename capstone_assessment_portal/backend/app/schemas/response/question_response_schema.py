from datetime import datetime
from pydantic import BaseModel


class QuestionResponseSchema(BaseModel):
    """
    Response schema for a single question.
    """

    id: str                         # MongoDB _id → string
    quiz_id: str                    # which quiz this belongs to
    question_text: str              # the question
    question_type: str              # "mcq" or "true_false"
    options: list[str]              # list of answer choices
    correct_answer: str             # the right answer
    difficulty: str                 # "easy", "medium", "hard"
    tags: list[str]                 # topic tags
    marks: int                      # marks for correct answer
    is_active: bool                 # soft delete flag
    created_at: datetime            # when created
    updated_at: datetime            # when last updated


class QuestionListResponseSchema(BaseModel):
    """
    Response schema when returning multiple questions.
    """

    total: int
    questions: list[QuestionResponseSchema]