from app.schemas.response.question_response_schema import (
    QuestionResponseSchema,
    QuestionListResponseSchema,
    QuestionStudentResponseSchema,
    QuestionStudentListResponseSchema
)


def question_to_response(question: dict) -> QuestionResponseSchema:
    """Admin mapper — includes correct_answer."""
    return QuestionResponseSchema(
        id=str(question["_id"]),
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
    """Admin list mapper — includes correct_answer for each question."""
    return QuestionListResponseSchema(
        total=len(questions),
        questions=[question_to_response(q) for q in questions]
    )


def question_to_student_response(question: dict) -> QuestionStudentResponseSchema:
    """
    Student mapper for a single question.
    Never reads question["correct_answer"] — that's what makes it safe.
    """
    return QuestionStudentResponseSchema(
        id=str(question["_id"]),
        quiz_id=question["quiz_id"],
        question_text=question["question_text"],
        question_type=question["question_type"],
        options=question["options"],
        difficulty=question["difficulty"],
        tags=question["tags"],
        marks=question["marks"]
    )


def questions_to_student_response(
    questions: list[dict]
) -> QuestionStudentListResponseSchema:
    """
    Student mapper for a list of questions.
    Every question in the list has correct_answer stripped out.
    """
    return QuestionStudentListResponseSchema(
        total=len(questions),
        questions=[question_to_student_response(q) for q in questions]
    )