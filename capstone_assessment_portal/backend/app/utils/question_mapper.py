from app.schemas.response.question_response_schema import (
    QuestionResponseSchema,
    QuestionListResponseSchema
)


def question_to_response(question: dict) -> QuestionResponseSchema:
    """
    Converts a raw MongoDB question document into a clean response schema.
    """
    return QuestionResponseSchema(
        id=str(question["_id"]),            # ObjectId → string
        quiz_id=question["quiz_id"],
        question_text=question["question_text"],
        question_type=question["question_type"],
        options=question["options"],
        correct_answer=question["correct_answer"],
        difficulty=question["difficulty"],
        tags=question["tags"],
        marks=question["marks"],
        is_active=question["is_active"],
        created_at=question["created_at"],
        updated_at=question["updated_at"]
    )


def questions_to_response(questions: list[dict]) -> QuestionListResponseSchema:
    """
    Converts a list of MongoDB question documents into a list response.
    """
    return QuestionListResponseSchema(
        total=len(questions),
        questions=[question_to_response(q) for q in questions]
    )