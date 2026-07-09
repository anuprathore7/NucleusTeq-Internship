from app.schemas.response.result_response_schema import (
    ResultResponseSchema,
    ResultListResponseSchema,
    ResultSummarySchema,
    ResultSummaryListResponseSchema,
    AnswerBreakdownSchema
)


def attempt_to_result_response(attempt: dict) -> ResultResponseSchema:
    breakdown = [
        AnswerBreakdownSchema(
            question_id=item["question_id"],
            question_text=item["question_text"],
            selected_answer=item["selected_answer"],
            correct_answer=item["correct_answer"],
            is_correct=item["is_correct"],
            marks_obtained=item["marks_obtained"],
            marks_possible=item["marks_possible"]
        )
        for item in attempt.get("answer_breakdown", [])
    ]

    result = ResultResponseSchema(
        attempt_id=str(attempt["_id"]),
        student_id=attempt["student_id"],
        quiz_id=attempt["quiz_id"],
        quiz_title=attempt["snapshot"]["title"],
        score=attempt["score"],
        total_marks=attempt["total_marks"],
        percentage=attempt["percentage"],
        passed=attempt["passed"],
        pass_percentage=attempt["snapshot"]["pass_percentage"],
        answer_breakdown=breakdown,
        submitted_at=attempt["submitted_at"]
    )
    return result


def attempt_to_result_summary(attempt: dict) -> ResultSummarySchema:
    result = ResultSummarySchema(
        attempt_id=str(attempt["_id"]),
        student_id=attempt["student_id"],
        quiz_id=attempt["quiz_id"],
        quiz_title=attempt["snapshot"]["title"],
        score=attempt["score"],
        total_marks=attempt["total_marks"],
        percentage=attempt["percentage"],
        passed=attempt["passed"],
        submitted_at=attempt["submitted_at"]
    )
    return result

def attempts_to_result_summary_list(
    attempts: list[dict]
) -> ResultSummaryListResponseSchema:
    """
    Converts a list of submitted attempts into a summary list response.
    """
    result = ResultSummaryListResponseSchema(
        total=len(attempts),
        results=[attempt_to_result_summary(a) for a in attempts]
    )
    return result