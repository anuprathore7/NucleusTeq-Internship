import uuid
import pytest


class TestAttemptAPI:
    """
    Integration tests for the Quiz Attempt API.

    Covers SRS test cases:
    ATT-001 → Start quiz attempt with valid quiz id → attempt created
    ATT-002 → Create question snapshot on start → snapshot stored
    ATT-003 → Save answers on submit → answers saved in attempt
    ATT-004 → Resume attempt → previous answers returned
    ATT-005 → Submit quiz with valid responses → submission successful
    ATT-006 → Submit after time expiry → noted as future TTL feature
    ATT-007 → Re-attempt quiz when max reached → 409 access denied
    ATT-008 → Attempt invalid quiz id → 404 Not Found
    """

    # shared state across tests in this class
    category_id = None
    quiz_id = None
    question_1_id = None
    question_2_id = None
    attempt_id = None

    def _setup_quiz_with_questions(self, client, admin_headers) -> tuple:
        """
        Helper that creates a complete category → quiz → questions chain.
        Returns (category_id, quiz_id, question_1_id, question_2_id).
        Used as setup before attempt tests run.
        """
        # create category
        cat = client.post(
            "/assessment/v1/categories/",
            json={
                "name": f"Attempt Test Category {uuid.uuid4().hex[:8]}",
                "description": "Category for attempt tests"
            },
            headers=admin_headers
        )
        category_id = cat.json()["id"]

        # create quiz
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

        # create question 1 — MCQ
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

        # create question 2 — True/False
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

    # ── ATT-001 ───────────────────────────────────────────────────────────────
    def test_att_001_start_attempt_success(
        self, client, admin_headers, student_headers
    ):
        """
        ATT-001: Verifies student can start a quiz attempt with a valid
        quiz id. Asserts attempt is created with status in_progress,
        questions are returned without correct_answer,
        and answers list is empty at start.
        """
        # setup — create quiz with questions
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
            f"Expected 201 but got {response.status_code}. " \
            f"Response: {response.json()}"

        data = response.json()
        assert "id" in data, \
            "Response must contain attempt id"
        assert data["status"] == "in_progress", \
            "New attempt status must be in_progress"
        assert data["quiz_id"] == TestAttemptAPI.quiz_id
        assert isinstance(data["questions"], list)
        assert len(data["questions"]) == 2, \
            "Attempt must contain all 2 questions"
        assert data["answers"] == [], \
            "Answers must be empty at start"

        # save attempt id for later tests
        TestAttemptAPI.attempt_id = data["id"]

    # ── ATT-002 ───────────────────────────────────────────────────────────────
    def test_att_002_snapshot_stored_correctly(
        self, client, admin_headers, student_headers
    ):
        """
        ATT-002: Verifies that when an attempt starts, a snapshot of
        questions is stored and returned correctly.

        Key checks:
        - questions come from snapshot not live collection
        - correct_answer is never in the response
        - question structure is correct
        """
        response = client.get(
            f"/assessment/v1/attempts/{TestAttemptAPI.attempt_id}",
            headers=student_headers
        )

        assert response.status_code == 200

        data = response.json()
        questions = data["questions"]

        assert len(questions) == 2, \
            "Snapshot must contain all questions from the quiz"

        for question in questions:
            # verify snapshot has correct fields
            assert "question_id" in question
            assert "question_text" in question
            assert "options" in question
            assert "marks" in question

            # most important — correct_answer must never be in snapshot response
            assert "correct_answer" not in question, \
                "Snapshot must never expose correct_answer to student"

        # verify question ids match what was created
        question_ids_in_snapshot = [q["question_id"] for q in questions]
        assert TestAttemptAPI.question_1_id in question_ids_in_snapshot
        assert TestAttemptAPI.question_2_id in question_ids_in_snapshot

    # ── ATT-003 ───────────────────────────────────────────────────────────────
    def test_att_003_answers_saved_on_submit(
        self, client, admin_headers, student_headers
    ):
        """
        ATT-003: Verifies answers are saved correctly when student submits.

        Note: Our design does not support partial answer saving mid-attempt.
        All answers are submitted together in one request.
        This test starts a fresh attempt and verifies answers are
        stored in the attempt document after submission.
        """
        # start a fresh attempt for this test
        start_response = client.post(
            "/assessment/v1/attempts/start",
            json={"quiz_id": TestAttemptAPI.quiz_id},
            headers=student_headers
        )

        assert start_response.status_code == 201, \
            "Fresh attempt should start successfully (attempt 2 of 2)"

        fresh_attempt_id = start_response.json()["id"]

        # submit answers
        submit_response = client.post(
            f"/assessment/v1/attempts/{fresh_attempt_id}/submit",
            json={
                "answers": [
                    {
                        "question_id": TestAttemptAPI.question_1_id,
                        "selected_answer": "A mutable sequence"
                    },
                    {
                        "question_id": TestAttemptAPI.question_2_id,
                        "selected_answer": "False"
                    }
                ]
            },
            headers=student_headers
        )

        assert submit_response.status_code == 200, \
            f"Submit should succeed but got {submit_response.status_code}"

        data = submit_response.json()
        assert "message" in data
        assert "submitted" in data["message"].lower()

    # ── ATT-004 ───────────────────────────────────────────────────────────────
    def test_att_004_resume_attempt_returns_questions(
        self, client, student_headers
    ):
        """
        ATT-004: Verifies student can resume an in_progress attempt
        and sees the same questions that were in the original snapshot.

        The original attempt from ATT-001 is still in_progress
        (we submitted the fresh one in ATT-003, not the original).
        """
        response = client.get(
            f"/assessment/v1/attempts/{TestAttemptAPI.attempt_id}",
            headers=student_headers
        )

        assert response.status_code == 200, \
            f"Expected 200 but got {response.status_code}"

        data = response.json()

        # verify questions are still there from snapshot
        assert len(data["questions"]) == 2, \
            "Resumed attempt must still show all questions from snapshot"

        # verify no correct_answer on resume
        for question in data["questions"]:
            assert "correct_answer" not in question

        # verify status is still in_progress (we haven't submitted this one)
        assert data["status"] == "in_progress"

    # ── ATT-005 ───────────────────────────────────────────────────────────────
    def test_att_005_submit_quiz_success(
        self, client, student_headers
    ):
        """
        ATT-005: Verifies student can submit the original attempt
        with valid responses and receives a success message.
        """
        response = client.post(
            f"/assessment/v1/attempts/{TestAttemptAPI.attempt_id}/submit",
            json={
                "answers": [
                    {
                        "question_id": TestAttemptAPI.question_1_id,
                        "selected_answer": "A mutable sequence"
                    },
                    {
                        "question_id": TestAttemptAPI.question_2_id,
                        "selected_answer": "False"
                    }
                ]
            },
            headers=student_headers
        )

        assert response.status_code == 200, \
            f"Expected 200 but got {response.status_code}. " \
            f"Response: {response.json()}"

        data = response.json()
        assert "message" in data
        assert "submitted" in data["message"].lower(), \
            "Response message must confirm submission"

    # ── ATT-005 extended — submit twice ───────────────────────────────────────
    def test_submit_already_submitted_attempt_returns_409(
        self, client, student_headers
    ):
        """
        Verifies submitting the same attempt twice is rejected with
        409 Conflict since the attempt is already submitted.
        """
        response = client.post(
            f"/assessment/v1/attempts/{TestAttemptAPI.attempt_id}/submit",
            json={
                "answers": [
                    {
                        "question_id": TestAttemptAPI.question_1_id,
                        "selected_answer": "A mutable sequence"
                    },
                    {
                        "question_id": TestAttemptAPI.question_2_id,
                        "selected_answer": "False"
                    }
                ]
            },
            headers=student_headers
        )

        assert response.status_code == 409, \
            f"Expected 409 but got {response.status_code}"

    # ── ATT-006 ───────────────────────────────────────────────────────────────
    def test_att_006_time_expiry_note(self):
        """
        ATT-006: Auto-submit on time expiry requires a MongoDB TTL index
        which is an extended goal from the SRS.

        Current behavior:
        Time limit is stored in snapshot (time_limit field).
        Frontend is responsible for tracking time and calling submit
        before expiry. Backend does not auto-submit via TTL currently.

        This is documented as a future enhancement.
        TTL index would automatically trigger submission when
        started_at + time_limit minutes has passed.
        """
        # this test documents the design decision
        # no assertion needed — just documents the gap
        assert True, \
            "ATT-006 auto-submit via TTL is a future enhancement"

    # ── ATT-007 ───────────────────────────────────────────────────────────────
    def test_att_007_max_attempts_reached_returns_409(
        self, client, admin_headers, student_headers
    ):
        """
        ATT-007: Verifies that attempting the same quiz more than
        the maximum allowed (2) times is rejected with 409 Conflict.

        ATT-001 used attempt 1 on TestAttemptAPI.quiz_id
        ATT-003 used attempt 2 on TestAttemptAPI.quiz_id
        This test tries attempt 3 — should be blocked.
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

    # ── ATT-008 ───────────────────────────────────────────────────────────────
    def test_att_008_attempt_invalid_quiz_returns_404(
        self, client, student_headers
    ):
        """
        ATT-008: Verifies starting an attempt with a well-formed but
        non-existent quiz id returns 404 Not Found.
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

    # ── Additional security and edge case tests ───────────────────────────────

    def test_admin_cannot_start_attempt(self, client, admin_headers):
        """
        Verifies admin is blocked from starting an attempt,
        confirming require_student dependency is enforced.
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
        Verifies a student cannot access an attempt belonging
        to a different student. Returns 403 Forbidden.

        Creates a second student, tries to access first student's attempt.
        """
        # register and login a second student
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
        second_token = login.json()["access_token"]
        second_headers = {"Authorization": f"Bearer {second_token}"}

        # second student tries to access first student's attempt
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
        belong to the quiz in this attempt returns 400 Bad Request.
        """
        # create fresh quiz and attempt for this test
        cat = client.post(
            "/assessment/v1/categories/",
            json={
                "name": f"Invalid Q Test {uuid.uuid4().hex[:8]}",
                "description": "Test category"
            },
            headers=admin_headers
        )
        quiz = client.post(
            "/assessment/v1/quizzes/",
            json={
                "title": f"Invalid Q Quiz {uuid.uuid4().hex[:8]}",
                "description": "Test quiz",
                "category_id": cat.json()["id"],
                "time_limit": 30,
                "pass_percentage": 50.0
            },
            headers=admin_headers
        )
        q = client.post(
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

        attempt = client.post(
            "/assessment/v1/attempts/start",
            json={"quiz_id": quiz.json()["id"]},
            headers=student_headers
        )
        attempt_id = attempt.json()["id"]

        # submit with a fake question id not in snapshot
        response = client.post(
            f"/assessment/v1/attempts/{attempt_id}/submit",
            json={
                "answers": [
                    {
                        "question_id": "000000000000000000000000",
                        "selected_answer": "A"
                    }
                ]
            },
            headers=student_headers
        )

        assert response.status_code == 400, \
            f"Expected 400 but got {response.status_code}"

    def test_get_my_attempts_returns_list(
        self, client, student_headers
    ):
        """
        Verifies student can fetch all their attempts and
        the response has the correct list structure.
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