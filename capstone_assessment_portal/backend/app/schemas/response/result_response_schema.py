from datetime import datetime
from pydantic import BaseModel


class AnswerBreakdownSchema(BaseModel):
    """
    Result for a single question.

    During attempt — correct_answer is never shown.
    After submission — correct_answer is shown for learning.
    """

    question_id: str
    question_text: str
    selected_answer: str        # what student chose
    correct_answer: str         # what was right (revealed after submit)
    is_correct: bool            # True if selected == correct
    marks_obtained: int         # marks student got for this question
    marks_possible: int         # total marks for this question


class ResultResponseSchema(BaseModel):
    """
    Complete result for one attempt.
    """

    attempt_id: str
    student_id: str
    username: str
    quiz_id: str
    quiz_title: str
    score: int
    total_marks: int
    percentage: float
    passed: bool
    pass_percentage: float
    answer_breakdown: list[AnswerBreakdownSchema]
    submitted_at: datetime


class ResultListResponseSchema(BaseModel):
    """
    Response when returning multiple results.
    Used for student history and admin dashboard.
    """

    total: int
    results: list[ResultResponseSchema]


class ResultSummarySchema(BaseModel):
    """
    Lightweight result summary — no answer breakdown.
    Used in list views where full breakdown is not needed.
    Student sees all their attempts at a glance.
    Admin sees all results at a glance.
    """

    attempt_id: str
    student_id: str
    username: str
    quiz_id: str
    quiz_title: str
    score: int
    total_marks: int
    percentage: float
    passed: bool
    submitted_at: datetime


class ResultSummaryListResponseSchema(BaseModel):
    """
    Response when returning multiple result summaries.
    Lighter than full ResultListResponseSchema.
    No answer_breakdown included — just scores.
    """

    total: int
    results: list[ResultSummarySchema]