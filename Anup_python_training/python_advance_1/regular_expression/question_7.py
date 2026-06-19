"""
Check whether a string contains
only alphabetic characters.
"""

import re

ALPHABET_PATTERN: str = r"^[A-Za-z]+$"


def contains_only_alphabets(
    text: str
) -> bool:
    """
    Return True if the string contains
    only alphabets.
    """

    return bool(
        re.match(ALPHABET_PATTERN, text)
    )


def main() -> None:

    user_input: str = input(
        "Enter a string: "
    )

    if contains_only_alphabets(user_input):
        print("Only alphabets found.")
    else:
        print("String contains other characters.")

"""Executes the program only when this file is run directly."""
if __name__ == "__main__":
    main()