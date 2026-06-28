from pydantic import BaseModel, Field


class CreateQuizSchema(BaseModel):
    """
    Request body schema for creating a new quiz.

    Admin sends this when creating a quiz.
    Pydantic validates every field automatically
    before our code even runs.
    """

    title: str = Field(
        ...,                        # required — must be provided
        min_length=3,
        max_length=200,
        description="Title of the quiz",
        example="Python Basics Test"
    )

    description: str = Field(
        ...,
        min_length=5,
        max_length=1000,
        description="What this quiz covers",
        example="Test covering Python fundamentals like variables, loops and functions"
    )

    category_id: str = Field(
        ...,
        description="MongoDB ID of the category this quiz belongs to",
        example="64abc123def456789012"
    )

    time_limit: int = Field(
        ...,
        gt=0,           # gt = greater than — must be more than 0 minutes
        le=300,         # le = less than or equal — max 300 minutes (5 hours)
        description="Time limit for the quiz in minutes",
        example=30
    )

    pass_percentage: float = Field(
        ...,
        gt=0,           # must be more than 0%
        le=100,         # cannot exceed 100%
        description="Minimum percentage required to pass the quiz",
        example=60.0
    )


class UpdateQuizSchema(BaseModel):
    """
    Request body schema for updating an existing quiz.

    All fields are optional — admin can update just the title,
    just the time limit, or any combination of fields.
    Fields not provided will NOT be changed in the database.
    """

    title: str | None = Field(
        default=None,
        min_length=3,
        max_length=200,
        description="Updated title of the quiz",
        example="Python Intermediate Test"
    )

    description: str | None = Field(
        default=None,
        min_length=5,
        max_length=1000,
        description="Updated description",
        example="Updated description for the quiz"
    )

    category_id: str | None = Field(
        default=None,
        description="Updated category ID if moving quiz to different category",
        example="64abc123def456789012"
    )

    time_limit: int | None = Field(
        default=None,
        gt=0,
        le=300,
        description="Updated time limit in minutes",
        example=45
    )

    pass_percentage: float | None = Field(
        default=None,
        gt=0,
        le=100,
        description="Updated pass percentage",
        example=70.0
    )