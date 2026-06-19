"""
Uses all mathematical operations
from the package.
"""

from mathematical_operations.addition import (
    add_numbers
)

from mathematical_operations.subtraction import (
    subtract_numbers
)

from mathematical_operations.multiplication import (
    multiply_numbers
)

from mathematical_operations.division import (
    divide_numbers
)


def main() -> None:
    """
    Program entry point.
    """

    first_number: float = 20
    second_number: float = 10

    print(
        f"Addition: "
        f"{add_numbers(first_number, second_number)}"
    )

    print(
        f"Subtraction: "
        f"{subtract_numbers(first_number, second_number)}"
    )

    print(
        f"Multiplication: "
        f"{multiply_numbers(first_number, second_number)}"
    )

    print(
        f"Division: "
        f"{divide_numbers(first_number, second_number)}"
    )


if __name__ == "__main__":
    main()