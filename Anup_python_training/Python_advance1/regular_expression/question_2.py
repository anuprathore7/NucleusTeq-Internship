"""
Validate email addresses using regex.
"""

import re

EMAIL_PATTERN: str = (
    r"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$"
)


def is_valid_email(email: str) -> bool:
    """
    Check whether an email is valid.
    """

    return bool(re.match(EMAIL_PATTERN, email))


def main() -> None:

    email: str = input("Enter email: ")

    if is_valid_email(email):
        print("Valid email address.")
    else:
        print("Invalid email address.")

"""Executes the program only when this file is run directly."""
if __name__ == "__main__":
    main()