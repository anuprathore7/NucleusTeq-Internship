"""
Find the product of all list elements using reduce().
"""

from functools import reduce

def main() -> None:

    numbers: list[int] = [1, 2, 3, 4]
    """Find the product of all list elements using reduce() and a lambda function."""
    product: int = reduce(
        lambda first, second: first * second,
        numbers
    )

    print(product)

"""Executes the program only when this file is run directly."""
if __name__ == "__main__":
    main()