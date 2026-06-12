"""
This module contains functions to check whether
a number or string is palindrome.
"""


""" This function checks whether a given value is palindrome or not. """
def check_palindrome(input_string: str) -> bool:

    reversed_string: str = input_string[::-1]

    return input_string == reversed_string


""" Executes the program only when this file is run directly. """
if __name__ == "__main__":

    string: str = input("Enter a string or number: ")

    if check_palindrome(string):
        print(f"{string} is a Palindrome")

    else:
        print(f"{string} is not a Palindrome")