"""
Program demonstrating multiple exception handling.
"""


def perform_multiple_exceptions() -> None:
    """
    Handles multiple exceptions.
    """
    try:
        number: int = int(input("Enter a number: "))
        result: float = 100 / number

        print(f"Result: {result}")

    except ValueError:
        print("Please enter a valid integer.")

    except ZeroDivisionError:
        print("Cannot divide by zero.")


"""Executes the program only when this file is run directly."""
if __name__ == "__main__":
    perform_multiple_exceptions()