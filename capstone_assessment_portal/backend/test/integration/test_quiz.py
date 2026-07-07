import uuid
import pytest


class TestQuizAPI:
    """
    Integration tests for the Quiz API.
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
    def test_get_quizzes_by_invalid_category(self, client, admin_headers):
        """
        Verifies fetching quizzes for a non-existent category id
        returns 404 Not Found instead of empty list.
        This hits get_quizzes_by_category service with missing category.
        """
        response = client.get(
            "/assessment/v1/quizzes/category/000000000000000000000000",
            headers=admin_headers
        )

        assert response.status_code == 404, \
            f"Expected 404 but got {response.status_code}"

    def test_get_quizzes_by_malformed_category_id(self, client, admin_headers):
        """
        Verifies fetching quizzes with a malformed category id
        returns 400 Bad Request instead of server error.
        This hits the _to_object_id conversion in repository.
        """
        response = client.get(
            "/assessment/v1/quizzes/category/not-a-valid-id",
            headers=admin_headers
        )

        assert response.status_code == 400, \
            f"Expected 400 but got {response.status_code}"

    def test_create_duplicate_quiz_title_in_same_category(
        self, client, admin_headers
    ):
        """
        Verifies creating two quizzes with the same title under the
        same category is rejected with 409 Conflict.
        This hits QuizAlreadyExistsException in quiz_service.create_quiz.
        """
        fresh_category_id = self._create_test_category(client, admin_headers)
        unique_title = f"Duplicate Quiz {uuid.uuid4().hex[:8]}"

        client.post(
            "/assessment/v1/quizzes/",
            json={
                "title": unique_title,
                "description": "First quiz",
                "category_id": fresh_category_id,
                "time_limit": 30,
                "pass_percentage": 60.0
            },
            headers=admin_headers
        )

        response = client.post(
            "/assessment/v1/quizzes/",
            json={
                "title": unique_title,
                "description": "Duplicate quiz same title",
                "category_id": fresh_category_id,
                "time_limit": 30,
                "pass_percentage": 60.0
            },
            headers=admin_headers
        )

        assert response.status_code == 409, \
            f"Expected 409 but got {response.status_code}"

    def test_same_title_different_category_is_allowed(
        self, client, admin_headers
    ):
        """
        Verifies same quiz title is allowed under a different category.
        Duplicate check is scoped to same category only.
        This hits the title + category duplicate logic in quiz_service.
        """
        category_one = self._create_test_category(client, admin_headers)
        category_two = self._create_test_category(client, admin_headers)
        shared_title = f"Shared Title Quiz {uuid.uuid4().hex[:8]}"

        response_one = client.post(
            "/assessment/v1/quizzes/",
            json={
                "title": shared_title,
                "description": "First category quiz",
                "category_id": category_one,
                "time_limit": 30,
                "pass_percentage": 60.0
            },
            headers=admin_headers
        )

        response_two = client.post(
            "/assessment/v1/quizzes/",
            json={
                "title": shared_title,
                "description": "Second category quiz same title",
                "category_id": category_two,
                "time_limit": 30,
                "pass_percentage": 60.0
            },
            headers=admin_headers
        )

        assert response_one.status_code == 201, \
            f"First quiz should succeed but got {response_one.status_code}"
        assert response_two.status_code == 201, \
            f"Same title in different category should be allowed but got {response_two.status_code}"

    def test_update_quiz_with_invalid_category_id(self, client, admin_headers):
        """
        Verifies updating a quiz with a non-existent category_id
        returns 404. This hits QuizCategoryNotFoundException
        in quiz_service.update_quiz when new category is checked.
        """
        fresh_category = self._create_test_category(client, admin_headers)
        quiz = client.post(
            "/assessment/v1/quizzes/",
            json={
                "title": f"Update Cat Quiz {uuid.uuid4().hex[:8]}",
                "description": "Quiz for update category test",
                "category_id": fresh_category,
                "time_limit": 30,
                "pass_percentage": 60.0
            },
            headers=admin_headers
        )
        quiz_id = quiz.json()["id"]

        response = client.put(
            f"/assessment/v1/quizzes/{quiz_id}",
            json={"category_id": "000000000000000000000000"},
            headers=admin_headers
        )

        assert response.status_code == 404, \
            f"Expected 404 but got {response.status_code}"

    def test_update_quiz_pass_percentage(self, client, admin_headers):
        """
        Verifies admin can update only pass_percentage of a quiz
        without affecting other fields.
        This hits the partial update logic in quiz_service.update_quiz.
        """
        fresh_category = self._create_test_category(client, admin_headers)
        quiz = client.post(
            "/assessment/v1/quizzes/",
            json={
                "title": f"Pass Pct Quiz {uuid.uuid4().hex[:8]}",
                "description": "Quiz for pass percentage update test",
                "category_id": fresh_category,
                "time_limit": 30,
                "pass_percentage": 60.0
            },
            headers=admin_headers
        )
        quiz_id = quiz.json()["id"]

        response = client.put(
            f"/assessment/v1/quizzes/{quiz_id}",
            json={"pass_percentage": 80.0},
            headers=admin_headers
        )

        assert response.status_code == 200, \
            f"Expected 200 but got {response.status_code}"

        data = response.json()
        assert data["pass_percentage"] == 80.0
        assert data["time_limit"] == 30

    def test_update_quiz_to_duplicate_title_in_same_category_returns_409(
        self, client, admin_headers
    ):
        """
        Verifies updating a quiz title to a title that already exists
        in the same category returns 409 Conflict.
        This hits the duplicate title check in quiz_service.update_quiz.
        """
        fresh_category = self._create_test_category(client, admin_headers)
        title_one = f"Title One {uuid.uuid4().hex[:8]}"
        title_two = f"Title Two {uuid.uuid4().hex[:8]}"

        client.post(
            "/assessment/v1/quizzes/",
            json={
                "title": title_one,
                "description": "First quiz",
                "category_id": fresh_category,
                "time_limit": 30,
                "pass_percentage": 60.0
            },
            headers=admin_headers
        )

        quiz_two = client.post(
            "/assessment/v1/quizzes/",
            json={
                "title": title_two,
                "description": "Second quiz",
                "category_id": fresh_category,
                "time_limit": 30,
                "pass_percentage": 60.0
            },
            headers=admin_headers
        )
        quiz_two_id = quiz_two.json()["id"]

        # try to update quiz_two title to title_one — should conflict
        response = client.put(
            f"/assessment/v1/quizzes/{quiz_two_id}",
            json={"title": title_one},
            headers=admin_headers
        )

        assert response.status_code == 409, \
            f"Expected 409 but got {response.status_code}"

    def test_student_cannot_update_quiz(self, client, student_headers):
        """
        Verifies student is blocked from updating a quiz.
        Confirms require_admin dependency is enforced on PUT route.
        """
        response = client.put(
            "/assessment/v1/quizzes/000000000000000000000000",
            json={"title": "Student Update Attempt"},
            headers=student_headers
        )

        assert response.status_code == 403, \
            f"Expected 403 but got {response.status_code}"

    def test_student_cannot_delete_quiz(self, client, student_headers):
        """
        Verifies student is blocked from deleting a quiz.
        Confirms require_admin dependency is enforced on DELETE route.
        This covers SEC-002 from SRS.
        """
        response = client.delete(
            "/assessment/v1/quizzes/000000000000000000000000",
            headers=student_headers
        )

        assert response.status_code == 403, \
            f"Expected 403 but got {response.status_code}"

    def test_delete_quiz_with_questions_returns_409(
        self, client, admin_headers
    ):
        """
        Verifies deleting a quiz that still has questions linked
        to it is blocked with 409 Conflict.
        This hits QuizHasQuestionsException in quiz_service.delete_quiz.
        Admin must delete all questions first before deleting the quiz.
        """
        fresh_category = self._create_test_category(client, admin_headers)
        quiz = client.post(
            "/assessment/v1/quizzes/",
            json={
                "title": f"Quiz With Questions {uuid.uuid4().hex[:8]}",
                "description": "Quiz that has questions linked",
                "category_id": fresh_category,
                "time_limit": 30,
                "pass_percentage": 60.0
            },
            headers=admin_headers
        )
        quiz_id = quiz.json()["id"]

        # add a question to this quiz
        client.post(
            "/assessment/v1/questions/",
            json={
                "quiz_id": quiz_id,
                "question_text": "Linked question for delete test",
                "question_type": "mcq",
                "options": ["A", "B", "C", "D"],
                "correct_answer": "A",
                "difficulty": "easy",
                "marks": 1
            },
            headers=admin_headers
        )

        # now try to delete the quiz — should be blocked
        response = client.delete(
            f"/assessment/v1/quizzes/{quiz_id}",
            headers=admin_headers
        )

        assert response.status_code == 409, \
            f"Expected 409 but got {response.status_code}. " \
            f"Should block delete when questions exist"

    def test_delete_quiz_with_malformed_id_returns_400(
        self, client, admin_headers
    ):
        """
        Verifies deleting a quiz with a malformed id
        returns 400 Bad Request.
        """
        response = client.delete(
            "/assessment/v1/quizzes/not-a-valid-id",
            headers=admin_headers
        )

        assert response.status_code == 400, \
            f"Expected 400 but got {response.status_code}"

    def test_student_can_view_quiz_list(self, client, student_headers):
        """
        Verifies student can access the quiz list endpoint.
        Quiz list is accessible by any authenticated user.
        """
        response = client.get(
            "/assessment/v1/quizzes/",
            headers=student_headers
        )

        assert response.status_code == 200, \
            f"Expected 200 but got {response.status_code}"

        data = response.json()
        assert "total" in data
        assert "quizzes" in data

    def test_student_can_view_quiz_by_id(self, client, admin_headers, student_headers):
        """
        Verifies student can fetch a single quiz by id.
        Quiz detail is accessible by any authenticated user.
        """
        fresh_category = self._create_test_category(client, admin_headers)
        quiz = client.post(
            "/assessment/v1/quizzes/",
            json={
                "title": f"Student View Quiz {uuid.uuid4().hex[:8]}",
                "description": "Quiz student can view",
                "category_id": fresh_category,
                "time_limit": 30,
                "pass_percentage": 60.0
            },
            headers=admin_headers
        )
        quiz_id = quiz.json()["id"]

        response = client.get(
            f"/assessment/v1/quizzes/{quiz_id}",
            headers=student_headers
        )

        assert response.status_code == 200, \
            f"Expected 200 but got {response.status_code}"

        data = response.json()
        assert data["id"] == quiz_id