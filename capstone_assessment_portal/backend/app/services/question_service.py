from datetime import datetime, timezone

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
from app.utils.question_mapper import question_to_response, questions_to_response ,question_to_student_response, questions_to_student_response
from app.constants.question_constants import MCQ, TRUE_FALSE, TRUE_FALSE_OPTIONS, MCQ_OPTIONS_COUNT


class QuestionService:
    """
    Contains ALL business logic for question operations.
    """

    def __init__(self):
        self.question_repo = QuestionRepository()
        # needed to validate quiz_id exists before creating/updating questions
        self.quiz_repo = QuizRepository()

    async def create_question(
        self,
        data: CreateQuestionSchema
    ) -> QuestionResponseSchema:
        """
        Add a new question to a quiz.
        """

        # verify quiz exists
        # We cannot add a question to a quiz that doesn't exist
        quiz = await self.quiz_repo.find_by_id(data.quiz_id)
        if not quiz:
            raise QuestionQuizNotFoundException()

        # check for duplicate question text in same quiz
        # Same question cannot appear twice in the same quiz
        existing = await self.question_repo.find_by_text_and_quiz(
            data.question_text,
            data.quiz_id
        )
        if existing:
            raise QuestionAlreadyExistsException()

        # verify correct_answer matches one of the options
        # Pydantic already validated this, but service-level check adds safety
        if data.correct_answer not in data.options:
            raise QuestionInvalidCorrectAnswerException()

        # build the MongoDB document
        new_question = {
            **data.model_dump(),
            "is_active": True,
            "created_at": datetime.now(timezone.utc),
            "updated_at": datetime.now(timezone.utc)
        }

        # save to database
        saved_question = await self.question_repo.create(new_question)

        # return clean response
        return question_to_response(saved_question)

    async def get_questions_by_quiz(
        self,
        quiz_id: str
    ) -> QuestionListResponseSchema:
        """
        Get all questions belonging to a specific quiz.
        """

        # verify quiz exists
        quiz = await self.quiz_repo.find_by_id(quiz_id)
        if not quiz:
            raise QuestionQuizNotFoundException()

        # fetch questions
        questions = await self.question_repo.find_by_quiz_id(quiz_id)

        # return structured response
        return questions_to_response(questions)

    async def get_questions_by_difficulty(
        self,
        quiz_id: str,
        difficulty: str
    ) -> QuestionListResponseSchema:
        """
        Get questions from a quiz filtered by difficulty level.
        """

        # verify quiz exists
        quiz = await self.quiz_repo.find_by_id(quiz_id)
        if not quiz:
            raise QuestionQuizNotFoundException()

        # fetch filtered questions
        questions = await self.question_repo.find_by_quiz_and_difficulty(
            quiz_id,
            difficulty
        )

        # return structured response
        return questions_to_response(questions)

    async def get_question_by_id(
        self,
        question_id: str
    ) -> QuestionResponseSchema:
        """
        Fetch a single question by its ID.
        """
        question = await self.question_repo.find_by_id(question_id)

        if not question:
            raise QuestionNotFoundException()

        return question_to_response(question)

    async def update_question(
        self,
        question_id: str,
        data: UpdateQuestionSchema
    ) -> QuestionResponseSchema:
        """
        Update an existing question.
        """

        # confirm question exists
        existing_question = await self.question_repo.find_by_id(question_id)
        if not existing_question:
            raise QuestionNotFoundException()

        # determine what the final options will be after update
        # If new options are provided, use them
        # If not, keep the existing options from DB
        # We need this to validate correct_answer against the right options
        final_options = data.options if data.options is not None \
            else existing_question["options"]

        # if correct_answer is being updated, validate it
        # against the final options (new or existing)
        if data.correct_answer is not None:
            if data.correct_answer not in final_options:
                raise QuestionInvalidCorrectAnswerException()

        # if question text is changing, check for duplicates
        if data.question_text:
            duplicate = await self.question_repo.find_by_text_and_quiz(
                data.question_text,
                existing_question["quiz_id"]  # same quiz as the existing question
            )
            # make sure the duplicate found is not the same question we are updating
            if duplicate and str(duplicate["_id"]) != question_id:
                raise QuestionAlreadyExistsException()

        # build update dict with only provided fields
        # exclude_none=True skips fields that are None
        # so we only update what was actually sent
        update_data = data.model_dump(exclude_none=True)
        update_data["updated_at"] = datetime.now(timezone.utc)

        # save and return
        updated_question = await self.question_repo.update(question_id, update_data)
        return question_to_response(updated_question)

    async def delete_question(self, question_id: str) -> dict:
        """
        Soft delete a question.
        """

        # confirm it exists
        existing_question = await self.question_repo.find_by_id(question_id)
        if not existing_question:
            raise QuestionNotFoundException()

        # soft delete
        await self.question_repo.delete(question_id)

        # return confirmation with the question text so admin
        # knows exactly which question was deleted
        return {
            "message": f"Question deleted successfully"
        }

    async def get_question_count_by_quiz(self, quiz_id: str) -> dict:
        """
        Get total number of active questions in a quiz.
        """

        # verify quiz exists first
        quiz = await self.quiz_repo.find_by_id(quiz_id)
        if not quiz:
            raise QuestionQuizNotFoundException()

        count = await self.question_repo.count_by_quiz(quiz_id)

        return {
            "quiz_id": quiz_id,
            "total_questions": count
        }
    
    async def get_question_by_id_for_student(
        self,
        question_id: str
    ) -> QuestionStudentResponseSchema:
        """
        Fetch a single question for a student.
        Same data lookup as the admin version, but mapped
        through the student-safe mapper so correct_answer never leaks.
        """
        question = await self.question_repo.find_by_id(question_id)

        if not question:
            raise QuestionNotFoundException()

        return question_to_student_response(question)

    async def get_questions_by_quiz_for_student(
        self,
        quiz_id: str
    ) -> QuestionStudentListResponseSchema:
        """
        Fetch all questions in a quiz for a student.
        Verifies the quiz exists first, same as the admin version,
        but returns the student-safe list (no correct_answer).
        """
        quiz = await self.quiz_repo.find_by_id(quiz_id)
        if not quiz:
            raise QuestionQuizNotFoundException()

        questions = await self.question_repo.find_by_quiz_id(quiz_id)
        return questions_to_student_response(questions)

    async def get_questions_by_difficulty_for_student(
        self,
        quiz_id: str,
        difficulty: str
    ) -> QuestionStudentListResponseSchema:
        """
        Fetch questions in a quiz filtered by difficulty, for a student.
        Same logic as admin version, mapped through the student-safe mapper.
        """
        quiz = await self.quiz_repo.find_by_id(quiz_id)
        if not quiz:
            raise QuestionQuizNotFoundException()

        questions = await self.question_repo.find_by_quiz_and_difficulty(
            quiz_id,
            difficulty
        )
        return questions_to_student_response(questions)