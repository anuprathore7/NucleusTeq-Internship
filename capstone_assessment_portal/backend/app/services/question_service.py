from datetime import datetime, timezone

from app.config.logger import get_logger
from app.repository.question_repository import QuestionRepository
from app.repository.quiz_repository import QuizRepository
from app.schemas.request.question_schema import (
    CreateQuestionSchema,
    UpdateQuestionSchema
)
from app.schemas.response.question_response_schema import (
    QuestionResponseSchema,
    QuestionListResponseSchema,
    QuestionStudentResponseSchema,
    QuestionStudentListResponseSchema
)
from app.exceptions.question_exceptions import (
    QuestionNotFoundException,
    QuestionAlreadyExistsException,
    QuestionQuizNotFoundException,
    QuestionInvalidCorrectAnswerException
)
from app.utils.question_mapper import (
    question_to_response,
    questions_to_response,
    question_to_student_response,
    questions_to_student_response
)
from app.constants.question_constants import MCQ_OPTIONS_COUNT
from app.schemas.response.message_response_schema import MessageResponseSchema
from app.constants.message import QUESTION_DELETED

logger = get_logger(__name__)


class QuestionService:
    """
    Contains ALL business logic for question operations.
    Uses QuestionRepository and QuizRepository.
    """

    def __init__(self):
        self.question_repo = QuestionRepository()
        self.quiz_repo = QuizRepository()

    async def create_question(
        self,
        data: CreateQuestionSchema
    ) -> QuestionResponseSchema:
        """
        Add a new question to a quiz.
        Validates quiz exists, no duplicate text, and correct_answer is in options.
        """
        logger.info(f"Create question attempt for quiz: {data.quiz_id}")

        quiz = await self.quiz_repo.find_by_id(data.quiz_id)
        if not quiz:
            logger.warning(f"Create question failed — quiz not found: {data.quiz_id}")
            raise QuestionQuizNotFoundException()

        existing = await self.question_repo.find_by_text_and_quiz(
            data.question_text,
            data.quiz_id
        )
        if existing:
            logger.warning(f"Create question failed — duplicate text in quiz: {data.quiz_id}")
            raise QuestionAlreadyExistsException()

        if data.correct_answer not in data.options:
            logger.warning(f"Create question failed — correct_answer not in options")
            raise QuestionInvalidCorrectAnswerException()

        new_question = {
            **data.model_dump(),
            "is_active": True,
            "created_at": datetime.now(timezone.utc),
            "updated_at": datetime.now(timezone.utc)
        }

        saved_question = await self.question_repo.create(new_question)
        logger.info(f"Question created successfully in quiz: {data.quiz_id}")

        result = question_to_response(saved_question)
        return result

    async def get_questions_by_quiz(
        self,
        quiz_id: str
    ) -> QuestionListResponseSchema:
        """
        Get all active questions for a quiz — admin view with correct_answer.
        """
        logger.info(f"Admin fetching questions for quiz: {quiz_id}")

        quiz = await self.quiz_repo.find_by_id(quiz_id)
        if not quiz:
            logger.warning(f"Get questions failed — quiz not found: {quiz_id}")
            raise QuestionQuizNotFoundException()

        questions = await self.question_repo.find_by_quiz_id(quiz_id)
        logger.info(f"Returned {len(questions)} questions for quiz: {quiz_id}")

        result = questions_to_response(questions)
        return result

    async def get_questions_by_difficulty(
        self,
        quiz_id: str,
        difficulty: str
    ) -> QuestionListResponseSchema:
        """
        Get questions filtered by difficulty — admin view with correct_answer.
        """
        logger.info(f"Admin fetching {difficulty} questions for quiz: {quiz_id}")

        quiz = await self.quiz_repo.find_by_id(quiz_id)
        if not quiz:
            logger.warning(f"Get by difficulty failed — quiz not found: {quiz_id}")
            raise QuestionQuizNotFoundException()

        questions = await self.question_repo.find_by_quiz_and_difficulty(
            quiz_id,
            difficulty
        )
        logger.info(f"Returned {len(questions)} {difficulty} questions for quiz: {quiz_id}")

        result = questions_to_response(questions)
        return result

    async def get_question_by_id(
        self,
        question_id: str
    ) -> QuestionResponseSchema:
        """
        Fetch a single question by ID — admin view with correct_answer.
        """
        logger.info(f"Admin fetching question: {question_id}")

        question = await self.question_repo.find_by_id(question_id)
        if not question:
            logger.warning(f"Question not found: {question_id}")
            raise QuestionNotFoundException()

        logger.info(f"Question found: {question_id}")

        result = question_to_response(question)
        return result

    async def update_question(
        self,
        question_id: str,
        data: UpdateQuestionSchema
    ) -> QuestionResponseSchema:
        """
        Update an existing question.
        Validates correct_answer against final options and checks for duplicate text.
        """
        logger.info(f"Update question attempt for id: {question_id}")

        existing_question = await self.question_repo.find_by_id(question_id)
        if not existing_question:
            logger.warning(f"Update failed — question not found: {question_id}")
            raise QuestionNotFoundException()

        final_options = data.options if data.options is not None \
            else existing_question["options"]

        if data.correct_answer is not None:
            if data.correct_answer not in final_options:
                logger.warning(f"Update failed — correct_answer not in options")
                raise QuestionInvalidCorrectAnswerException()

        if data.question_text:
            duplicate = await self.question_repo.find_by_text_and_quiz(
                data.question_text,
                existing_question["quiz_id"]
            )
            if duplicate and str(duplicate["_id"]) != question_id:
                logger.warning(f"Update failed — duplicate question text in quiz")
                raise QuestionAlreadyExistsException()

        update_data = data.model_dump(exclude_none=True)
        update_data["updated_at"] = datetime.now(timezone.utc)

        updated_question = await self.question_repo.update(question_id, update_data)
        logger.info(f"Question updated successfully: {question_id}")

        result = question_to_response(updated_question)
        return result

    async def delete_question(self, question_id: str) -> MessageResponseSchema:
        """
        Soft delete a question by marking it inactive.
        """
        logger.info(f"Delete question attempt for id: {question_id}")

        existing_question = await self.question_repo.find_by_id(question_id)
        if not existing_question:
            logger.warning(f"Delete failed — question not found: {question_id}")
            raise QuestionNotFoundException()

        await self.question_repo.delete(question_id)
        logger.info(f"Question deleted successfully: {question_id}")

        result = MessageResponseSchema(message=QUESTION_DELETED)
        return result

    async def get_question_count_by_quiz(self, quiz_id: str) -> dict:
        """
        Get total number of active questions in a quiz.
        """
        logger.info(f"Getting question count for quiz: {quiz_id}")

        quiz = await self.quiz_repo.find_by_id(quiz_id)
        if not quiz:
            logger.warning(f"Count failed — quiz not found: {quiz_id}")
            raise QuestionQuizNotFoundException()

        count = await self.question_repo.count_by_quiz(quiz_id)
        logger.info(f"Quiz {quiz_id} has {count} active questions")

        result = {
            "quiz_id": quiz_id,
            "total_questions": count
        }
        return result

    async def get_question_by_id_for_student(
        self,
        question_id: str
    ) -> QuestionStudentResponseSchema:
        """
        Fetch a single question for student — correct_answer never included.
        """
        logger.info(f"Student fetching question: {question_id}")

        question = await self.question_repo.find_by_id(question_id)
        if not question:
            logger.warning(f"Question not found for student: {question_id}")
            raise QuestionNotFoundException()

        logger.info(f"Student question found: {question_id}")

        result = question_to_student_response(question)
        return result

    async def get_questions_by_quiz_for_student(
        self,
        quiz_id: str
    ) -> QuestionStudentListResponseSchema:
        """
        Fetch all questions for a quiz — student safe, no correct_answer.
        """
        logger.info(f"Student fetching questions for quiz: {quiz_id}")

        quiz = await self.quiz_repo.find_by_id(quiz_id)
        if not quiz:
            logger.warning(f"Student get questions failed — quiz not found: {quiz_id}")
            raise QuestionQuizNotFoundException()

        questions = await self.question_repo.find_by_quiz_id(quiz_id)
        logger.info(f"Returned {len(questions)} questions (student view) for quiz: {quiz_id}")

        result = questions_to_student_response(questions)
        return result

    async def get_questions_by_difficulty_for_student(
        self,
        quiz_id: str,
        difficulty: str
    ) -> QuestionStudentListResponseSchema:
        """
        Fetch questions by difficulty for student — no correct_answer.
        """
        logger.info(f"Student fetching {difficulty} questions for quiz: {quiz_id}")

        quiz = await self.quiz_repo.find_by_id(quiz_id)
        if not quiz:
            logger.warning(f"Student difficulty filter failed — quiz not found: {quiz_id}")
            raise QuestionQuizNotFoundException()

        questions = await self.question_repo.find_by_quiz_and_difficulty(
            quiz_id,
            difficulty
        )
        logger.info(
            f"Returned {len(questions)} {difficulty} questions "
            f"(student view) for quiz: {quiz_id}"
        )

        result = questions_to_student_response(questions)
        return result