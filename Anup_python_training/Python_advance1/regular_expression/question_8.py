"""
Validate password strength using regex.
"""

import re

PASSWORD_PATTERN: str = (
    r"^(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{16,}$"
)


def is_valid_password(
    password: str
) -> bool:
    """
    Validate password based on
    length, digit and special character.
    """

    return bool(
        re.match(PASSWORD_PATTERN, password)
    )


def main() -> None:

    password: str = input(
        "Enter password: "
    )

    if is_valid_password(password):
        print("Password is valid.")
    else:
        print(
            "Password must contain at least "
            "16 characters, one digit and "
            "one special character."
        )

"""Executes the program only when this file is run directly."""
if __name__ == "__main__":
    main()