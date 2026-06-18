"""
Extract all numbers from a string using regex.
"""

import re

def extract_numbers(text: str) -> list:
    """
    Find all numbers present in the given text.
    """

    # \d+ means one or more digits
    return re.findall(r"\d+", text)


def main() -> None:

    sample_text: str = (
        "Anup is 21 years old and scored 95 marks."
    )

    numbers = extract_numbers(sample_text)

    print(numbers)

"""Executes the program only when this file is run directly."""
if __name__ == "__main__":
    main()