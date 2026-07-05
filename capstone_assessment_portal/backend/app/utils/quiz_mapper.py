from app.schemas.response.quiz_response_schema import (
    QuizResponseSchema,
    QuizListResponseSchema
)


def quiz_to_response(quiz: dict) -> QuizResponseSchema:
    """
    Converts a raw MongoDB quiz document into a clean response schema.
    """
    return QuizResponseSchema(
        id=str(quiz["_id"]),                # ObjectId → string
        title=quiz["title"],
        description=quiz["description"],
        category_id=quiz["category_id"],    # already stored as string
        time_limit=quiz["time_limit"],
        pass_percentage=quiz["pass_percentage"],
        is_active=quiz["is_active"],
        created_at=quiz["created_at"],
        updated_at=quiz["updated_at"]
    )


def quizzes_to_response(quizzes: list[dict]) -> QuizListResponseSchema:
    """
    Converts a list of MongoDB quiz documents into a list response.
    """
    return QuizListResponseSchema(
        total=len(quizzes),
        quizzes=[quiz_to_response(quiz) for quiz in quizzes]
    )