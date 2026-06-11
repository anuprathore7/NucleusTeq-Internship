
""" This function calculates and displays the sum, difference, multiplication, and division of two numbers."""

def perform_arithmetic_operations(first_number: int, second_number: int) -> None:

    print(f"Sum: {first_number + second_number}")
    print(f"Difference: {first_number - second_number}")
    print(f"Multiplication: {first_number * second_number}")
    print(f"Division: {first_number / second_number}")

"""Executes the program only when this file is run directly """
if __name__ == "__main__":

    first_number: int = int(input("Enter the first number: "))

    second_number: int = int(input("Enter the second number: "))

    perform_arithmetic_operations(first_number, second_number)