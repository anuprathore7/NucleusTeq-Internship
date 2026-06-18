"""
Convert normal execution into
parallel execution using ProcessPoolExecutor.
"""

from concurrent.futures import (
    ProcessPoolExecutor
)


def calculate_square(number: int) -> int:
    """
    Return square of a number.
    """

    return number ** 2


def main() -> None:

    numbers: list[int] = [
        1,
        2,
        3,
        4,
        5
    ]

    with ProcessPoolExecutor() as executor:

        results = executor.map(
            calculate_square,
            numbers
        )

        for result in results:
            print(result)

"""Executes the program only when this file is run directly."""
if __name__ == "__main__":
    main()