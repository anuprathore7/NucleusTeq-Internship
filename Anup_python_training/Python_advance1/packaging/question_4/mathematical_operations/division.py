"""
Division operations.
"""


def divide_numbers(
    first_number: float,
    second_number: float
) -> float:
    """
    Return division result.
    """

    if second_number == 0:
        raise ValueError(
            "Division by zero is not allowed."
        )

    return first_number / second_number