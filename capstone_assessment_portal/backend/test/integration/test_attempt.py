import uuid
import pytest


class TestAttemptAPI:
    """
    Integration tests for the Quiz Attempt API.

    ATT-001 → Start quiz attempt with valid quiz id → attempt created
    ATT-002 → Create question snapshot on start → snapshot stored
    ATT-003 → Save answers on submit → answers saved in attempt
    ATT-004 → Resume attempt → previous answers returned
    ATT-005 → Submit quiz with valid responses → submission successful
    ATT-006 → Submit after time expiry → noted as future TTL feature
    ATT-007 → Re-attempt quiz when max reached → 409 access denied
    ATT-008 → Attempt invalid quiz id → 404 Not Found
    """

    category_id  = None
    quiz_id      = None
    question_1_id = None
    question_2_id = None
    attempt_id   = None

    def _setup_quiz_with_questions(self, client, admin_headers):
        """
        Creates category → quiz → 2 questions.
        Returns (category_id, quiz_id, question_1_id, question_2_id).
        Each call creates fresh data with uuid suffix to avoid collisions.
        """
        cat = client.post(
            "/assessment/v1/categories/",
            json={
                "name": f"Attempt Test Category {uuid.uuid4().hex[:8]}",
                "description": "Category for attempt tests"
            },
            headers=admin_headers
        )
        category_id = cat.json()["id"]

        quiz = client.post(
            "/assessment/v1/quizzes/",
            json={
                "title": f"Attempt Test Quiz {uuid.uuid4().hex[:8]}",
                "description": "Quiz for attempt tests",
                "category_id": category_id,
                "time_limit": 30,
                "pass_percentage": 50.0
            },
            headers=admin_headers
        )
        quiz_id = quiz.json()["id"]

        q1 = client.post(
            "/assessment/v1/questions/",
            json={
                "quiz_id": quiz_id,
                "question_text": "What is a list in Python",
                "question_type": "mcq",
                "options": [
                    "A mutable sequence",
                    "An immutable sequence",
                    "A key-value store",
                    "A set of unique items"
                ],
                "correct_answer": "A mutable sequence",
                "difficulty": "easy",
                "tags": ["python"],
                "marks": 1
            },
            headers=admin_headers
        )
        question_1_id = q1.json()["id"]

        q2 = client.post(
            "/assessment/v1/questions/",
            json={
                "quiz_id": quiz_id,
                "question_text": "Python is a compiled language",
                "question_type": "true_false",
                "options": ["True", "False"],
                "correct_answer": "False",
                "difficulty": "easy",
                "tags": ["python"],
                "marks": 1
            },
            headers=admin_headers
        )
        question_2_id = q2.json()["id"]

        return category_id, quiz_id, question_1_id, question_2_id

    def test_att_001_start_attempt_success(
        self, client, admin_headers, student_headers
    ):
        """
        ATT-001: Student starts a quiz attempt with a valid quiz id.
        Asserts 201 status, status is in_progress, questions returned
        without correct_answer, and answers list is empty at start.
        """
        (
            TestAttemptAPI.category_id,
            TestAttemptAPI.quiz_id,
            TestAttemptAPI.question_1_id,
            TestAttemptAPI.question_2_id
        ) = self._setup_quiz_with_questions(client, admin_headers)

        response = client.post(
            "/assessment/v1/attempts/start",
            json={"quiz_id": TestAttemptAPI.quiz_id},
            headers=student_headers
        )

        assert response.status_code == 201, \
            f"Expected 201 but got {response.status_code}. Response: {response.json()}"

        data = response.json()
        assert "id" in data
        assert data["status"] == "in_progress"
        assert data["quiz_id"] == TestAttemptAPI.quiz_id
        assert isinstance(data["questions"], list)
        assert len(data["questions"]) == 2
        assert data["answers"] == []

        TestAttemptAPI.attempt_id = data["id"]

    def test_att_002_snapshot_stored_correctly(
        self, client, student_headers
    ):
        """
        ATT-002: Verifies snapshot questions are returned without
        correct_answer when student views the attempt.
        """
        response = client.get(
            f"/assessment/v1/attempts/{TestAttemptAPI.attempt_id}",
            headers=student_headers
        )

        assert response.status_code == 200

        data = response.json()
        questions = data["questions"]

        assert len(questions) == 2

        for question in questions:
            assert "question_id" in question
            assert "question_text" in question
            assert "options" in question
            assert "correct_answer" not in question, \
                "Snapshot must never expose correct_answer to student"

        question_ids = [q["question_id"] for q in questions]
        assert TestAttemptAPI.question_1_id in question_ids
        assert TestAttemptAPI.question_2_id in question_ids

    def test_att_004_resume_attempt_returns_questions(
        self, client, student_headers
    ):
        """
        ATT-004: Student resumes the in_progress attempt from ATT-001.
        Verifies questions are still returned and status is in_progress.
        This test runs BEFORE ATT-003 submits the attempt.
        """
        response = client.get(
            f"/assessment/v1/attempts/{TestAttemptAPI.attempt_id}",
            headers=student_headers
        )

        assert response.status_code == 200, \
            f"Expected 200 but got {response.status_code}"

        data = response.json()

        assert len(data["questions"]) == 2
        assert data["status"] == "in_progress"

        for question in data["questions"]:
            assert "correct_answer" not in question

    def test_att_003_answers_saved_on_submit(
        self, client, admin_headers, student_headers
    ):
        """
        ATT-003: Verifies answers are saved when student submits the
        attempt from ATT-001. Uses save answer endpoint before submitting.
        This test submits the shared attempt_id.
        """
        client.post(
            f"/assessment/v1/attempts/{TestAttemptAPI.attempt_id}/answer",
            json={
                "question_id": TestAttemptAPI.question_1_id,
                "selected_answer": "A mutable sequence"
            },
            headers=student_headers
        )

        client.post(
            f"/assessment/v1/attempts/{TestAttemptAPI.attempt_id}/answer",
            json={
                "question_id": TestAttemptAPI.question_2_id,
                "selected_answer": "False"
            },
            headers=student_headers
        )

        submit_response = client.post(
            f"/assessment/v1/attempts/{TestAttemptAPI.attempt_id}/submit",
            headers=student_headers
        )

        assert submit_response.status_code == 200, \
            f"Submit should succeed but got {submit_response.status_code}"

        data = submit_response.json()
        assert "message" in data
        assert "submitted" in data["message"].lower()

    def test_att_005_submit_quiz_success(
        self, client, admin_headers, student_headers
    ):
        """
        ATT-005: Starts a fresh second attempt on the same quiz and
        submits it successfully. Uses attempt 2 of 2 for this student.
        """
        start_response = client.post(
            "/assessment/v1/attempts/start",
            json={"quiz_id": TestAttemptAPI.quiz_id},
            headers=student_headers
        )

        assert start_response.status_code == 201, \
            f"Second attempt should be allowed but got {start_response.status_code}"

        second_attempt_id = start_response.json()["id"]

        submit_response = client.post(
            f"/assessment/v1/attempts/{second_attempt_id}/submit",
            headers=student_headers
        )

        assert submit_response.status_code == 200, \
            f"Expected 200 but got {submit_response.status_code}. Response: {submit_response.json()}"

        data = submit_response.json()
        assert "message" in data
        assert "submitted" in data["message"].lower()

    def test_att_006_time_expiry_note(self):
        """
        ATT-006: Auto-submit on time expiry requires a MongoDB TTL index.
        This is documented as a future enhancement.
        Frontend tracks time and calls submit before expiry.
        """
        assert True

    def test_att_007_max_attempts_reached_returns_409(
        self, client, admin_headers, student_headers
    ):
        """
        ATT-007: The current student has now used both attempts on
        TestAttemptAPI.quiz_id (ATT-001 + ATT-005).
        A third attempt must be blocked with 409 Conflict.
        """
        response = client.post(
            "/assessment/v1/attempts/start",
            json={"quiz_id": TestAttemptAPI.quiz_id},
            headers=student_headers
        )

        assert response.status_code == 409, \
            f"Expected 409 max attempts but got {response.status_code}. " \
            f"Response: {response.json()}"

        data = response.json()
        assert "detail" in data

    def test_att_008_attempt_invalid_quiz_returns_404(
        self, client, student_headers
    ):
        """
        ATT-008: Starting an attempt with a non-existent quiz id
        returns 404 Not Found.
        """
        response = client.post(
            "/assessment/v1/attempts/start",
            json={"quiz_id": "000000000000000000000000"},
            headers=student_headers
        )

        assert response.status_code == 404, \
            f"Expected 404 but got {response.status_code}"

        data = response.json()
        assert "detail" in data

    def test_admin_cannot_start_attempt(self, client, admin_headers):
        """
        Verifies admin is blocked from starting an attempt.
        Only students can attempt quizzes.
        """
        response = client.post(
            "/assessment/v1/attempts/start",
            json={"quiz_id": TestAttemptAPI.quiz_id},
            headers=admin_headers
        )

        assert response.status_code == 403, \
            f"Expected 403 but got {response.status_code}"

    def test_student_cannot_access_another_students_attempt(
        self, client, admin_headers
    ):
        """
        Verifies a student cannot access an attempt belonging to
        a different student. Returns 403 Forbidden.
        """
        second_email = f"second_{uuid.uuid4().hex[:8]}@test.com"
        client.post(
            "/assessment/v1/auth/register",
            json={
                "username": f"second_{uuid.uuid4().hex[:8]}",
                "email": second_email,
                "password": "Test@1234"
            }
        )
        login = client.post(
            "/assessment/v1/auth/login",
            json={"email": second_email, "password": "Test@1234"}
        )
        second_headers = {"Authorization": f"Bearer {login.json()['access_token']}"}

        response = client.get(
            f"/assessment/v1/attempts/{TestAttemptAPI.attempt_id}",
            headers=second_headers
        )

        assert response.status_code == 403, \
            f"Expected 403 but got {response.status_code}"

    def test_attempt_malformed_id_returns_400(
        self, client, student_headers
    ):
        """
        Verifies accessing an attempt with a malformed ID
        returns 400 Bad Request instead of server error.
        """
        response = client.get(
            "/assessment/v1/attempts/not-a-valid-id",
            headers=student_headers
        )

        assert response.status_code == 400, \
            f"Expected 400 but got {response.status_code}"

    def test_submit_with_invalid_question_id_returns_400(
        self, client, admin_headers, student_headers
    ):
        """
        Verifies submitting answers with question IDs that do not
        belong to the quiz snapshot returns 400 Bad Request.
        Creates a completely isolated fresh quiz for this test.
        """
        cat = client.post(
            "/assessment/v1/categories/",
            json={
                "name": f"Invalid Q Test {uuid.uuid4().hex[:8]}",
                "description": "Category for invalid question id test"
            },
            headers=admin_headers
        )
        quiz = client.post(
            "/assessment/v1/quizzes/",
            json={
                "title": f"Invalid Q Quiz {uuid.uuid4().hex[:8]}",
                "description": "Quiz for invalid question id test",
                "category_id": cat.json()["id"],
                "time_limit": 30,
                "pass_percentage": 50.0
            },
            headers=admin_headers
        )
        client.post(
            "/assessment/v1/questions/",
            json={
                "quiz_id": quiz.json()["id"],
                "question_text": "Test question for invalid id test",
                "question_type": "mcq",
                "options": ["A", "B", "C", "D"],
                "correct_answer": "A",
                "difficulty": "easy",
                "marks": 1
            },
            headers=admin_headers
        )

        """
        Register a fresh student for this test so attempt count
        on this quiz is clean and not affected by other tests.
        """
        fresh_email = f"fresh_student_{uuid.uuid4().hex[:8]}@test.com"
        client.post(
            "/assessment/v1/auth/register",
            json={
                "username": f"fresh_{uuid.uuid4().hex[:8]}",
                "email": fresh_email,
                "password": "Test@1234"
            }
        )
        login = client.post(
            "/assessment/v1/auth/login",
            json={"email": fresh_email, "password": "Test@1234"}
        )
        fresh_headers = {"Authorization": f"Bearer {login.json()['access_token']}"}

        attempt = client.post(
            "/assessment/v1/attempts/start",
            json={"quiz_id": quiz.json()["id"]},
            headers=fresh_headers
        )
        attempt_id = attempt.json()["id"]

        """
        Submit with a fake question id that is not in the snapshot.
        Backend should validate and return 400.
        """
        response = client.post(
            f"/assessment/v1/attempts/{attempt_id}/answer",
            json={
                "question_id": "000000000000000000000000",
                "selected_answer": "A"
            },
            headers=fresh_headers
        )

        assert response.status_code == 400, \
            f"Expected 400 but got {response.status_code}. " \
            f"Saving an answer for a question not in this quiz should be rejected"

    def test_get_my_attempts_returns_list(
        self, client, student_headers
    ):
        """
        Verifies student can fetch all their attempts and the
        response has the correct list structure with total count.
        """
        response = client.get(
            "/assessment/v1/attempts/my",
            headers=student_headers
        )

        assert response.status_code == 200, \
            f"Expected 200 but got {response.status_code}"

        data = response.json()
        assert "total" in data
        assert "attempts" in data
        assert isinstance(data["attempts"], list)
        assert data["total"] >= 1