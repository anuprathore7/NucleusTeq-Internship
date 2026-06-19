"""
Generate even numbers from 1 to 50
using a generator expression.
"""


def main() -> None:
    """Generate even numbers from 1 to 50 using a generator expression."""
    even_numbers = ( number
        for number in range(1, 51)
        if number % 2 == 0
    )

    for number in even_numbers:
        print(number)

"""Executes the program only when this file is run directly."""
if __name__ == "__main__":
    main()