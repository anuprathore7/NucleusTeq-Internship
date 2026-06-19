"""
Checks whether a number is prime.
"""


def is_prime(number: int) -> bool:
    """
    Returns True if the number is prime.
    """

    if number < 2:
        return False

    for divisor in range(
        2,
        int(number ** 0.5) + 1
    ):
        if number % divisor == 0:
            return False

    return True