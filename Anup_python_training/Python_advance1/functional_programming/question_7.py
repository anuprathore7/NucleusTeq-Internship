"""
Convert loop-based filtering into functional style.
"""


def main() -> None:

    numbers: list[int] = [1, 2, 3, 4, 5, 6]
    """Extract even numbers using filter() and a lambda function."""
    even_numbers = list(
        filter(
            lambda number: number % 2 == 0,
            numbers
        )
    )

    print(even_numbers)

"""Executes the program only when this file is run directly."""
if __name__ == "__main__":
    main()