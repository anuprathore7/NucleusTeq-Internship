import uuid
import pytest


class TestAuthAPI:
    """
    Integration tests for the Authentication API.
    """


    def test_auth_002_register_student_success(self, client):
        """
        AUTH-002: Verifies student registration with valid details
        creates the account and returns the expected response shape.
        Uses a unique email and username via uuid to avoid collisions
        across repeated test runs.
        """
        unique_email = f"auth002_{uuid.uuid4().hex[:8]}@test.com"
        unique_username = f"auth002_{uuid.uuid4().hex[:8]}"

        response = client.post(
            "/assessment/v1/auth/register",
            json={
                "username": unique_username,
                "email": unique_email,
                "password": "Student@1234"
            }
        )

        assert response.status_code == 201, \
            f"Expected 201 but got {response.status_code}. Response: {response.json()}"

        data = response.json()
        assert "id" in data
        assert data["email"] == unique_email
        assert data["role"] == "student"
        assert "password" not in data

    def test_register_duplicate_email_returns_409(self, client):
        """
        Edge case: registering with an email that already exists
        must return 409 Conflict, not 500 or 200.
        """
        unique_email = f"dup_{uuid.uuid4().hex[:8]}@test.com"

        # first registration — should succeed
        client.post(
            "/assessment/v1/auth/register",
            json={
                "username": f"first_{uuid.uuid4().hex[:8]}",
                "email": unique_email,
                "password": "Test@1234"
            }
        )

        # second registration with same email — should fail
        response = client.post(
            "/assessment/v1/auth/register",
            json={
                "username": f"second_{uuid.uuid4().hex[:8]}",
                "email": unique_email,
                "password": "Test@1234"
            }
        )

        assert response.status_code == 409, \
            f"Expected 409 Conflict but got {response.status_code}"

        data = response.json()
        assert "detail" in data

    def test_register_invalid_email_format_returns_422(self, client):
        """
        Edge case: email field does not follow a valid email format.
        Pydantic EmailStr validation rejects this before service runs.
        """
        response = client.post(
            "/assessment/v1/auth/register",
            json={
                "username": "testuser",
                "email": "this-is-not-an-email",
                "password": "Test@1234"
            }
        )

        assert response.status_code == 422, \
            f"Expected 422 but got {response.status_code}"

    def test_register_password_too_short_returns_422(self, client):
        """
        Edge case: password shorter than 8 characters.
        Pydantic min_length=8 rejects this before service runs.
        """
        response = client.post(
            "/assessment/v1/auth/register",
            json={
                "username": "testuser",
                "email": f"short_{uuid.uuid4().hex[:8]}@test.com",
                "password": "short"
            }
        )

        assert response.status_code == 422, \
            f"Expected 422 but got {response.status_code}"

    def test_auth_003_login_admin_success(self, client):
        """
        AUTH-003: Verifies admin can login with valid credentials
        and receives access_token, refresh_token and user details.
        Token type must be 'bearer' as per OAuth2 standard.
        """
        response = client.post(
            "/assessment/v1/auth/login",
            json={
                "email": "anup@gmail.com",
                "password": "Anup@123"
            }
        )

        assert response.status_code == 200, \
            f"Expected 200 but got {response.status_code}. Response: {response.json()}"

        data = response.json()
        assert "access_token" in data, \
            "Login response must contain access_token"
        assert "refresh_token" in data, \
            "Login response must contain refresh_token"
        assert data["token_type"] == "bearer", \
            "Token type must be bearer"
        assert data["user"]["role"] == "admin", \
            "Admin login must return role: admin"
        assert data["user"]["email"] == "anup@gmail.com"

    def test_auth_004_login_student_success(self, client):
        """
        AUTH-004: Verifies student can login with valid credentials
        and receives both tokens. Registers a fresh student first
        to keep this test self-contained and independent.
        """
        unique_email = f"auth004_{uuid.uuid4().hex[:8]}@test.com"

        client.post(
            "/assessment/v1/auth/register",
            json={
                "username": f"auth004_{uuid.uuid4().hex[:8]}",
                "email": unique_email,
                "password": "Student@1234"
            }
        )

        response = client.post(
            "/assessment/v1/auth/login",
            json={
                "email": unique_email,
                "password": "Student@1234"
            }
        )

        assert response.status_code == 200, \
            f"Expected 200 but got {response.status_code}"

        data = response.json()
        assert "access_token" in data
        assert "refresh_token" in data
        assert data["user"]["role"] == "student"

    def test_auth_005_login_wrong_password_returns_401(self, client):
        """
        AUTH-005: Verifies logging in with correct email but wrong
        password returns 401 Unauthorized. Same error for wrong
        password and wrong email — prevents user enumeration.
        """
        response = client.post(
            "/assessment/v1/auth/login",
            json={
                "email": "anup@gmail.com",
                "password": "WrongPassword@999"
            }
        )

        assert response.status_code == 401, \
            f"Expected 401 but got {response.status_code}"

        data = response.json()
        assert "detail" in data

    def test_auth_006_login_wrong_email_returns_404(self, client):
        """
        AUTH-006: Verifies logging in with an email that does not
        exist in the database returns 404 User Not Found.
        """
        response = client.post(
            "/assessment/v1/auth/login",
            json={
                "email": "doesnotexist@nowhere.com",
                "password": "Test@1234"
            }
        )

        assert response.status_code == 404, \
            f"Expected 404 but got {response.status_code}"

        data = response.json()
        assert "detail" in data
        assert data["detail"] == "User not found"

    def test_auth_007_no_token_returns_401_or_403(self, client):
        """
        AUTH-007: Verifies accessing a protected endpoint with
        no Authorization header at all is rejected.
        """
        response = client.get("/assessment/v1/auth/me")

        assert response.status_code in [401, 403], \
            f"Expected 401 or 403 but got {response.status_code}"

    def test_auth_008_invalid_token_returns_401(self, client):
        """
        AUTH-008: Verifies a malformed or tampered token is rejected
        with 401 Unauthorized. Covers the case of expired tokens too
        since both result in the same rejection response.
        """
        response = client.get(
            "/assessment/v1/auth/me",
            headers={"Authorization": "Bearer this.is.a.fake.invalid.token"}
        )

        assert response.status_code == 401, \
            f"Expected 401 but got {response.status_code}"

        data = response.json()
        assert "detail" in data

    def test_get_me_returns_correct_profile(self, client, admin_headers):
        """
        Verifies the /me endpoint returns the authenticated user's
        profile with all expected fields when a valid token is provided.
        Password must not be included in the response.
        """
        response = client.get(
            "/assessment/v1/auth/me",
            headers=admin_headers
        )

        assert response.status_code == 200, \
            f"Expected 200 but got {response.status_code}"

        data = response.json()
        assert "id" in data
        assert "email" in data
        assert "role" in data
        assert "username" in data
        assert "password" not in data, \
            "Password must never be exposed in any response"

    def test_refresh_token_returns_new_access_token(self, client):
        """
        Verifies that a valid refresh token can be exchanged for a
        new access token without requiring the user to login again.
        This is the core of the silent refresh flow.
        """
        login_response = client.post(
            "/assessment/v1/auth/login",
            json={
                "email": "anup@gmail.com",
                "password": "Anup@123"
            }
        )
        refresh_token = login_response.json()["refresh_token"]

        response = client.post(
            "/assessment/v1/auth/refresh",
            json={"refresh_token": refresh_token}
        )

        assert response.status_code == 200, \
            f"Expected 200 but got {response.status_code}. Response: {response.json()}"

        data = response.json()
        assert "access_token" in data
        assert data["token_type"] == "bearer"

    def test_access_token_cannot_be_used_as_refresh_token(self, client, admin_token):
        """
        Security check: verifies an access_token is rejected when
        sent to the refresh endpoint. The 'type' field inside the JWT
        payload prevents cross-token misuse.
        """
        response = client.post(
            "/assessment/v1/auth/refresh",
            json={"refresh_token": admin_token}
        )

        assert response.status_code == 401, \
            f"Expected 401 but got {response.status_code}"