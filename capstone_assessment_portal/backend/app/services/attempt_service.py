

from datetime import datetime, timezone, timedelta

from app.config.logger import get_logger
from app.repository.attempt_repository import AttemptRepository
from app.repository.quiz_repository import QuizRepository
from app.repository.question_repository import QuestionRepository
from app.schemas.request.attempt_schema import (
    StartAttemptSchema,
    SaveAnswerSchema,
    SubmitAttemptSchema
)
from app.schemas.response.attempt_response_schema import (
    AttemptResponseSchema,
    AttemptListResponseSchema
)
from app.schemas.response.message_response_schema import MessageResponseSchema
from app.exceptions.attempt_exceptions import (
    AttemptNotFoundException,
    AttemptQuizNotFoundException,
    AttemptAlreadySubmittedException,
    AttemptUnauthorizedException,
    AttemptInvalidQuestionException,
    AttemptMaxReachedException
)
from app.utils.attempt_mapper import (
    attempt_to_response,
    attempts_to_response
)
from app.constants.attempt_constants import (
    ATTEMPT_STATUS_IN_PROGRESS,
    ATTEMPT_STATUS_SUBMITTED,
    MAX_ATTEMPTS_PER_QUIZ
)
from app.constants.message import (
    ATTEMPT_SUBMITTED,
    ATTEMPT_ANSWER_SAVED,
    ATTEMPT_AUTO_SUBMITTED
)

logger = get_logger(__name__)


