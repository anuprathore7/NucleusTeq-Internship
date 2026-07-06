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

    def test_duplicate_question_text_in_same_quiz(
        self, client, admin_headers
    ):
        """
        Verifies creating two questions with identical text
        in the same quiz is rejected with 409 Conflict.
        Creates a fresh quiz specifically for this test
        so it is independent of other tests that delete questions.
        """
        # create a fresh category and quiz for this test
        # so we are not affected by deletes in previous tests
        cat_response = client.post(
            "/assessment/v1/categories/",
            json={
                "name": f"Dup Test Category {uuid.uuid4().hex[:8]}",
                "description": "Category for duplicate test"
            },
            headers=admin_headers
        )
        category_id = cat_response.json()["id"]

        quiz_response = client.post(
            "/assessment/v1/quizzes/",
            json={
                "title": f"Dup Test Quiz {uuid.uuid4().hex[:8]}",
                "description": "Quiz for duplicate question test",
                "category_id": category_id,
                "time_limit": 30,
                "pass_percentage": 60.0
            },
            headers=admin_headers
        )
        fresh_quiz_id = quiz_response.json()["id"]

        # use a fixed question text for both attempts
        question_text = f"Is Python interpreted {uuid.uuid4().hex[:8]}?"

        # first question — should succeed
        first_response = client.post(
            "/assessment/v1/questions/",
            json={
                "quiz_id": fresh_quiz_id,
                "question_text": question_text,
                "question_type": "mcq",
                "options": ["A", "B", "C", "D"],
                "correct_answer": "A",
                "difficulty": "easy",
                "marks": 1
            },
            headers=admin_headers
        )

        assert first_response.status_code == 201, \
            f"First question should succeed but got {first_response.status_code}"

        # second question with same text in same quiz — should fail
        response = client.post(
            "/assessment/v1/questions/",
            json={
                "quiz_id": fresh_quiz_id,
                "question_text": question_text,   # exact same text
                "question_type": "mcq",
                "options": ["A", "B", "C", "D"],
                "correct_answer": "A",
                "difficulty": "easy",
                "marks": 1
            },
            headers=admin_headers
        )

        assert response.status_code == 409, \
            f"Expected 409 but got {response.status_code}. " \
            f"Duplicate question text should be rejected"

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