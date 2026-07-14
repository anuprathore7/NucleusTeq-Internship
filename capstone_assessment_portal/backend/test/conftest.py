import base64
import uuid
import pytest
from fastapi.testclient import TestClient
from cryptography.hazmat.primitives.asymmetric import padding
from cryptography.hazmat.primitives.serialization import load_pem_public_key

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
def rsa_public_key(client):
    """
    Fetches the RSA public key exposed by GET /auth/public-key and
    loads it into a usable key object. The backend now requires every
    password sent to /auth/register and /auth/login to be RSA-encrypted
    with this key and base64-encoded — plain text passwords fail with
    a decryption error, not a validation error.
    """
    response = client.get("/assessment/v1/auth/public-key")

    assert response.status_code == 200, \
        f"Failed to fetch RSA public key. Response: {response.json()}"

    # note: the backend's response key is "public_Key" (capital K) — must match exactly
    public_key_pem = response.json()["public_Key"]

    return load_pem_public_key(public_key_pem.encode("utf-8"))


@pytest.fixture(scope="session")
def encrypt_password(rsa_public_key):
    """
    Returns a helper function that RSA-encrypts and base64-encodes a
    plain text password, exactly matching what the frontend does
    before sending passwords to the backend. Use this in any test
    that needs a password to successfully pass through
    app.utils.rsa_utils.decrypt_password() on the server.

    Usage inside a test:
        json={"email": "...", "password": encrypt_password("Test@1234")}
    """

    def _encrypt(plain_password: str) -> str:
        encrypted_bytes = rsa_public_key.encrypt(
            plain_password.encode("utf-8"),
            padding.PKCS1v15()
        )
        return base64.b64encode(encrypted_bytes).decode("utf-8")

    return _encrypt


@pytest.fixture(scope="session")
def admin_token(client, encrypt_password):
    """
    Logs in as admin once for the entire test session.
    scope="session" = login happens once, token reused everywhere.
    """
    response = client.post(
        "/assessment/v1/auth/login",
        json={
            "email": "anup@gmail.com",
            "password": encrypt_password("Anup@123")
        }
    )

    assert response.status_code == 200, \
        f"Admin login failed. Response: {response.json()}"

    return response.json()["access_token"]


@pytest.fixture(scope="session")
def student_token(client, encrypt_password):
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
            "password": encrypt_password("Test@1234")
        }
    )

    response = client.post(
        "/assessment/v1/auth/login",
        json={
            "email": unique_email,
            "password": encrypt_password("Test@1234")
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