"""
NumPy array statistics operations.

This module demonstrates how to calculate
basic statistical values from a NumPy array.
"""

import numpy as np


def calculate_array_statistics() -> None:
    """
    Create a NumPy array and calculate
    mean, maximum, minimum, and sum.
    """
    numbers = np.array([10, 20, 30, 40, 50])

    print("Array:", numbers)
    print("Mean:", np.mean(numbers))
    print("Maximum:", np.max(numbers))
    print("Minimum:", np.min(numbers))
    print("Sum:", np.sum(numbers))


if __name__ == "__main__":
    calculate_array_statistics()