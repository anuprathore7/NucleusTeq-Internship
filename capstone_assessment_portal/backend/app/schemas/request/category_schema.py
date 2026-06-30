from pydantic import BaseModel, Field


class CreateCategorySchema(BaseModel):
    """
    Request body schema for creating a new category.
    Admin sends this when creating a category like 'Python', 'DSA' etc.
    Pydantic validates this automatically before our code runs.
    """

    name: str = Field(
        ...,                        # required field
        min_length=2,
        max_length=100,
        description="Name of the category",
        example="Python Programming"
    )

    description: str = Field(
        ...,
        min_length=5,
        max_length=500,
        description="Short description of what this category covers",
        example="Quizzes related to Python programming language"
    )


class UpdateCategorySchema(BaseModel):
    """
    Request body schema for updating an existing category.
    Both fields are optional — admin can update just name,
    just description, or both at the same time.
    """

    name: str | None = Field(
        default=None,               # optional — not required
        min_length=2,
        max_length=100,
        description="Updated name of the category",
        example="Advanced Python"
    )

    description: str | None = Field(
        default=None,
        min_length=5,
        max_length=500,
        description="Updated description of the category",
        example="Advanced Python programming quizzes"
    )