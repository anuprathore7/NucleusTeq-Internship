import uuid
import pytest
from fastapi.testclient import TestClient
from main import app


"""
conftest.py — root level shared fixtures for all test files.
"""


@pytest.fixture(scope="session")
def client():
    """
    Creates ONE TestClient shared across the entire test session.
    scope="session" = created once, used by every test file.

    We pass raise_server_exceptions=True so test failures show
    the real error, not a generic 500.
    """

    with TestClient(app, raise_server_exceptions=True) as test_client:
        yield test_client


@pytest.fixture(scope="session")
def admin_token(client):
    """
    Logs in as admin once for the entire test session.
    scope="session" = login happens once, token reused everywhere.
    """
    response = client.post(
        "/assessment/v1/auth/login",
        json={
            "email": "anup@gmail.com",
            "password": "Anup@123"
        }
    )

    assert response.status_code == 200, \
        f"Admin login failed. Response: {response.json()}"

    return response.json()["access_token"]


@pytest.fixture(scope="session")
def student_token(client):
    """
    Registers and logs in a student once for the entire test session.
    Uses uuid so it never conflicts with existing data.
    """
    unique_suffix = uuid.uuid4().hex[:8]
    unique_email = f"teststudent_{unique_suffix}@test.com"
    unique_username = f"teststudent_{unique_suffix}"

    # register — ignore if already exists from a previous run
    client.post(
        "/assessment/v1/auth/register",
        json={
            "username": unique_username,
            "email": unique_email,
            "password": "Test@1234"
        }
    )

    response = client.post(
        "/assessment/v1/auth/login",
        json={
            "email": unique_email,
            "password": "Test@1234"
        }
    )

    assert response.status_code == 200, \
        f"Student login failed. Response: {response.json()}"

    return response.json()["access_token"]


@pytest.fixture(scope="session")
def admin_headers(admin_token):
    """
    Authorization header dict for admin — built once, reused everywhere.
    """
    return {"Authorization": f"Bearer {admin_token}"}


@pytest.fixture(scope="session")
def student_headers(student_token):
    """
    Authorization header dict for student — built once, reused everywhere.
    """
    return {"Authorization": f"Bearer {student_token}"}