"""
Create two threads that print numbers
from 1 to 5 simultaneously.
"""

import threading


def print_numbers(thread_name: str) -> None:
    """
    Print numbers from 1 to 5.
    """

    for number in range(1, 6):
        print(f"{thread_name}: {number}")


def main() -> None:

    first_thread = threading.Thread(
        target=print_numbers,
        args=("Thread-1",)
    )

    second_thread = threading.Thread(
        target=print_numbers,
        args=("Thread-2",)
    )

    first_thread.start()
    second_thread.start()

"""Executes the program only when this file is run directly."""
if __name__ == "__main__":
    main()