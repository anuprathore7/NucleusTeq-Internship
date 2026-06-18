"""
Create two processes and display
their process IDs.
"""

import multiprocessing
import os


def print_process_id() -> None:
    """
    Print the current process ID.
    """

    print(
        f"Process ID: {os.getpid()}"
    )


def main() -> None:

    process_one = multiprocessing.Process(
        target=print_process_id
    )

    process_two = multiprocessing.Process(
        target=print_process_id
    )

    process_one.start()
    process_two.start()

    process_one.join()
    process_two.join()

"""Executes the program only when this file is run directly."""
if __name__ == "__main__":
    main()