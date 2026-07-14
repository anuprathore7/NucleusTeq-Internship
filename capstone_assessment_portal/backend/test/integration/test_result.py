import uuid
import pytest


class TestResultAPI:
    """
    Integration tests for the Result API.
    """

    category_id = None
    quiz_id = None
    question_1_id = None
    question_2_id = None
    attempt_id = None

    def _setup_and_submit_attempt(self, client, admin_headers, student_headers):
        """
        Helper that creates full chain and submits attempt.
        Returns attempt_id of submitted attempt.
        Used as setup so result tests have data to work with.
        """
        # create category
        cat = client.post(
            "/assessment/v1/categories/",
            json={
                "name": f"Result Test Cat {uuid.uuid4().hex[:8]}",
                "description": "Category for result tests"
            },
            headers=admin_headers
        )
        category_id = cat.json()["id"]

        # create quiz with pass_percentage 50
        quiz = client.post(
            "/assessment/v1/quizzes/",
            json={
                "title": f"Result Test Quiz {uuid.uuid4().hex[:8]}",
                "description": "Quiz for result tests",
                "category_id": category_id,
                "time_limit": 30,
                "pass_percentage": 50.0
            },
            headers=admin_headers
        )
        quiz_id = quiz.json()["id"]

        # create question 1 — correct answer A
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
                "marks": 1
            },
            headers=admin_headers
        )
        question_1_id = q1.json()["id"]

        # create question 2 — correct answer False
        q2 = client.post(
            "/assessment/v1/questions/",
            json={
                "quiz_id": quiz_id,
                "question_text": "Python is a compiled language",
                "question_type": "true_false",
                "options": ["True", "False"],
                "correct_answer": "False",
                "difficulty": "easy",
                "marks": 1
            },
            headers=admin_headers
        )
        question_2_id = q2.json()["id"]

        # start attempt
        attempt = client.post(
            "/assessment/v1/attempts/start",
            json={"quiz_id": quiz_id},
            headers=student_headers
        )
        attempt_id = attempt.json()["id"]

        # save answers
        client.post(
            f"/assessment/v1/attempts/{attempt_id}/answer",
            json={
                "question_id": question_1_id,
                "selected_answer": "A mutable sequence"
            },
            headers=student_headers
        )
        client.post(
            f"/assessment/v1/attempts/{attempt_id}/answer",
            json={
                "question_id": question_2_id,
                "selected_answer": "False"
            },
            headers=student_headers
        )

        # submit
        client.post(
            f"/assessment/v1/attempts/{attempt_id}/submit",
            headers=student_headers
        )

        return category_id, quiz_id, question_1_id, question_2_id, attempt_id

    # ── RES-001 ───────────────────────────────────────────────────────────────
    def test_res_001_result_generated_after_submission(
        self, client, admin_headers, student_headers
    ):
        """
        RES-001: Verifies result is generated and accessible
        after student submits a quiz attempt.
        Asserts result has required fields and attempt_id matches.
        """
        (
            TestResultAPI.category_id,
            TestResultAPI.quiz_id,
            TestResultAPI.question_1_id,
            TestResultAPI.question_2_id,
            TestResultAPI.attempt_id
        ) = self._setup_and_submit_attempt(
            client, admin_headers, student_headers
        )

        response = client.get(
            f"/assessment/v1/results/{TestResultAPI.attempt_id}",
            headers=student_headers
        )

        assert response.status_code == 200, \
            f"Expected 200 but got {response.status_code}. " \
            f"Response: {response.json()}"

        data = response.json()
        assert data["attempt_id"] == TestResultAPI.attempt_id
        assert "score" in data
        assert "total_marks" in data
        assert "percentage" in data
        assert "passed" in data
        assert "answer_breakdown" in data

    # ── RES-002 ───────────────────────────────────────────────────────────────
    def test_res_002_percentage_calculated_correctly(
        self, client, student_headers
    ):
        """
        RES-002: Verifies percentage is calculated correctly.
        Both questions answered correctly → score=2, total=2, percentage=100.0
        """
        response = client.get(
            f"/assessment/v1/results/{TestResultAPI.attempt_id}",
            headers=student_headers
        )

        data = response.json()

        assert data["score"] == 2, \
            f"Expected score 2 but got {data['score']}"
        assert data["total_marks"] == 2, \
            f"Expected total_marks 2 but got {data['total_marks']}"
        assert data["percentage"] == 100.0, \
            f"Expected 100.0% but got {data['percentage']}"

    # ── RES-003 ───────────────────────────────────────────────────────────────
    def test_res_003_pass_fail_based_on_threshold(
        self, client, student_headers
    ):
        """
        RES-003: Verifies pass/fail is determined correctly
        based on pass_percentage threshold set in quiz.
        Quiz pass_percentage = 50.0, student got 100% → passed = True
        """
        response = client.get(
            f"/assessment/v1/results/{TestResultAPI.attempt_id}",
            headers=student_headers
        )

        data = response.json()

        assert data["passed"] is True, \
            "Student scored 100% on a 50% threshold quiz — should have passed"
        assert data["pass_percentage"] == 50.0

    def test_res_003_fail_when_below_threshold(
        self, client, admin_headers, student_headers
    ):
        """
        RES-003 extended: Verifies student fails when score
        is below pass_percentage threshold.
        Creates a quiz with 100% pass_percentage, student answers one wrong.
        """
        # create quiz with 100% pass requirement
        cat = client.post(
            "/assessment/v1/categories/",
            json={
                "name": f"Fail Test Cat {uuid.uuid4().hex[:8]}",
                "description": "Category for fail test"
            },
            headers=admin_headers
        )
        quiz = client.post(
            "/assessment/v1/quizzes/",
            json={
                "title": f"Fail Test Quiz {uuid.uuid4().hex[:8]}",
                "description": "Quiz with high threshold",
                "category_id": cat.json()["id"],
                "time_limit": 30,
                "pass_percentage": 100.0     # must get everything right
            },
            headers=admin_headers
        )
        quiz_id = quiz.json()["id"]

        q1 = client.post(
            "/assessment/v1/questions/",
            json={
                "quiz_id": quiz_id,
                "question_text": "What is Python",
                "question_type": "mcq",
                "options": ["A language", "A snake", "A tool", "A framework"],
                "correct_answer": "A language",
                "difficulty": "easy",
                "marks": 1
            },
            headers=admin_headers
        )

        # start attempt
        attempt = client.post(
            "/assessment/v1/attempts/start",
            json={"quiz_id": quiz_id},
            headers=student_headers
        )
        attempt_id = attempt.json()["id"]

        # save WRONG answer
        client.post(
            f"/assessment/v1/attempts/{attempt_id}/answer",
            json={
                "question_id": q1.json()["id"],
                "selected_answer": "A snake"   # wrong answer
            },
            headers=student_headers
        )

        # submit
        client.post(
            f"/assessment/v1/attempts/{attempt_id}/submit",
            headers=student_headers
        )

        # check result
        response = client.get(
            f"/assessment/v1/results/{attempt_id}",
            headers=student_headers
        )

        data = response.json()
        assert data["passed"] is False, \
            "Student answered wrong on 100% threshold quiz — should have failed"
        assert data["score"] == 0
        assert data["percentage"] == 0.0

    # ── RES-004 ───────────────────────────────────────────────────────────────
    def test_res_004_fetch_student_result_history(
        self, client, student_headers
    ):
        """
        RES-004: Verifies student can fetch their full result history.
        Returns list with total count and result summaries.
        """
        response = client.get(
            "/assessment/v1/results/my",
            headers=student_headers
        )

        assert response.status_code == 200, \
            f"Expected 200 but got {response.status_code}"

        data = response.json()
        assert "total" in data
        assert "results" in data
        assert isinstance(data["results"], list)
        assert data["total"] >= 1, \
            "Student should have at least 1 result after submitting"

    def test_res_004_result_history_has_correct_fields(
        self, client, student_headers
    ):
        """
        RES-004 extended: Verifies result history items have
        the expected summary fields.
        """
        response = client.get(
            "/assessment/v1/results/my",
            headers=student_headers
        )

        data = response.json()
        first_result = data["results"][0]

        assert "attempt_id" in first_result
        assert "quiz_title" in first_result
        assert "score" in first_result
        assert "total_marks" in first_result
        assert "percentage" in first_result
        assert "passed" in first_result
        assert "submitted_at" in first_result
        # summary should NOT have answer_breakdown
        assert "answer_breakdown" not in first_result

    # ── RES-005 ───────────────────────────────────────────────────────────────
    def test_res_005_admin_can_view_all_results(
        self, client, admin_headers
    ):
        """
        RES-005: Verifies admin can access the result dashboard
        showing all students results.
        """
        response = client.get(
            "/assessment/v1/results/admin/all",
            headers=admin_headers
        )

        assert response.status_code == 200, \
            f"Expected 200 but got {response.status_code}"

        data = response.json()
        assert "total" in data
        assert "results" in data
        assert data["total"] >= 1

    def test_res_005_admin_can_filter_results_by_quiz(
        self, client, admin_headers
    ):
        """
        RES-005 extended: Verifies admin can filter results
        by a specific quiz id.
        """
        response = client.get(
            f"/assessment/v1/results/admin/quiz/{TestResultAPI.quiz_id}",
            headers=admin_headers
        )

        assert response.status_code == 200, \
            f"Expected 200 but got {response.status_code}"

        data = response.json()
        assert "total" in data
        assert data["total"] >= 1

    # ── RES-006 ───────────────────────────────────────────────────────────────
    def test_res_006_result_breakdown_per_question(
        self, client, student_headers
    ):
        """
        RES-006: Verifies the result contains per-question breakdown
        showing selected answer, correct answer, is_correct and marks.
        """
        response = client.get(
            f"/assessment/v1/results/{TestResultAPI.attempt_id}",
            headers=student_headers
        )

        data = response.json()
        breakdown = data["answer_breakdown"]

        assert len(breakdown) == 2, \
            "Breakdown should have one entry per question"

        for item in breakdown:
            assert "question_id" in item
            assert "question_text" in item
            assert "selected_answer" in item
            assert "correct_answer" in item, \
                "correct_answer must be revealed in result breakdown"
            assert "is_correct" in item
            assert "marks_obtained" in item
            assert "marks_possible" in item

    def test_res_006_correct_marks_in_breakdown(
        self, client, student_headers
    ):
        """
        RES-006 extended: Verifies marks_obtained is correct
        for each question in the breakdown.
        Both answered correctly → marks_obtained == marks_possible for each.
        """
        response = client.get(
            f"/assessment/v1/results/{TestResultAPI.attempt_id}",
            headers=student_headers
        )

        data = response.json()
        for item in data["answer_breakdown"]:
            assert item["is_correct"] is True
            assert item["marks_obtained"] == item["marks_possible"]

    # ── Security tests ────────────────────────────────────────────────────────

    def test_student_cannot_access_admin_results(
        self, client, student_headers
    ):
        """
        Verifies student is blocked from admin result endpoints.
        Confirms require_admin dependency is enforced.
        """
        response = client.get(
            "/assessment/v1/results/admin/all",
            headers=student_headers
        )

        assert response.status_code == 403, \
            f"Expected 403 but got {response.status_code}"

    def test_admin_cannot_access_student_result(
        self, client, admin_headers
    ):
        """
        Verifies admin is blocked from student result endpoint.
        Result details are student only.
        """
        response = client.get(
            f"/assessment/v1/results/{TestResultAPI.attempt_id}",
            headers=admin_headers
        )

        assert response.status_code == 403, \
            f"Expected 403 but got {response.status_code}"

    def test_result_not_found_for_invalid_attempt(
        self, client, student_headers
    ):
        """
        Verifies requesting result for non-existent attempt
        returns 404 Not Found.
        """
        response = client.get(
            "/assessment/v1/results/000000000000000000000000",
            headers=student_headers
        )

        assert response.status_code == 404, \
            f"Expected 404 but got {response.status_code}"

    def test_student_cannot_see_another_students_result(
    self, client, encrypt_password   # add encrypt_password here
):
        """
        Verifies student cannot access another student's result.
        Creates second student, tries to access first student's result.
        """
        second_email = f"result_test_{uuid.uuid4().hex[:8]}@test.com"
        client.post(
            "/assessment/v1/auth/register",
            json={
                "username": f"result_test_{uuid.uuid4().hex[:8]}",
                "email": second_email,
                "password": encrypt_password("Test@1234")   # was plain "Test@1234"
            }
        )
        login = client.post(
            "/assessment/v1/auth/login",
            json={"email": second_email, "password": encrypt_password("Test@1234")}   # was plain "Test@1234"
        )
        second_headers = {
            "Authorization": f"Bearer {login.json()['access_token']}"
        }

        # second student tries to access first student's result
        response = client.get(
            f"/assessment/v1/results/{TestResultAPI.attempt_id}",
            headers=second_headers
        )

        assert response.status_code == 404, \
            f"Expected 404 but got {response.status_code}"