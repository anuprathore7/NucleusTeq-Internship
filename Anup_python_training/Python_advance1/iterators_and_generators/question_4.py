"""
Generate Fibonacci numbers using a generator.
"""


def fibonacci_generator(limit: int):
    """
    Yield Fibonacci numbers.
    """

    first_number: int = 0
    second_number: int = 1
    """Yield Fibonacci numbers up to the specified limit."""
    for _ in range(limit):
        yield first_number

        first_number, second_number = (
            second_number,
            first_number + second_number
        )

"""Main function to demonstrate the fibonacci_generator."""
def main() -> None:

    for number in fibonacci_generator(10):
        print(number)

"""Executes the program only when this file is run directly."""
if __name__ == "__main__":
    main()