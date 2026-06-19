"""
Calculate square values using
multiple processes.
"""

import multiprocessing


def calculate_square(number: int) -> None:
    """
    Calculate and print square.
    """

    print(
        f"Square of {number}: "
        f"{number ** 2}"
    )


def main() -> None:

    processes: list = []

    numbers: list[int] = [
        2,
        4,
        6,
        8
    ]

    for number in numbers:

        process = multiprocessing.Process(
            target=calculate_square,
            args=(number,)
        )

        processes.append(process)

        process.start()

    for process in processes:
        process.join()

"""Executes the program only when this file is run directly."""
if __name__ == "__main__":
    main()