"""
This module demonstrates the use of Python's built-in math module
to perform mathematical operations like square root, power, and factorial.
"""

import math


""" This function calculates square root of a given number. """
def calculate_square_root(number: float) -> None:

    square_root: float = math.sqrt(number)

    print(f"Square root of {number} is {square_root}")


""" This function calculates power of a number. """
def calculate_power(base_number: float, exponent: float) -> None:

    power_result: float = math.pow(base_number,exponent)

    print(f"{base_number} raised to power {exponent} is {power_result}")


""" This function calculates factorial of a given number. """
def calculate_factorial(number: int) -> None:

    factorial_result: int = math.factorial(number)

    print(f"Factorial of {number} is {factorial_result}")


""" Executes the program only when this file is run directly. """
if __name__ == "__main__":

    calculate_square_root(25)

    calculate_power(base_number=2,exponent=5)

    calculate_factorial(5)