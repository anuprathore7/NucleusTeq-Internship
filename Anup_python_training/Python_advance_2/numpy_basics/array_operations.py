"""
NumPy arithmetic operations.

This module demonstrates addition
and multiplication of two arrays.
"""

import numpy as np


def perform_array_operations() -> None:
    """
    Perform element-wise addition
    and multiplication of two arrays.
    """
    arr_1 = np.array([1, 2, 3])
    arr_2 = np.array([4, 5, 6])

    addition_result = arr_1 + arr_2
    multiplication_result = arr_1 * arr_2

    print("First Array:", arr_1)
    print("Second Array:", arr_2)

    print("\nAddition Result:")
    print(addition_result)

    print("\nMultiplication Result:")
    print(multiplication_result)


if __name__ == "__main__":
    perform_array_operations()