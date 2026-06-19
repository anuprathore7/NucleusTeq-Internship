"""
Check whether a word exists in a sentence.
"""

import re


def main() -> None:

    sentence: str = (
        "Python is one of the most popular languages."
    )

    search_word: str = "Python"
    
    # search() finds the first occurrence
    result = re.search(search_word, sentence)

    if result:
        print("Word found.")
    else:
        print("Word not found.")

"""Executes the program only when this file is run directly."""
if __name__ == "__main__":
    main()