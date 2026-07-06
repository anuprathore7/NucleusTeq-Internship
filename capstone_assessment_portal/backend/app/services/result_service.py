from app.config.logger import get_logger
from app.repository.result_repository import ResultRepository
from app.schemas.response.result_response_schema import (
    ResultResponseSchema,
    ResultSummaryListResponseSchema
)
from app.exceptions.result_exceptions import (
    ResultNotFoundException,
    ResultAttemptNotSubmittedException
)
from app.exceptions.attempt_exceptions import AttemptUnauthorizedException
from app.utils.result_mapper import (
    attempt_to_result_response,
    attempts_to_result_summary_list
)

logger = get_logger(__name__)


class ResultService:
    """
    Contains all business logic for result retrieval.

    Results are NOT created here.
    They are created inside AttemptService when student submits.
    This service only READS result data from submitted attempts.
    """

    def __init__(self):
        self.result_repo = ResultRepository()

    async def get_result_by_attempt(
        self,
        attempt_id: str,
        student_id: str
    ) -> ResultResponseSchema:
        """
        Fetch the full result for a specific attempt.
        """
        logger.info(
            f"Get result — attempt: {attempt_id}, student: {student_id}"
        )

        # find submitted attempt
        attempt = await self.result_repo.find_submitted_by_attempt_id(
            attempt_id,
            student_id
        )

        # handle not found cases
        if not attempt:
            logger.warning(
                f"Result not found — attempt: {attempt_id}, "
                f"student: {student_id}"
            )
            raise ResultNotFoundException()

        logger.info(
            f"Result found — attempt: {attempt_id}, "
            f"score: {attempt['score']}/{attempt['total_marks']}"
        )

        # return full result with breakdown
        result = attempt_to_result_response(attempt)
        return result

    async def get_my_results(
        self,
        student_id: str
    ) -> ResultSummaryListResponseSchema:
        """
        Fetch all results for the current student.
        Returns summary list — no breakdown, just scores.

        Used for student result history page where student
        can see all their past quiz performances at a glance.
        """
        logger.info(f"Get my results — student: {student_id}")

        attempts = await self.result_repo.find_all_submitted_by_student(
            student_id
        )
        logger.info(
            f"Returned {len(attempts)} results for student: {student_id}"
        )

        result = attempts_to_result_summary_list(attempts)
        return result

    async def get_all_results_admin(
        self
    ) -> ResultSummaryListResponseSchema:
        """
        Fetch all results across all students.
        Admin only — used for the result dashboard.

        Returns summary list — admin gets overview of
        all quiz performances across all students.
        """
        logger.info("Admin fetching all results")

        attempts = await self.result_repo.find_all_submitted()
        logger.info(f"Returned {len(attempts)} total results for admin")

        result = attempts_to_result_summary_list(attempts)
        return result

    async def get_results_by_quiz_admin(
        self,
        quiz_id: str
    ) -> ResultSummaryListResponseSchema:
        """
        Fetch all results for a specific quiz.
        Admin only — used to see how all students performed
        on a particular quiz.

        Returns summary list sorted by submitted_at descending.
        """
        logger.info(f"Admin fetching results for quiz: {quiz_id}")

        attempts = await self.result_repo.find_all_submitted_by_quiz(quiz_id)
        logger.info(
            f"Returned {len(attempts)} results for quiz: {quiz_id}"
        )

        result = attempts_to_result_summary_list(attempts)
        return result