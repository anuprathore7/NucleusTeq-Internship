"""
Pytest test cases for prime number checking.
"""

from question_2 import is_prime


def test_prime_number() -> None:

    assert is_prime(13) is True


def test_non_prime_number() -> None:

    assert is_prime(12) is False


def test_one() -> None:

    assert is_prime(1) is False


def test_two() -> None:

    assert is_prime(2) is True