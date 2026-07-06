
from pydantic import BaseModel, Field


class StartAttemptSchema(BaseModel):
    """
    Request body when student starts a quiz attempt.
    Student only needs to send which quiz they want to attempt.
    """
    quiz_id: str = Field(
        ...,
        description="MongoDB ID of the quiz to attempt",
        example="64abc123def456789012abcd"
    )


class SaveAnswerSchema(BaseModel):
    """
    Request body when student saves a single answer.

    Frontend calls this automatically every time
    student selects an option — no manual action needed.

    If student changes their answer for same question,
    this overwrites the previous answer.
    Only one answer stored per question at any time.
    """
    question_id: str = Field(
        ...,
        description="MongoDB ID of the question being answered",
        example="64q1abc123def456789012"
    )

    selected_answer: str = Field(
        ...,
        description="The answer student selected from options",
        example="A mutable sequence"
    )


class SubmitAttemptSchema(BaseModel):
    """
    Request body when student manually submits quiz.

    Student clicks submit button.
    No answers needed in body — all answers
    were already saved via POST /answer endpoint.
    System evaluates whatever is saved in attempt.answers.
    """
    pass