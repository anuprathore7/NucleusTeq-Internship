"""
Generate square numbers up to N.
"""


def square_generator(limit: int):
    """Yield square values from 1 to N """

    for number in range(1, limit + 1):
        yield number ** 2

"""Main function to demonstrate the square_generator."""
def main() -> None:

    for square in square_generator(5):
        print(square)

"""Executes the program only when this file is run directly."""
if __name__ == "__main__":
    main()