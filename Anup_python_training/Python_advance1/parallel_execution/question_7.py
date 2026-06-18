"""
Convert normal execution into
parallel execution using ThreadPoolExecutor.
"""

from concurrent.futures import (
    ThreadPoolExecutor
)
import time


def process_task(number: int) -> int:
    """
    Simulate processing.
    """

    time.sleep(1)

    return number * 2


def main() -> None:

    numbers: list[int] = [
        1,
        2,
        3,
        4,
        5
    ]

    with ThreadPoolExecutor(
        max_workers=3
    ) as executor:

        results = executor.map(
            process_task,
            numbers
        )

        for result in results:
            print(result)

"""Executes the program only when this file is run directly."""
if __name__ == "__main__":
    main()