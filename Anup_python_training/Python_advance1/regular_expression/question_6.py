"""
Replace multiple spaces with a single space.
"""

import re

def remove_extra_spaces(text: str) -> str:
    """
    Convert multiple spaces into one space.
    """

    return re.sub(r"\s+", " ", text)


def main() -> None:

    sentence: str = (
        "Python     is      easy      to learn"
    )
    cleaned_text = remove_extra_spaces(
        sentence
    )

    print(cleaned_text)


"""Executes the program only when this file is run directly."""
if __name__ == "__main__":
    main()