from typing import Literal
from pydantic import BaseModel, Field, model_validator

from app.constants.question_constants import (
    MCQ,
    TRUE_FALSE,
    EASY,
    MEDIUM,
    HARD,
    TRUE_FALSE_OPTIONS,
    MCQ_OPTIONS_COUNT
)


class CreateQuestionSchema(BaseModel):
    """
    Request body schema for creating a new question.
    """

    quiz_id: str = Field(
        ...,
        description="MongoDB ID of the quiz this question belongs to",
        example="64abc123def456789012abcd"
    )

    question_text: str = Field(
        ...,
        min_length=5,
        max_length=1000,
        description="The actual question text shown to the student",
        example="What is a list in Python?"
    )

    question_type: Literal[MCQ, TRUE_FALSE] = Field(
        ...,
        description="Type of question — 'mcq' or 'true_false'",
        example="mcq"
    )

    options: list[str] = Field(
        ...,
        description="List of answer choices. MCQ needs 4, True/False needs exactly ['True', 'False']",
        example=["A mutable sequence", "An immutable sequence",
                 "A key-value store", "A set of unique items"]
    )

    correct_answer: str = Field(
        ...,
        description="Must exactly match one of the provided options",
        example="A mutable sequence"
    )

    difficulty: Literal[EASY, MEDIUM, HARD] = Field(
        ...,
        description="Difficulty level of the question",
        example="easy"
    )

    tags: list[str] = Field(
        default=[],             # optional — empty list if not provided
        description="Tags for categorizing the question",
        example=["python", "data-structures"]
    )

    marks: int = Field(
        default=1,              # default 1 mark per question
        gt=0,                   # must be greater than 0
        le=10,                  # maximum 10 marks per question
        description="Marks awarded for correct answer",
        example=1
    )

    @model_validator(mode="after")
    def validate_options_and_answer(self):
        """
        Custom validator that runs AFTER all individual field validations.
        """

        # MCQ must have exactly 4 options
        if self.question_type == MCQ:
            if len(self.options) != MCQ_OPTIONS_COUNT:
                raise ValueError(
                    f"MCQ questions must have exactly {MCQ_OPTIONS_COUNT} options"
                )

        # True/False must have exactly ["True", "False"]
        if self.question_type == TRUE_FALSE:
            if self.options != TRUE_FALSE_OPTIONS:
                raise ValueError(
                    f"True/False questions must have exactly these options: {TRUE_FALSE_OPTIONS}"
                )

        # correct_answer must be one of the provided options
        # strip() removes accidental spaces before comparing
        if self.correct_answer.strip() not in [opt.strip() for opt in self.options]:
            raise ValueError(
                "correct_answer must exactly match one of the provided options"
            )

        # returning self is required by pydantic v2 model_validator
        return self


class UpdateQuestionSchema(BaseModel):
    """
    Request body schema for updating an existing question.

    All fields are optional — admin can update just the text,
    just the difficulty, or any combination.
    """

    question_text: str | None = Field(
        default=None,
        min_length=5,
        max_length=1000,
        description="Updated question text",
        example="What is a tuple in Python?"
    )

    options: list[str] | None = Field(
        default=None,
        description="Updated list of options",
        example=["An immutable sequence", "A mutable sequence",
                 "A key-value store", "A set of unique items"]
    )

    correct_answer: str | None = Field(
        default=None,
        description="Updated correct answer — must match one of the options",
        example="An immutable sequence"
    )

    difficulty: Literal[EASY, MEDIUM, HARD] | None = Field(
        default=None,
        description="Updated difficulty level",
        example="medium"
    )

    tags: list[str] | None = Field(
        default=None,
        description="Updated tags",
        example=["python", "data-structures", "tuples"]
    )

    marks: int | None = Field(
        default=None,
        gt=0,
        le=10,
        description="Updated marks for correct answer",
        example=2
    )