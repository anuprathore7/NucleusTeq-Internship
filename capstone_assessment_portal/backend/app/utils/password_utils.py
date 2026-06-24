import bcrypt


def hash_password(plain_password: str) -> str:
    """
    Hash a plain text password using bcrypt.

    bcrypt.gensalt() generates a random salt each time —
    so the same password hashed twice gives two different hashes.

    encode() converts the string to bytes because
    bcrypt works with bytes, not strings.
    """
    password_bytes = plain_password.encode("utf-8")  # "Anup@123" → b"Anup@123"
    salt = bcrypt.gensalt()                          # random salt
    hashed = bcrypt.hashpw(password_bytes, salt)     # actual hashing
    return hashed.decode("utf-8")                    # bytes → string for MongoDB storage


def verify_password(plain_password: str, hashed_password: str) -> bool:
    """
    Check if a plain password matches a stored bcrypt hash.

    bcrypt.checkpw re-hashes the plain password with the
    salt embedded in hashed_password and compares the result.
    Returns True if they match, False otherwise.
    """
    password_bytes = plain_password.encode("utf-8")
    hashed_bytes = hashed_password.encode("utf-8")   # string → bytes for comparison
    return bcrypt.checkpw(password_bytes, hashed_bytes)