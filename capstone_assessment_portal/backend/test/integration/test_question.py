import uuid
import pytest


class TestQuestionAPI:
    """
    Integration tests for the Question API.
    """

    category_id = None
    quiz_id = None
    mcq_question_id = None
    tf_question_id = None

    def _create_category_and_quiz(self, client, admin_headers) -> tuple:
        """
        Setup helper that creates the category and quiz needed
        before any question can be created. Not a test itself.
        Returns (category_id, quiz_id).
        """
        category_response = client.post(
            "/assessment/v1/categories/",
            json={
                "name": f"QST Category {uuid.uuid4().hex[:8]}",
                "description": "Category for question tests"
            },
            headers=admin_headers
        )
        category_id = category_response.json()["id"]

        quiz_response = client.post(
            "/assessment/v1/quizzes/",
            json={
                "title": f"QST Quiz {uuid.uuid4().hex[:8]}",
                "description": "Quiz for question tests",
                "category_id": category_id,
                "time_limit": 30,
                "pass_percentage": 60.0
            },
            headers=admin_headers
        )
        quiz_id = quiz_response.json()["id"]

        return category_id, quiz_id

    def test_qst_001_add_mcq_question_success(self, client, admin_headers):
        """
        QST-001: Verifies admin can create an MCQ question with exactly
        4 options and a correct_answer that matches one of the options.
        Asserts 201 status, question type, and correct field values.
        """
        TestQuestionAPI.category_id, TestQuestionAPI.quiz_id = \
            self._create_category_and_quiz(client, admin_headers)

        response = client.post(
            "/assessment/v1/questions/",
            json={
                "quiz_id": TestQuestionAPI.quiz_id,
                "question_text": "What is a list in Python?",
                "question_type": "mcq",
                "options": [
                    "A mutable sequence",
                    "An immutable sequence",
                    "A key-value store",
                    "A set of unique items"
                ],
                "correct_answer": "A mutable sequence",
                "difficulty": "easy",
                "tags": ["python", "data-structures"],
                "marks": 1
            },
            headers=admin_headers
        )

        assert response.status_code == 201, \
            f"Expected 201 but got {response.status_code}. Response: {response.json()}"

        data = response.json()
        assert "id" in data
        assert data["question_type"] == "mcq"
        assert data["correct_answer"] == "A mutable sequence"
        assert len(data["options"]) == 4
        assert data["difficulty"] == "easy"
        assert data["marks"] == 1

        TestQuestionAPI.mcq_question_id = data["id"]

    def test_qst_002_add_true_false_question_success(self, client, admin_headers):
        """
        QST-002: Verifies admin can create a True/False question with
        exactly ["True", "False"] as options. Any other option values
        are rejected by the model_validator.
        """
        response = client.post(
            "/assessment/v1/questions/",
            json={
                "quiz_id": TestQuestionAPI.quiz_id,
                "question_text": "Python is a compiled language.",
                "question_type": "true_false",
                "options": ["True", "False"],
                "correct_answer": "False",
                "difficulty": "easy",
                "tags": ["python", "basics"],
                "marks": 1
            },
            headers=admin_headers
        )

        assert response.status_code == 201, \
            f"Expected 201 but got {response.status_code}. Response: {response.json()}"

        data = response.json()
        assert data["question_type"] == "true_false"
        assert data["options"] == ["True", "False"]
        assert data["correct_answer"] == "False"

        TestQuestionAPI.tf_question_id = data["id"]

    def test_qst_003_missing_correct_answer_returns_422(self, client, admin_headers):
        """
        QST-003: Verifies a question where correct_answer does not
        match any of the provided options is rejected with 422.
        The model_validator catches this before reaching the service.
        """
        response = client.post(
            "/assessment/v1/questions/",
            json={
                "quiz_id": TestQuestionAPI.quiz_id,
                "question_text": "Answer not in options?",
                "question_type": "mcq",
                "options": ["A", "B", "C", "D"],
                "correct_answer": "E",
                "difficulty": "easy",
                "marks": 1
            },
            headers=admin_headers
        )

        assert response.status_code == 422, \
            f"Expected 422 but got {response.status_code}"

    def test_qst_003_mcq_wrong_option_count_returns_422(self, client, admin_headers):
        """
        QST-003 extended: MCQ with fewer than 4 options is rejected.
        model_validator enforces exactly 4 options for MCQ questions.
        """
        response = client.post(
            "/assessment/v1/questions/",
            json={
                "quiz_id": TestQuestionAPI.quiz_id,
                "question_text": "Only two options?",
                "question_type": "mcq",
                "options": ["A", "B"],
                "correct_answer": "A",
                "difficulty": "easy",
                "marks": 1
            },
            headers=admin_headers
        )

        assert response.status_code == 422, \
            f"Expected 422 but got {response.status_code}"

    def test_qst_003_true_false_wrong_options_returns_422(self, client, admin_headers):
        """
        QST-003 extended: True/False question with options other than
        ["True", "False"] is rejected by model_validator with 422.
        """
        response = client.post(
            "/assessment/v1/questions/",
            json={
                "quiz_id": TestQuestionAPI.quiz_id,
                "question_text": "Is Python easy?",
                "question_type": "true_false",
                "options": ["Yes", "No"],
                "correct_answer": "Yes",
                "difficulty": "easy",
                "marks": 1
            },
            headers=admin_headers
        )

        assert response.status_code == 422, \
            f"Expected 422 but got {response.status_code}"

    def test_qst_006_get_questions_by_quiz(self, client, admin_headers):
        """
        QST-006: Verifies fetching all questions for a valid quiz id
        returns the correct list structure with total count and
        includes both questions created in QST-001 and QST-002.
        """
        response = client.get(
            f"/assessment/v1/questions/quiz/{TestQuestionAPI.quiz_id}",
            headers=admin_headers
        )

        assert response.status_code == 200, \
            f"Expected 200 but got {response.status_code}"

        data = response.json()
        assert "total" in data
        assert "questions" in data
        assert isinstance(data["questions"], list)
        assert data["total"] >= 2

        question_ids = [q["id"] for q in data["questions"]]
        assert TestQuestionAPI.mcq_question_id in question_ids
        assert TestQuestionAPI.tf_question_id in question_ids

    def test_qst_007_invalid_quiz_id_returns_404(self, client, admin_headers):
        """
        QST-007: Verifies fetching questions with a well-formed but
        non-existent quiz id returns 404 Not Found, not an empty list
        or a server error.
        """
        response = client.get(
            "/assessment/v1/questions/quiz/000000000000000000000000",
            headers=admin_headers
        )

        assert response.status_code == 404, \
            f"Expected 404 but got {response.status_code}"

    def test_admin_question_response_includes_correct_answer(
        self, client, admin_headers
    ):
        """
        Security check: verifies admin question list endpoint always
        includes correct_answer, confirming the admin mapper is used.
        """
        response = client.get(
            f"/assessment/v1/questions/quiz/{TestQuestionAPI.quiz_id}",
            headers=admin_headers
        )

        data = response.json()
        for question in data["questions"]:
            assert "correct_answer" in question, \
                "Admin view must always include correct_answer"

    def test_student_question_response_hides_correct_answer(
        self, client, student_headers
    ):
        """
        Security check: verifies student question endpoint never exposes
        correct_answer in any question in the response.
        """
        response = client.get(
            f"/assessment/v1/questions/student/quiz/{TestQuestionAPI.quiz_id}",
            headers=student_headers
        )

        assert response.status_code == 200, \
            f"Expected 200 but got {response.status_code}"

        data = response.json()
        for question in data["questions"]:
            assert "correct_answer" not in question, \
                "Student view must never expose correct_answer"

    def test_admin_blocked_from_student_routes(self, client, admin_headers):
        """
        Verifies admin cannot access student-only routes,
        confirming require_student dependency blocks them with 403.
        """
        response = client.get(
            f"/assessment/v1/questions/student/quiz/{TestQuestionAPI.quiz_id}",
            headers=admin_headers
        )

        assert response.status_code == 403, \
            f"Expected 403 but got {response.status_code}"

    def test_get_questions_by_difficulty(self, client, admin_headers):
        """
        Verifies filtering questions by difficulty level returns
        only questions matching the requested difficulty.
        """
        response = client.get(
            f"/assessment/v1/questions/quiz/"
            f"{TestQuestionAPI.quiz_id}/difficulty/easy",
            headers=admin_headers
        )

        assert response.status_code == 200, \
            f"Expected 200 but got {response.status_code}"

        data = response.json()
        for question in data["questions"]:
            assert question["difficulty"] == "easy", \
                f"Expected difficulty: easy but got {question['difficulty']}"

    def test_qst_004_update_question_success(self, client, admin_headers):
        """
        QST-004: Verifies admin can update an existing question's
        difficulty and marks. Only provided fields are updated,
        other fields remain unchanged.
        """
        response = client.put(
            f"/assessment/v1/questions/{TestQuestionAPI.mcq_question_id}",
            json={
                "difficulty": "medium",
                "marks": 2
            },
            headers=admin_headers
        )

        assert response.status_code == 200, \
            f"Expected 200 but got {response.status_code}. Response: {response.json()}"

        data = response.json()
        assert data["difficulty"] == "medium"
        assert data["marks"] == 2
        assert data["id"] == TestQuestionAPI.mcq_question_id

    def test_update_question_invalid_id_returns_404(self, client, admin_headers):
        """
        Edge case: updating a question with a non-existent id
        returns 404 Not Found.
        """
        response = client.put(
            "/assessment/v1/questions/000000000000000000000000",
            json={"difficulty": "hard"},
            headers=admin_headers
        )

        assert response.status_code == 404, \
            f"Expected 404 but got {response.status_code}"

    def test_student_cannot_create_question(self, client, student_headers):
        """
        Security check: verifies student is blocked from creating
        a question, confirming require_admin is enforced on that route.
        """
        response = client.post(
            "/assessment/v1/questions/",
            json={
                "quiz_id": TestQuestionAPI.quiz_id,
                "question_text": "Student attempt?",
                "question_type": "mcq",
                "options": ["A", "B", "C", "D"],
                "correct_answer": "A",
                "difficulty": "easy",
                "marks": 1
            },
            headers=student_headers
        )

        assert response.status_code == 403, \
            f"Expected 403 but got {response.status_code}"

    def test_qst_005_delete_question_success(self, client, admin_headers):
        """
        QST-005: Verifies admin can soft delete a question with a
        valid id and receives a confirmation message with 200 OK.
        Runs near the end so question is available for previous tests.
        """
        response = client.delete(
            f"/assessment/v1/questions/{TestQuestionAPI.tf_question_id}",
            headers=admin_headers
        )

        assert response.status_code == 200, \
            f"Expected 200 but got {response.status_code}. Response: {response.json()}"

        data = response.json()
        assert "message" in data
        assert "deleted" in data["message"].lower()

    def test_delete_already_deleted_question_returns_404(
        self, client, admin_headers
    ):
        """
        Verifies deleting the same question again returns 404,
        confirming soft-deleted questions are excluded from
        find_by_id lookups after is_active=False is set.
        """
        response = client.delete(
            f"/assessment/v1/questions/{TestQuestionAPI.tf_question_id}",
            headers=admin_headers
        )

        assert response.status_code == 404, \
            f"Expected 404 but got {response.status_code}"
    def test_get_question_count_for_quiz(self, client, admin_headers):
        """
        Verifies the question count endpoint returns the correct
        number of active questions in a quiz.
        """
        response = client.get(
            f"/assessment/v1/questions/quiz/{TestQuestionAPI.quiz_id}/count",
            headers=admin_headers
        )

        assert response.status_code == 200, \
            f"Expected 200 but got {response.status_code}"

        data = response.json()
        assert "total_questions" in data
        assert "quiz_id" in data
        assert data["total_questions"] >= 1

    def test_get_question_count_invalid_quiz(self, client, admin_headers):
        """
        Verifies getting question count for non-existent quiz
        returns 404 Not Found.
        """
        response = client.get(
            "/assessment/v1/questions/quiz/000000000000000000000000/count",
            headers=admin_headers
        )

        assert response.status_code == 404, \
            f"Expected 404 but got {response.status_code}"

    def test_student_difficulty_filter(self, client, student_headers):
        """
        Verifies student can filter questions by difficulty
        and correct_answer is never included in the response.
        """
        response = client.get(
            f"/assessment/v1/questions/student/quiz/"
            f"{TestQuestionAPI.quiz_id}/difficulty/easy",
            headers=student_headers
        )

        assert response.status_code == 200, \
            f"Expected 200 but got {response.status_code}"

        data = response.json()
        for question in data["questions"]:
            assert "correct_answer" not in question, \
                "Student difficulty view must never expose correct_answer"

    def test_student_cannot_update_question(self, client, student_headers):
        """
        Verifies student is blocked from updating a question.
        """
        response = client.put(
            f"/assessment/v1/questions/{TestQuestionAPI.mcq_question_id}",
            json={"difficulty": "hard"},
            headers=student_headers
        )

        assert response.status_code == 403, \
            f"Expected 403 but got {response.status_code}"

    def test_student_cannot_delete_question(self, client, student_headers):
        """
        Verifies student is blocked from deleting a question.
        """
        response = client.delete(
            f"/assessment/v1/questions/{TestQuestionAPI.mcq_question_id}",
            headers=student_headers
        )

        assert response.status_code == 403, \
            f"Expected 403 but got {response.status_code}"

    def test_get_questions_invalid_quiz_id_format(
        self, client, admin_headers
    ):
        """
        Verifies getting questions with a malformed quiz id
        returns 400 Bad Request instead of a server error.
        """
        response = client.get(
            "/assessment/v1/questions/quiz/not-a-valid-id",
            headers=admin_headers
        )

        assert response.status_code == 400, \
            f"Expected 400 but got {response.status_code}"

    def test_get_single_question_invalid_id_format(
        self, client, admin_headers
    ):
        """
        Verifies getting a question with malformed id
        returns 400 Bad Request.
        """
        response = client.get(
            "/assessment/v1/questions/not-a-valid-id",
            headers=admin_headers
        )

        assert response.status_code == 400, \
            f"Expected 400 but got {response.status_code}"

    def test_student_invalid_quiz_id_format(self, client, student_headers):
        """
        Verifies student getting questions with malformed quiz id
        returns 400 Bad Request.
        """
        response = client.get(
            "/assessment/v1/questions/student/quiz/not-a-valid-id",
            headers=student_headers
        )

        assert response.status_code == 400, \
            f"Expected 400 but got {response.status_code}"

    def test_student_quiz_not_found(self, client, student_headers):
        """
        Verifies student getting questions for non-existent quiz
        returns 404 Not Found.
        """
        response = client.get(
            "/assessment/v1/questions/student/quiz/000000000000000000000000",
            headers=student_headers
        )

        assert response.status_code == 404, \
            f"Expected 404 but got {response.status_code}"
        
    def test_get_single_question_success(self, client, admin_headers):
        """
        Verifies fetching a single question by valid id returns
        the correct question with correct_answer for admin.
        This hits question_service.get_question_by_id.
        """
        response = client.get(
            f"/assessment/v1/questions/{TestQuestionAPI.mcq_question_id}",
            headers=admin_headers
        )

        assert response.status_code == 200, \
            f"Expected 200 but got {response.status_code}"

        data = response.json()
        assert data["id"] == TestQuestionAPI.mcq_question_id
        assert "correct_answer" in data

    def test_get_single_question_not_found(self, client, admin_headers):
        """
        Verifies fetching a question with valid format but
        non-existent id returns 404 Not Found.
        This hits QuestionNotFoundException in question_service.get_question_by_id.
        """
        response = client.get(
            "/assessment/v1/questions/000000000000000000000000",
            headers=admin_headers
        )

        assert response.status_code == 404, \
            f"Expected 404 but got {response.status_code}"

    def test_update_correct_answer_not_in_options_returns_422(
        self, client, admin_headers
    ):
        """
        Verifies updating correct_answer to a value not in the current
        options is rejected with 422.
        This hits QuestionInvalidCorrectAnswerException
        in question_service.update_question.
        """
        response = client.put(
            f"/assessment/v1/questions/{TestQuestionAPI.mcq_question_id}",
            json={"correct_answer": "Z option not in list"},
            headers=admin_headers
        )

        assert response.status_code == 422, \
            f"Expected 422 but got {response.status_code}"

    def test_update_options_and_correct_answer_together(
        self, client, admin_headers
    ):
        """
        Verifies admin can update both options and correct_answer
        at the same time and the new correct_answer is validated
        against the new options not the old ones.
        This hits the final_options logic in question_service.update_question.
        """
        category_response = client.post(
            "/assessment/v1/categories/",
            json={
                "name": f"Update Opts Cat {uuid.uuid4().hex[:8]}",
                "description": "Category for update options test"
            },
            headers=admin_headers
        )
        category_id = category_response.json()["id"]

        quiz_response = client.post(
            "/assessment/v1/quizzes/",
            json={
                "title": f"Update Opts Quiz {uuid.uuid4().hex[:8]}",
                "description": "Quiz for update options test",
                "category_id": category_id,
                "time_limit": 30,
                "pass_percentage": 60.0
            },
            headers=admin_headers
        )
        quiz_id = quiz_response.json()["id"]

        q = client.post(
            "/assessment/v1/questions/",
            json={
                "quiz_id": quiz_id,
                "question_text": "Options update test question",
                "question_type": "mcq",
                "options": ["A", "B", "C", "D"],
                "correct_answer": "A",
                "difficulty": "easy",
                "marks": 1
            },
            headers=admin_headers
        )
        question_id = q.json()["id"]

        # update both options and correct_answer to new values
        response = client.put(
            f"/assessment/v1/questions/{question_id}",
            json={
                "options": ["W", "X", "Y", "Z"],
                "correct_answer": "W"
            },
            headers=admin_headers
        )

        assert response.status_code == 200, \
            f"Expected 200 but got {response.status_code}"

        data = response.json()
        assert data["correct_answer"] == "W"
        assert "W" in data["options"]

    def test_update_new_options_but_old_correct_answer_returns_422(
        self, client, admin_headers
    ):
        """
        Verifies updating options alone without updating correct_answer
        fails if the existing correct_answer is not in the new options.
        This hits the final_options validation in question_service.update_question.
        """
        category_response = client.post(
            "/assessment/v1/categories/",
            json={
                "name": f"Invalid Opts Cat {uuid.uuid4().hex[:8]}",
                "description": "Category for invalid options test"
            },
            headers=admin_headers
        )
        category_id = category_response.json()["id"]

        quiz_response = client.post(
            "/assessment/v1/quizzes/",
            json={
                "title": f"Invalid Opts Quiz {uuid.uuid4().hex[:8]}",
                "description": "Quiz for invalid options test",
                "category_id": category_id,
                "time_limit": 30,
                "pass_percentage": 60.0
            },
            headers=admin_headers
        )
        quiz_id = quiz_response.json()["id"]

        q = client.post(
            "/assessment/v1/questions/",
            json={
                "quiz_id": quiz_id,
                "question_text": "Options conflict test question",
                "question_type": "mcq",
                "options": ["A", "B", "C", "D"],
                "correct_answer": "A",
                "difficulty": "easy",
                "marks": 1
            },
            headers=admin_headers
        )
        question_id = q.json()["id"]

        # update options to W X Y Z but keep correct_answer as A
        # A is not in new options → should fail
        response = client.put(
            f"/assessment/v1/questions/{question_id}",
            json={
                "options": ["W", "X", "Y", "Z"]
                # correct_answer stays A — not in new options
            },
            headers=admin_headers
        )

        assert response.status_code == 422, \
            f"Expected 422 but got {response.status_code}"

    def test_student_single_question_not_found(self, client, student_headers):
        """
        Verifies student getting a non-existent question by id
        returns 404 Not Found.
        This hits QuestionNotFoundException
        in question_service.get_question_by_id_for_student.
        """
        response = client.get(
            "/assessment/v1/questions/student/000000000000000000000000",
            headers=student_headers
        )

        assert response.status_code == 404, \
            f"Expected 404 but got {response.status_code}"

    def test_student_single_question_invalid_id_format(
        self, client, student_headers
    ):
        """
        Verifies student getting a question with malformed id
        returns 400 Bad Request.
        """
        response = client.get(
            "/assessment/v1/questions/student/not-a-valid-id",
            headers=student_headers
        )

        assert response.status_code == 400, \
            f"Expected 400 but got {response.status_code}"

    def test_difficulty_filter_invalid_quiz_returns_404(
        self, client, admin_headers
    ):
        """
        Verifies filtering questions by difficulty for a non-existent quiz
        returns 404 Not Found.
        This hits QuestionQuizNotFoundException
        in question_service.get_questions_by_difficulty.
        """
        response = client.get(
            "/assessment/v1/questions/quiz/000000000000000000000000/difficulty/easy",
            headers=admin_headers
        )

        assert response.status_code == 404, \
            f"Expected 404 but got {response.status_code}"

    def test_student_difficulty_filter_invalid_quiz_returns_404(
        self, client, student_headers
    ):
        """
        Verifies student filtering questions by difficulty for a
        non-existent quiz returns 404 Not Found.
        This hits QuestionQuizNotFoundException
        in question_service.get_questions_by_difficulty_for_student.
        """
        response = client.get(
            "/assessment/v1/questions/student/quiz/000000000000000000000000/difficulty/easy",
            headers=student_headers
        )

        assert response.status_code == 404, \
            f"Expected 404 but got {response.status_code}"

    def test_delete_question_invalid_id_format_returns_400(
        self, client, admin_headers
    ):
        """
        Verifies deleting a question with a malformed id
        returns 400 Bad Request.
        """
        response = client.delete(
            "/assessment/v1/questions/not-a-valid-id",
            headers=admin_headers
        )

        assert response.status_code == 400, \
            f"Expected 400 but got {response.status_code}"

    def test_delete_question_not_found_returns_404(self, client, admin_headers):
        """
        Verifies deleting a question with valid format but
        non-existent id returns 404 Not Found.
        This hits QuestionNotFoundException
        in question_service.delete_question.
        """
        response = client.delete(
            "/assessment/v1/questions/000000000000000000000000",
            headers=admin_headers
        )

        assert response.status_code == 404, \
            f"Expected 404 but got {response.status_code}"

    def test_question_count_for_quiz_with_no_questions(
        self, client, admin_headers
    ):
        """
        Verifies question count returns 0 for a quiz that exists
        but has no questions added yet.
        This hits question_service.get_question_count_by_quiz
        with an empty result from the repository.
        """
        fresh_category = client.post(
            "/assessment/v1/categories/",
            json={
                "name": f"Empty Quiz Cat {uuid.uuid4().hex[:8]}",
                "description": "Category for empty quiz count test"
            },
            headers=admin_headers
        )
        empty_quiz = client.post(
            "/assessment/v1/quizzes/",
            json={
                "title": f"Empty Quiz {uuid.uuid4().hex[:8]}",
                "description": "Quiz with no questions",
                "category_id": fresh_category.json()["id"],
                "time_limit": 30,
                "pass_percentage": 60.0
            },
            headers=admin_headers
        )
        empty_quiz_id = empty_quiz.json()["id"]

        response = client.get(
            f"/assessment/v1/questions/quiz/{empty_quiz_id}/count",
            headers=admin_headers
        )

        assert response.status_code == 200, \
            f"Expected 200 but got {response.status_code}"

        data = response.json()
        assert data["total_questions"] == 0
        assert data["quiz_id"] == empty_quiz_id

    def test_update_duplicate_question_text_same_quiz_returns_409(
        self, client, admin_headers
    ):
        """
        Verifies updating a question text to match another existing
        question in the same quiz is rejected with 409 Conflict.
        This hits QuestionAlreadyExistsException
        in question_service.update_question duplicate text check.
        """
        fresh_category = client.post(
            "/assessment/v1/categories/",
            json={
                "name": f"Dup Update Cat {uuid.uuid4().hex[:8]}",
                "description": "Category for duplicate update test"
            },
            headers=admin_headers
        )
        fresh_quiz = client.post(
            "/assessment/v1/quizzes/",
            json={
                "title": f"Dup Update Quiz {uuid.uuid4().hex[:8]}",
                "description": "Quiz for duplicate update test",
                "category_id": fresh_category.json()["id"],
                "time_limit": 30,
                "pass_percentage": 60.0
            },
            headers=admin_headers
        )
        quiz_id = fresh_quiz.json()["id"]

        text_one = f"First unique question {uuid.uuid4().hex[:8]}"
        text_two = f"Second unique question {uuid.uuid4().hex[:8]}"

        q1 = client.post(
            "/assessment/v1/questions/",
            json={
                "quiz_id": quiz_id,
                "question_text": text_one,
                "question_type": "mcq",
                "options": ["A", "B", "C", "D"],
                "correct_answer": "A",
                "difficulty": "easy",
                "marks": 1
            },
            headers=admin_headers
        )

        q2 = client.post(
            "/assessment/v1/questions/",
            json={
                "quiz_id": quiz_id,
                "question_text": text_two,
                "question_type": "mcq",
                "options": ["A", "B", "C", "D"],
                "correct_answer": "A",
                "difficulty": "easy",
                "marks": 1
            },
            headers=admin_headers
        )
        q2_id = q2.json()["id"]

        # try to update q2 text to match q1 text — should conflict
        response = client.put(
            f"/assessment/v1/questions/{q2_id}",
            json={"question_text": text_one},
            headers=admin_headers
        )

        assert response.status_code == 409, \
            f"Expected 409 but got {response.status_code}"