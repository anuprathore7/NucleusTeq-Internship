"""
This module demonstrates the use of Python's random module
to generate random numbers.
"""

import random


""" This function generates a random integer within a given range. """
def generate_random_number( minimum_value: int, maximum_value: int) -> None:

    random_number: int = random.randint(minimum_value,maximum_value)

    print(f"Generated random number is {random_number}")


""" This function generates multiple random numbers. """
def generate_multiple_random_numbers(
    total_numbers: int
) -> None:

    random_numbers: list[int] = []

    for number in range(total_numbers):

        random_number: int = random.randint(1 ,100)

        random_numbers.append(random_number)

    print(f"Random numbers are {random_numbers}")


""" Executes the program only when this file is run directly. """
if __name__ == "__main__":

    generate_random_number(minimum_value=1,maximum_value=50)

    generate_multiple_random_numbers(total_numbers=5)