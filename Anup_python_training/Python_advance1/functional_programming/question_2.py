"""
Convert numbers into squares using map().
"""

def main() -> None:

    numbers: list[int] = [1, 2, 3, 4, 5]
    """Convert numbers into squares using map() and a lambda function."""
    square_numbers = list(
        map(
            lambda number: number ** 2,
            numbers
        )
    )

    print(square_numbers)

"""Executes the program only when this file is run directly."""
if __name__ == "__main__":
    main()