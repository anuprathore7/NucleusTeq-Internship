import uuid
import pytest


class TestQuizAPI:
    """
    Integration tests for the Quiz API.

    QUIZ-001 → Create quiz with valid payload → 201 Created
    QUIZ-002 → Create quiz with invalid category id → error
    QUIZ-003 → Get quiz list → 200 with list
    QUIZ-004 → Update quiz with valid id → 200 Updated
    QUIZ-005 → Delete quiz with valid id → 200 Deleted
    QUIZ-006 → Fetch quiz details with valid id → 200 returned
    QUIZ-007 → Fetch quiz with invalid id → 404 Not Found
    
    """

    # shared state across test methods in this class
    category_id = None
    quiz_id = None
    quiz_title = None

    def _create_test_category(self, client, admin_headers) -> str:
        """
        Helper to create a category needed before any quiz can exist.
        Not a test itself — used as setup inside test methods.
        Returns the created category id.
        """
        unique_name = f"Quiz Test Category {uuid.uuid4().hex[:8]}"

        response = client.post(
            "/assessment/v1/categories/",
            json={
                "name": unique_name,
                "description": "Category created to support quiz tests"
            },
            headers=admin_headers
        )

        category_id = response.json()["id"]
        return category_id

    def test_create_quiz_success(self, client, admin_headers):
        """
        QUIZ-001: Verifies admin can create a quiz with a valid payload
        under an existing category. Asserts 201 status and that the
        response fields match what was submitted.
        """
        TestQuizAPI.category_id = self._create_test_category(client, admin_headers)

        unique_title = f"Test Quiz {uuid.uuid4().hex[:8]}"

        response = client.post(
            "/assessment/v1/quizzes/",
            json={
                "title": unique_title,
                "description": "Quiz created during automated testing",
                "category_id": TestQuizAPI.category_id,
                "time_limit": 30,
                "pass_percentage": 60.0
            },
            headers=admin_headers
        )

        assert response.status_code == 201, \
            f"Expected 201 but got {response.status_code}. Response: {response.json()}"

        data = response.json()
        assert "id" in data
        assert data["title"] == unique_title
        assert data["category_id"] == TestQuizAPI.category_id
        assert data["time_limit"] == 30
        assert data["pass_percentage"] == 60.0
        assert data["is_active"] is True

        TestQuizAPI.quiz_id = data["id"]
        TestQuizAPI.quiz_title = unique_title

    def test_create_quiz_with_invalid_category(self, client, admin_headers):
        """
        QUIZ-002: Verifies creating a quiz with a category_id that does
        not exist in the database is rejected with a 404 Not Found,
        since the quiz cannot be linked to a non-existent category.
        """
        fake_category_id = "000000000000000000000000"

        response = client.post(
            "/assessment/v1/quizzes/",
            json={
                "title": f"Orphan Quiz {uuid.uuid4().hex[:8]}",
                "description": "This quiz should not be created",
                "category_id": fake_category_id,
                "time_limit": 30,
                "pass_percentage": 60.0
            },
            headers=admin_headers
        )

        assert response.status_code == 404, \
            f"Expected 404 but got {response.status_code}. Response: {response.json()}"

        data = response.json()
        assert "detail" in data

    def test_create_quiz_with_malformed_category_id(self, client, admin_headers):
        """
        Edge case: category_id is not even a valid ObjectId format
        (not 24 hex characters). Verifies the API returns 400 Bad Request
        instead of crashing with an unhandled server error.
        """
        response = client.post(
            "/assessment/v1/quizzes/",
            json={
                "title": f"Bad Id Quiz {uuid.uuid4().hex[:8]}",
                "description": "Malformed category id test",
                "category_id": "not-a-valid-object-id",
                "time_limit": 30,
                "pass_percentage": 60.0
            },
            headers=admin_headers
        )

        assert response.status_code == 400, \
            f"Expected 400 but got {response.status_code}. Response: {response.json()}"

    def test_create_quiz_missing_required_fields(self, client, admin_headers):
        """
        Edge case: request body is missing required fields entirely.
        Pydantic validation should reject this before it reaches the
        service layer, returning 422 Unprocessable Entity.
        """
        response = client.post(
            "/assessment/v1/quizzes/",
            json={
                "title": "Incomplete Quiz"
                # description, category_id, time_limit, pass_percentage missing
            },
            headers=admin_headers
        )

        assert response.status_code == 422, \
            f"Expected 422 but got {response.status_code}"

    def test_get_quiz_list(self, client, admin_headers):
        """
        QUIZ-003: Verifies the quiz list endpoint returns a properly
        structured response with 'total' and 'quizzes' fields, and
        that at least one quiz exists (the one created earlier).
        """
        response = client.get(
            "/assessment/v1/quizzes/",
            headers=admin_headers
        )

        assert response.status_code == 200, \
            f"Expected 200 but got {response.status_code}"

        data = response.json()
        assert "total" in data
        assert "quizzes" in data
        assert isinstance(data["quizzes"], list)
        assert data["total"] >= 1

    def test_fetch_quiz_details_success(self, client, admin_headers):
        """
        QUIZ-006: Verifies fetching a single quiz by a valid id
        returns the correct quiz with matching id and title.
        """
        response = client.get(
            f"/assessment/v1/quizzes/{TestQuizAPI.quiz_id}",
            headers=admin_headers
        )

        assert response.status_code == 200, \
            f"Expected 200 but got {response.status_code}. Response: {response.json()}"

        data = response.json()
        assert data["id"] == TestQuizAPI.quiz_id
        assert data["title"] == TestQuizAPI.quiz_title

    def test_fetch_invalid_quiz(self, client, admin_headers):
        """
        QUIZ-007: Verifies fetching a quiz with a well-formed but
        non-existent ObjectId returns 404 Not Found, not a server error.
        """
        fake_quiz_id = "000000000000000000000000"

        response = client.get(
            f"/assessment/v1/quizzes/{fake_quiz_id}",
            headers=admin_headers
        )

        assert response.status_code == 404, \
            f"Expected 404 but got {response.status_code}"

    def test_fetch_quiz_with_malformed_id(self, client, admin_headers):
        """
        Edge case: quiz id is not a valid ObjectId format at all.
        Verifies a clean 400 Bad Request instead of an unhandled error.
        """
        response = client.get(
            "/assessment/v1/quizzes/not-a-valid-id",
            headers=admin_headers
        )

        assert response.status_code == 400, \
            f"Expected 400 but got {response.status_code}"

    def test_get_quizzes_by_category(self, client, admin_headers):
        """
        Verifies fetching quizzes filtered by category returns only
        quizzes belonging to that category, and the created quiz
        appears in the results.
        """
        response = client.get(
            f"/assessment/v1/quizzes/category/{TestQuizAPI.category_id}",
            headers=admin_headers
        )

        assert response.status_code == 200, \
            f"Expected 200 but got {response.status_code}"

        data = response.json()
        assert data["total"] >= 1

        quiz_ids = [q["id"] for q in data["quizzes"]]
        assert TestQuizAPI.quiz_id in quiz_ids

    def test_update_quiz_success(self, client, admin_headers):
        """
        QUIZ-004: Verifies admin can update an existing quiz's title
        and time_limit with a valid id, and that the changes are
        reflected in the response while the id stays the same.
        """
        updated_title = f"Updated Quiz {uuid.uuid4().hex[:8]}"

        response = client.put(
            f"/assessment/v1/quizzes/{TestQuizAPI.quiz_id}",
            json={
                "title": updated_title,
                "time_limit": 45
            },
            headers=admin_headers
        )

        assert response.status_code == 200, \
            f"Expected 200 but got {response.status_code}. Response: {response.json()}"

        data = response.json()
        assert data["title"] == updated_title
        assert data["time_limit"] == 45
        assert data["id"] == TestQuizAPI.quiz_id

        TestQuizAPI.quiz_title = updated_title

    def test_update_quiz_invalid_id(self, client, admin_headers):
        """
        Edge case: updating a quiz with a well-formed but non-existent
        id should return 404 Not Found.
        """
        fake_quiz_id = "000000000000000000000000"

        response = client.put(
            f"/assessment/v1/quizzes/{fake_quiz_id}",
            json={"title": "This Should Fail"},
            headers=admin_headers
        )

        assert response.status_code == 404, \
            f"Expected 404 but got {response.status_code}"

    def test_student_cannot_create_quiz(self, client, student_headers):
        """
        Verifies a student is blocked from creating a quiz,
        confirming the require_admin dependency is enforced.
        """
        response = client.post(
            "/assessment/v1/quizzes/",
            json={
                "title": f"Student Quiz {uuid.uuid4().hex[:8]}",
                "description": "Should be blocked",
                "category_id": TestQuizAPI.category_id,
                "time_limit": 30,
                "pass_percentage": 60.0
            },
            headers=student_headers
        )

        assert response.status_code == 403, \
            f"Expected 403 but got {response.status_code}"

    def test_delete_quiz_success(self, client, admin_headers):
        """
        QUIZ-005: Verifies admin can soft delete a quiz with a valid id.
        Expects 200 OK with a confirmation message. Runs near the end
        so the quiz remains available for the tests above.
        """
        response = client.delete(
            f"/assessment/v1/quizzes/{TestQuizAPI.quiz_id}",
            headers=admin_headers
        )

        assert response.status_code == 200, \
            f"Expected 200 but got {response.status_code}. Response: {response.json()}"

        data = response.json()
        assert "message" in data
        assert "deleted" in data["message"].lower()

    def test_delete_already_deleted_quiz(self, client, admin_headers):
        """
        Verifies deleting the SAME quiz again (already soft-deleted
        in the previous test) returns 404, confirming soft-deleted
        quizzes are correctly excluded from find_by_id lookups.
        """
        response = client.delete(
            f"/assessment/v1/quizzes/{TestQuizAPI.quiz_id}",
            headers=admin_headers
        )

        assert response.status_code == 404, \
            f"Expected 404 but got {response.status_code}"