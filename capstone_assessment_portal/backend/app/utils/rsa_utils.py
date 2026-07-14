import base64
import os

from cryptography.hazmat.primitives import hashes
from cryptography.hazmat.primitives.asymmetric import padding
from cryptography.hazmat.primitives.serialization import load_pem_private_key
from cryptography.hazmat.primitives.serialization import load_pem_public_key


BASE_DIR = os.path.dirname(os.path.dirname(os.path.dirname(__file__)))

PRIVATE_KEY_PATH = os.path.join(
    BASE_DIR,
    "keys",
    "private_key.pem"
)

PUBLIC_KEY_PATH = os.path.join(
    BASE_DIR,
    "keys",
    "public_key.pem"
)


with open(PRIVATE_KEY_PATH, "rb") as file:
    PRIVATE_KEY = load_pem_private_key(
        file.read(),
        password=None
    )


with open(PUBLIC_KEY_PATH, "rb") as file:
    PUBLIC_KEY = load_pem_public_key(
        file.read()
    )


def decrypt_password(encrypted_password: str) -> str:
    """
    Decrypt password received from frontend.
    """

    encrypted_bytes = base64.b64decode(encrypted_password)

    decrypted = PRIVATE_KEY.decrypt(
        encrypted_bytes,
        padding.PKCS1v15()
    )

    return decrypted.decode("utf-8")