class AttemptService:
    """
    Contains all business logic for quiz attempt operations.

    Uses three repositories:
    - AttemptRepository  → attempt DB operations
    - QuizRepository     → fetch quiz for snapshot
    - QuestionRepository → fetch questions for snapshot
    """

    def __init__(self):
        self.attempt_repo = AttemptRepository()
        self.quiz_repo = QuizRepository()
        self.question_repo = QuestionRepository()

    def _is_time_expired(self, attempt: dict) -> bool:
        """
        Check whether an attempt has exceeded its time limit.

        MongoDB may return older datetime values without timezone information.
        If tzinfo is missing, assume the stored time is UTC.
        """

        started_at = attempt["started_at"]

        # Handle older records that don't contain timezone information
        if started_at.tzinfo is None:
            started_at = started_at.replace(tzinfo=timezone.utc)

        time_limit = attempt["snapshot"]["time_limit"]

        deadline = started_at + timedelta(minutes=time_limit)

        now = datetime.now(timezone.utc)

        logger.info(f"Started At : {started_at}")
        logger.info(f"Deadline   : {deadline}")
        logger.info(f"Current    : {now}")

        return now > deadline

    def _evaluate_answers(self, attempt: dict) -> dict:
        """
        Evaluate saved answers against snapshot correct_answers.

        Loops through ALL questions in snapshot.
        For each question:
        finds student answer in attempt.answers (or empty if not answered)
        compares with correct_answer from snapshot
        calculates marks
        """
        # build lookup: question_id → selected_answer
        # student may not have answered all questions
        answers_lookup = {
            a["question_id"]: a["selected_answer"]
            for a in attempt.get("answers", [])
        }

        score = 0
        total_marks = 0
        answer_breakdown = []

        # loop through ALL questions in snapshot
        for question in attempt["snapshot"]["questions"]:
            q_id = question["question_id"]
            correct = question["correct_answer"]
            marks_possible = question["marks"]

            # get student answer — empty string if not answered
            selected = answers_lookup.get(q_id, "")

            is_correct = selected == correct
            marks_obtained = marks_possible if is_correct else 0

            score += marks_obtained
            total_marks += marks_possible

            answer_breakdown.append({
                "question_id": q_id,
                "question_text": question["question_text"],
                "selected_answer": selected,
                "correct_answer": correct,
                "is_correct": is_correct,
                "marks_obtained": marks_obtained,
                "marks_possible": marks_possible
            })

        # calculate percentage safely
        percentage = round(
            (score / total_marks * 100), 2
        ) if total_marks > 0 else 0.0

        passed = percentage >= attempt["snapshot"]["pass_percentage"]

        return {
            "score": score,
            "total_marks": total_marks,
            "percentage": percentage,
            "passed": passed,
            "answer_breakdown": answer_breakdown
        }

    async def _auto_submit(self, attempt: dict) -> MessageResponseSchema:
        """
        Auto submit an attempt when time has expired.

        Called when any endpoint detects time has expired.
        Evaluates whatever answers student saved so far.
        Unanswered questions get 0 marks.

        Returns success message so the calling endpoint
        can return it directly to the student.
        """
        attempt_id = str(attempt["_id"])
        logger.info(f"Auto submitting expired attempt: {attempt_id}")

        # evaluate whatever answers are saved
        evaluation = self._evaluate_answers(attempt)

        # build update data
        update_data = {
            "status": ATTEMPT_STATUS_SUBMITTED,
            "score": evaluation["score"],
            "total_marks": evaluation["total_marks"],
            "percentage": evaluation["percentage"],
            "passed": evaluation["passed"],
            "answer_breakdown": evaluation["answer_breakdown"],
            "submitted_at": datetime.now(timezone.utc)
        }

        await self.attempt_repo.submit_attempt(attempt_id, update_data)
        logger.info(
            f"Attempt auto submitted — id: {attempt_id}, "
            f"score: {evaluation['score']}/{evaluation['total_marks']}"
        )

        result = MessageResponseSchema(message=ATTEMPT_AUTO_SUBMITTED)
        return result

    async def start_attempt(
        self,
        data: StartAttemptSchema,
        student_id: str
    ) -> AttemptResponseSchema:
        """
        Start a new quiz attempt for a student.
        """
        logger.info(
            f"Start attempt — student: {student_id}, quiz: {data.quiz_id}"
        )

        # verify quiz exists
        quiz = await self.quiz_repo.find_by_id(data.quiz_id)
        if not quiz:
            logger.warning(
                f"Start attempt failed — quiz not found: {data.quiz_id}"
            )
            raise AttemptQuizNotFoundException()
        # resume an existing in-progress attempt instead of creating a duplicate
        existing = await self.attempt_repo.find_in_progress_attempt(student_id, data.quiz_id)
        if existing:
            if self._is_time_expired(existing):
                logger.info(f"Existing attempt expired — auto submitting: {existing['_id']}")
                await self._auto_submit(existing)
                # falls through below — old attempt is now finished, a fresh one can be created
            else:
                logger.info(f"Resuming existing in-progress attempt: {existing['_id']}")
                result = attempt_to_response(existing)
                return result

        # check max attempts not exceeded
        attempt_count = await self.attempt_repo.count_attempts_by_student_and_quiz(
            student_id,
            data.quiz_id
        )
        if attempt_count >= MAX_ATTEMPTS_PER_QUIZ:
            logger.warning(
                f"Max attempts reached — student: {student_id}, "
                f"quiz: {data.quiz_id}"
            )
            raise AttemptMaxReachedException()

        # fetch all questions for this quiz
        questions = await self.question_repo.find_by_quiz_id(data.quiz_id)

        # build snapshot
        # correct_answer stored in snapshot for scoring
        # but never returned to student in response
        questions_snapshot = [
            {
                "question_id": str(q["_id"]),
                "question_text": q["question_text"],
                "question_type": q["question_type"],
                "options": q["options"],
                "correct_answer": q["correct_answer"],
                "marks": q["marks"],
                "difficulty": q["difficulty"],
                "tags": q["tags"]
            }
            for q in questions
        ]

        # build and save attempt document
        new_attempt = {
            "student_id": student_id,
            "quiz_id": data.quiz_id,
            "status": ATTEMPT_STATUS_IN_PROGRESS,
            "snapshot": {
                "title": quiz["title"],
                "description": quiz["description"],
                "time_limit": quiz["time_limit"],
                "pass_percentage": quiz["pass_percentage"],
                "questions": questions_snapshot
            },
            "answers": [],              # empty at start
            "score": None,
            "total_marks": None,
            "percentage": None,
            "passed": None,
            "answer_breakdown": [],
            "started_at": datetime.now(timezone.utc),
            "submitted_at": None
        }

        saved_attempt = await self.attempt_repo.create(new_attempt)
        logger.info(
            f"Attempt created — id: {str(saved_attempt['_id'])}, "
            f"attempt number: {attempt_count + 1} of {MAX_ATTEMPTS_PER_QUIZ}"
        )

        result = attempt_to_response(saved_attempt)
        return result

    async def get_attempt(
        self,
        attempt_id: str,
        student_id: str
    ) -> AttemptResponseSchema | MessageResponseSchema:
        """
        Fetch an attempt by ID — used when student resumes a quiz.

        Returns questions from snapshot (no correct answers)
        and answers student already saved so frontend can
        show which options are already ticked.
        """
        logger.info(
            f"Get attempt — attempt: {attempt_id}, student: {student_id}"
        )

        # find attempt
        attempt = await self.attempt_repo.find_by_id(attempt_id)
        if not attempt:
            logger.warning(f"Attempt not found: {attempt_id}")
            raise AttemptNotFoundException()

        # verify belongs to this student
        if attempt["student_id"] != student_id:
            logger.warning(
                f"Unauthorized — student {student_id} "
                f"tried to access attempt {attempt_id}"
            )
            raise AttemptUnauthorizedException()

        # check if time expired — auto submit if in_progress
        if attempt["status"] == ATTEMPT_STATUS_IN_PROGRESS:
            if self._is_time_expired(attempt):
                logger.info(
                    f"Time expired on resume — auto submitting: {attempt_id}"
                )
                result = await self._auto_submit(attempt)
                return result

        logger.info(f"Returning attempt: {attempt_id}")

        result = attempt_to_response(attempt)
        return result

    async def save_answer(
        self,
        attempt_id: str,
        data: SaveAnswerSchema,
        student_id: str
    ) -> MessageResponseSchema:
        """
        Save or update a single answer for a question.

        Called automatically by frontend when student
        selects or changes an option — no manual action needed.
        """
        logger.info(
            f"Save answer — attempt: {attempt_id}, "
            f"question: {data.question_id}"
        )

        # find attempt
        attempt = await self.attempt_repo.find_by_id(attempt_id)
        if not attempt:
            logger.warning(f"Attempt not found: {attempt_id}")
            raise AttemptNotFoundException()

        # verify belongs to this student
        if attempt["student_id"] != student_id:
            logger.warning(
                f"Unauthorized save — student {student_id}, "
                f"attempt: {attempt_id}"
            )
            raise AttemptUnauthorizedException()

        # check time expired
        if self._is_time_expired(attempt):
            logger.info(
                f"Time expired on save answer — auto submitting: {attempt_id}"
            )
            result = await self._auto_submit(attempt)
            return result

        # verify attempt is still in_progress
        if attempt["status"] != ATTEMPT_STATUS_IN_PROGRESS:
            logger.warning(
                f"Save failed — attempt already submitted: {attempt_id}"
            )
            raise AttemptAlreadySubmittedException()

        # verify question exists in snapshot
        # student can only answer questions from their snapshot
        snapshot_question_ids = {
            q["question_id"]
            for q in attempt["snapshot"]["questions"]
        }
        if data.question_id not in snapshot_question_ids:
            logger.warning(
                f"Save failed — question {data.question_id} "
                f"not in snapshot for attempt {attempt_id}"
            )
            raise AttemptInvalidQuestionException()

        # save or overwrite answer
        await self.attempt_repo.save_answer(
            attempt_id,
            data.question_id,
            data.selected_answer
        )
        logger.info(
            f"Answer saved — attempt: {attempt_id}, "
            f"question: {data.question_id}"
        )

        # return success message
        result = MessageResponseSchema(message=ATTEMPT_ANSWER_SAVED)
        return result

    async def submit_attempt(
        self,
        attempt_id: str,
        student_id: str
    ) -> MessageResponseSchema:
        """
        Manually submit a quiz attempt.

        Student clicks submit button — no answers in request body.
        All answers were already saved via POST /answer endpoint.
        System evaluates whatever is saved in attempt.answers.
        """
        logger.info(
            f"Submit attempt — attempt: {attempt_id}, student: {student_id}"
        )

        # find attempt
        attempt = await self.attempt_repo.find_by_id(attempt_id)
        if not attempt:
            logger.warning(
                f"Submit failed — attempt not found: {attempt_id}"
            )
            raise AttemptNotFoundException()

        # verify belongs to this student
        if attempt["student_id"] != student_id:
            logger.warning(
                f"Unauthorized submit — student {student_id}, "
                f"attempt: {attempt_id}"
            )
            raise AttemptUnauthorizedException()

        # verify attempt is in_progress
        if attempt["status"] != ATTEMPT_STATUS_IN_PROGRESS:
            logger.warning(
                f"Submit failed — already submitted: {attempt_id}"
            )
            raise AttemptAlreadySubmittedException()

        # check time (auto submit handles expired case same way)
        if self._is_time_expired(attempt):
            logger.info(
                f"Time expired on manual submit — auto submitting: {attempt_id}"
            )
            result = await self._auto_submit(attempt)
            return result

        # evaluate saved answers
        evaluation = self._evaluate_answers(attempt)

        logger.info(
            f"Evaluation done — score: {evaluation['score']}/"
            f"{evaluation['total_marks']}, "
            f"percentage: {evaluation['percentage']}%, "
            f"passed: {evaluation['passed']}"
        )

        # store score and mark as submitted
        update_data = {
            "status": ATTEMPT_STATUS_SUBMITTED,
            "score": evaluation["score"],
            "total_marks": evaluation["total_marks"],
            "percentage": evaluation["percentage"],
            "passed": evaluation["passed"],
            "answer_breakdown": evaluation["answer_breakdown"],
            "submitted_at": datetime.now(timezone.utc)
        }

        await self.attempt_repo.submit_attempt(attempt_id, update_data)
        logger.info(f"Attempt submitted successfully: {attempt_id}")

        # return success message
        result = MessageResponseSchema(message=ATTEMPT_SUBMITTED)
        return result

    async def get_my_attempts(
        self,
        student_id: str
    ) -> AttemptListResponseSchema:
        """
        Fetch all attempts for the current student.
        Used for student attempt history page.
        Sorted by most recent first.
        """
        logger.info(f"Fetching all attempts for student: {student_id}")

        attempts = await self.attempt_repo.find_by_student(student_id)
        logger.info(
            f"Returned {len(attempts)} attempts for student: {student_id}"
        )

        result = attempts_to_response(attempts)
        return result