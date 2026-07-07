from app.schemas.response.attempt_response_schema import (
    AttemptResponseSchema,
    AttemptListResponseSchema,
    QuestionInAttemptSchema,
    AnswerInAttemptSchema
)


def attempt_to_response(attempt: dict) -> AttemptResponseSchema:
    """
    Converts a raw MongoDB attempt document into a clean response.

    Maps snapshot questions into QuestionInAttemptSchema
    which deliberately excludes correct_answer.

    Maps saved answers into AnswerInAttemptSchema
    so student can see what they already answered on resume.
    """
    questions = [
        QuestionInAttemptSchema(
            question_id=q["question_id"],
            question_text=q["question_text"],
            question_type=q["question_type"],
            options=q["options"],
            marks=q["marks"],
            difficulty=q["difficulty"],
            tags=q["tags"]
            # correct_answer deliberately excluded
        )
        for q in attempt["snapshot"]["questions"]
    ]

    answers = [
        AnswerInAttemptSchema(
            question_id=a["question_id"],
            selected_answer=a["selected_answer"]
        )
        for a in attempt.get("answers", [])
    ]

    result = AttemptResponseSchema(
        id=str(attempt["_id"]),
        student_id=attempt["student_id"],
        quiz_id=attempt["quiz_id"],
        status=attempt["status"],
        time_limit=attempt["snapshot"]["time_limit"],
        started_at=attempt["started_at"],
        submitted_at=attempt.get("submitted_at"),
        questions=questions,
        answers=answers
    )
    return result


def attempts_to_response(attempts: list[dict]) -> AttemptListResponseSchema:
    """
    Converts a list of attempt documents into a list response.
    """
    result = AttemptListResponseSchema(
        total=len(attempts),
        attempts=[attempt_to_response(a) for a in attempts]
    )
    return result