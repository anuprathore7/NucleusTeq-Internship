"""
NumPy matrix creation.

This module demonstrates how to create
a 3x3 matrix using NumPy.
"""

import numpy as np


def create_matrix() -> None:
    """
    Create and display a 3x3 matrix.
    """
    matrix = np.array(
        [
            [1, 2, 3],
            [4, 5, 6],
            [7, 8, 9]
        ]
    )

    print("3x3 Matrix:")
    print(matrix)


if __name__ == "__main__":
    create_matrix()