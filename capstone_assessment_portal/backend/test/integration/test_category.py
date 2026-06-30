import uuid
import pytest

"""
Category Integration Tests

Tests the full API flow: Request → Route → Service → Repository → MongoDB → Response

Covers SRS test cases:
CAT-001, CAT-002, CAT-003, CAT-004, CAT-005, CAT-006, CAT-007, SEC-001
"""

# shared state between tests in this file — set by create test, used by others
created_category_id = None
created_category_name = None


class TestCategoryAPI:
    """
    Groups all category-related test cases together.
    """

    def test_create_category_success(self, client, admin_headers):
        """
        CAT-001: Verifies admin can create a category with valid data.
        Generates a unique name using uuid so repeated test runs
        never collide with leftover data from previous runs.
        Asserts 201 status, correct response fields, and is_active=True.
        Saves the created id and name for later tests to reuse.
        """
        global created_category_id, created_category_name

        unique_name = f"Test Python Category {uuid.uuid4().hex[:8]}"

        response = client.post(
            "/assessment/v1/categories/",
            json={
                "name": unique_name,
                "description": "Category created during automated testing"
            },
            headers=admin_headers
        )

        assert response.status_code == 201, \
            f"Expected 201 but got {response.status_code}. Response: {response.json()}"

        data = response.json()
        assert "id" in data
        assert data["name"] == unique_name
        assert data["description"] == "Category created during automated testing"
        assert data["is_active"] is True

        created_category_id = data["id"]
        created_category_name = unique_name

    def test_create_duplicate_category(self, client, admin_headers):
        """
        CAT-002: Verifies creating a category with a name that already
        exists is rejected. Reuses the exact name from the previous test
        to guarantee a real duplicate, and expects 409 Conflict.
        """
        response = client.post(
            "/assessment/v1/categories/",
            json={
                "name": created_category_name,
                "description": "This is intentionally a duplicate"
            },
            headers=admin_headers
        )

        assert response.status_code == 409, \
            f"Expected 409 but got {response.status_code}"

        data = response.json()
        assert "detail" in data

    def test_get_all_categories(self, client, admin_headers):
        """
        CAT-003: Verifies the list endpoint returns a properly
        structured response with 'total' and 'categories' fields,
        and that at least one category exists (the one we just created).
        """
        response = client.get(
            "/assessment/v1/categories/",
            headers=admin_headers
        )

        assert response.status_code == 200
        data = response.json()
        assert "total" in data
        assert "categories" in data
        assert isinstance(data["categories"], list)
        assert data["total"] >= 1

    def test_get_single_category_success(self, client, admin_headers):
        """
        Verifies fetching a single category by its ID returns
        the correct document with matching id and name.
        """
        response = client.get(
            f"/assessment/v1/categories/{created_category_id}",
            headers=admin_headers
        )

        assert response.status_code == 200, \
            f"Expected 200 but got {response.status_code}. Response: {response.json()}"

        data = response.json()
        assert data["id"] == created_category_id
        assert data["name"] == created_category_name

    def test_update_category_success(self, client, admin_headers):
        """
        CAT-004: Verifies admin can update an existing category's
        name and description, and that the changes are reflected
        in the response while the id stays the same.
        """
        updated_name = f"Updated Category {uuid.uuid4().hex[:8]}"

        response = client.put(
            f"/assessment/v1/categories/{created_category_id}",
            json={
                "name": updated_name,
                "description": "Description updated during testing"
            },
            headers=admin_headers
        )

        assert response.status_code == 200, \
            f"Expected 200 but got {response.status_code}. Response: {response.json()}"

        data = response.json()
        assert data["name"] == updated_name
        assert data["description"] == "Description updated during testing"
        assert data["id"] == created_category_id

    def test_update_nonexistent_category(self, client, admin_headers):
        """
        CAT-005: Verifies updating a category with a well-formed
        but non-existent ObjectId returns 404, not a server error.
        """
        fake_id = "000000000000000000000000"

        response = client.put(
            f"/assessment/v1/categories/{fake_id}",
            json={
                "name": "This Should Fail",
                "description": "Category does not exist in database"
            },
            headers=admin_headers
        )

        assert response.status_code == 404, \
            f"Expected 404 but got {response.status_code}"

    def test_student_cannot_create_category(self, client, student_headers):
        """
        SEC-001: Verifies a student (authenticated but not admin)
        is blocked from creating a category. Expects 403 Forbidden,
        confirming the require_admin dependency works correctly.
        """
        response = client.post(
            "/assessment/v1/categories/",
            json={
                "name": f"Student Attempt {uuid.uuid4().hex[:8]}",
                "description": "This should be blocked"
            },
            headers=student_headers
        )

        assert response.status_code == 403, \
            f"Expected 403 but got {response.status_code}"

    def test_no_token_returns_401_or_403(self, client):
        """
        AUTH-007: Verifies that hitting a protected endpoint with
        no Authorization header at all is rejected. Accepts either
        401 or 403 since the exact code can vary by FastAPI/Starlette
        version, but both correctly represent "not authenticated".
        """
        response = client.get("/assessment/v1/categories/")

        assert response.status_code in [401, 403], \
            f"Expected 401 or 403 but got {response.status_code}"

    def test_invalid_id_format(self, client, admin_headers):
        """
        Verifies that passing a malformed ID (not a valid MongoDB
        ObjectId) returns a clean 400 Bad Request instead of letting
        MongoDB throw an unhandled internal error.
        """
        response = client.get(
            "/assessment/v1/categories/abc-invalid-id",
            headers=admin_headers
        )

        assert response.status_code == 400, \
            f"Expected 400 but got {response.status_code}"

    def test_delete_category_success(self, client, admin_headers):
        """
        CAT-006: Verifies admin can soft delete a category.
        Expects 200 OK with a confirmation message containing
        the word 'deleted'. This must run before the next test
        so there is something valid to delete.
        """
        response = client.delete(
            f"/assessment/v1/categories/{created_category_id}",
            headers=admin_headers
        )

        assert response.status_code == 200, \
            f"Expected 200 but got {response.status_code}. Response: {response.json()}"

        data = response.json()
        assert "message" in data
        assert "deleted" in data["message"].lower()

    def test_delete_nonexistent_category(self, client, admin_headers):
        """
        CAT-007: Verifies that deleting the SAME category again
        (already soft-deleted in the previous test) returns 404,
        not a successful re-delete.
        """
        response = client.delete(
            f"/assessment/v1/categories/{created_category_id}",
            headers=admin_headers
        )

        assert response.status_code == 404, \
            f"Expected 404 but got {response.status_code}. Response: {response.json()}"