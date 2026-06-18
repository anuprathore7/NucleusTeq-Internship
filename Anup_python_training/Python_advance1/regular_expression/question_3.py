"""
Validate a 10-digit mobile number."""
import re

MOBILE_PATTERN: str = r"^[6-9]\d{9}$"


def is_valid_mobile(number: str) -> bool:
    """
    Validate an Indian mobile number.

    Rules:
    - Must contain exactly 10 digits.
    - First digit must be between 6 and 9.
    """

    return bool(re.fullmatch(MOBILE_PATTERN, number))


def main() -> None:

    mobile_number: str = input(
        "Enter mobile number: "
    )

    if is_valid_mobile(mobile_number):
        print("Valid mobile number.")
    else:
        print("Invalid mobile number.")

"""Executes the program only when this file is run directly."""
if __name__ == "__main__":
    main()