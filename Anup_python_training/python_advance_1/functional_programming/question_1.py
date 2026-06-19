"""
Find the square of a number using lambda.
"""


def main() -> None:
    """Find the square of a number using a lambda function."""
    square = lambda number: number ** 2

    print(square(5))

"""Executes the program only when this file is run directly."""
if __name__ == "__main__":
    main()