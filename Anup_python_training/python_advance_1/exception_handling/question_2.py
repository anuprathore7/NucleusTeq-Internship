"""
Program to divide two numbers and handle ZeroDivisionError.
"""
def divide_numbers() -> None:
    """
    Takes two numbers and performs division.
    """
    try:
        first_number: float = float(input("Enter first number: "))
        second_number: float = float(input("Enter second number: "))

        result: float = first_number / second_number

        print(f"Result: {result}")

    except ZeroDivisionError:
        print("Division by zero is not allowed.")


"""Executes the program only when this file is run directly."""
if __name__ == "__main__":
    divide_numbers()