"""
Pytest test cases for add_two_numbers().
"""

from question_1 import add_two_numbers


def test_positive_numbers() -> None:

    assert add_two_numbers(5, 10) == 15


def test_negative_numbers() -> None:

    assert add_two_numbers(-2, -3) == -5


def test_zero() -> None:

    assert add_two_numbers(0, 0) == 0