"""
Find all words starting with a capital letter.
"""

import re

def extract_capital_words(
    text: str
) -> list[str]:
    """
    Return words that start
    with a capital letter.
    """

    return re.findall(r"\b[A-Z][a-zA-Z]*\b", text)


def main() -> None:

    sentence: str = (
        "Anup and Rahul visited Delhi and Mumbai."
    )

    words = extract_capital_words(sentence)

    print(words)

"""Executes the program only when this file is run directly."""
if __name__ == "__main__":
    main()