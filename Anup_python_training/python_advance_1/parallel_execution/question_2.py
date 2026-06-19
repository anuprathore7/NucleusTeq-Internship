"""
Calculate the sum of numbers from 1 to 100
using a separate thread.
"""

import threading


def calculate_sum() -> None:
    """
    Calculate and display the sum.
    """

    total_sum: int = sum(range(1, 101))

    print(f"Sum: {total_sum}")


def main() -> None:

    calculation_thread = threading.Thread(
        target=calculate_sum
    )

    calculation_thread.start()
    calculation_thread.join()

"""Executes the program only when this file is run directly."""
if __name__ == "__main__":
    main()