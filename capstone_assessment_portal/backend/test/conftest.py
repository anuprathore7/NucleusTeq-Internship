import pytest
from fastapi.testclient import TestClient
from main import app

"""
conftest.py — Special pytest file

pytest automatically reads this file BEFORE running any test.
we never import conftest.py manually — pytest does it for us.

FIXTURES — reusable setup code shared across ALL test files.

In our case:
- client fixture    → creates a fake HTTP client to call our API
- admin_token       → logs in as admin, returns JWT token
- student_token     → logs in as student, returns JWT token
- admin_headers     → builds the Authorization header for admin
- student_headers   → builds the Authorization header for student
"""


@pytest.fixture(scope="module")
def client():
    """
    Creates a TestClient for our FastAPI app.

    TestClient is like a FAKE BROWSER.
    It calls our API directly in memory — no real HTTP request.
    No need to run uvicorn separately.

    We use "module" so we don't create a new client for every test.
    """
    with TestClient(app) as test_client:
        # yield means: "give this to the test, then come back here after"
        # everything BEFORE yield = setup
        # everything AFTER yield = teardown (cleanup)
        yield test_client
        # TestClient closes cleanly after all tests in module finish


@pytest.fixture(scope="module")
def admin_token(client):
    """
    Logs in as admin and returns the access token.

    admin_token takes 'client' as a parameter.
    This means admin_token DEPENDS ON client fixture.
    """
    response = client.post(
        "/assessment/v1/auth/login",
        json={
            "email": "anup@gmail.com",
            "password": "Anup@123"
        }
    )

    # make sure login worked before extracting token
    assert response.status_code == 200, \
        f"Admin login failed! Check email/password. Got: {response.json()}"

    # extract and return just the token string
    return response.json()["access_token"]


@pytest.fixture(scope="module")
def student_token(client):
    """
    Creates a test student account and returns their token.
    """

    # register a test student
    # ignore if already exists (email conflict) — might be from previous run
    client.post(
        "/assessment/v1/auth/register",
        json={
            "username": "test_student_user",
            "email": "teststudent@testmail.com",
            "password": "Test@1234"
        }
    )

    # login to get token
    response = client.post(
        "/assessment/v1/auth/login",
        json={
            "email": "teststudent@testmail.com",
            "password": "Test@1234"
        }
    )

    assert response.status_code == 200, \
        f"Student login failed! Got: {response.json()}"

    return response.json()["access_token"]


@pytest.fixture(scope="module")
def admin_headers(admin_token):
    """
    Builds the Authorization header dict for admin requests.

    Every protected API call needs this header:
    Authorization: Bearer eyJhbGci...

    With this fixture:
    def test_something(self, client, admin_headers):
    → admin_headers already has the dict ready
    """
    return {"Authorization": f"Bearer {admin_token}"}


@pytest.fixture(scope="module")
def student_headers(student_token):
    """
    Builds the Authorization header dict for student requests.
    Used to test that students CANNOT access admin-only endpoints.
    """
    return {"Authorization": f"Bearer {student_token}"}