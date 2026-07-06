from datetime import datetime
from pydantic import BaseModel


class QuestionInAttemptSchema(BaseModel):
    """
    A single question shown to student during attempt.
    correct_answer is deliberately NOT included.
    Student never sees the answer while attempting.
    """
    question_id: str
    question_text: str
    question_type: str
    options: list[str]
    marks: int
    difficulty: str
    tags: list[str]


class AnswerInAttemptSchema(BaseModel):
    """
    A single saved answer inside an attempt.

    Returned when student resumes so frontend
    can tick the options student already selected.
    selected_answer = what student picked, not correct answer.
    """
    question_id: str
    selected_answer: str


class AttemptResponseSchema(BaseModel):
    """
    Response when student starts or resumes an attempt.

    questions → all questions from snapshot (no correct_answer)
    answers   → what student already saved so far
                empty [] at start
                fills up as student answers questions
    """
    id: str
    student_id: str
    quiz_id: str
    status: str
    time_limit: int             # in minutes — frontend uses this for countdown
    started_at: datetime
    submitted_at: datetime | None
    questions: list[QuestionInAttemptSchema]
    answers: list[AnswerInAttemptSchema]


class AttemptListResponseSchema(BaseModel):
    """
    Response when returning multiple attempts.
    Used for student attempt history.
    """
    total: int
    attempts: list[AttemptResponseSchema]