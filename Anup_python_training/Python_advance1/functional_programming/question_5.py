"""
Calculate factorial using recursion.
"""


def factorial(number: int) -> int:
    """Base Case"""
    if number == 0 or number == 1:
        return 1
    """Recursive Case"""
    return number * factorial(number - 1)


def main() -> None:

    print(factorial(5))

"""Executes the program only when this file is run directly."""
if __name__ == "__main__":
    main()