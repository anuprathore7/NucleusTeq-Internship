""" Age validation module """

"""This module provides functionality to validate a user's age and handle exceptions related to age validation."""
MINIMUM_AGE = 18

class AgeException(Exception):
    """Custom exception class for age validation errors."""
    pass


def validate_age(age: int)->None:
    """Validates the age and raises appropriate exceptions."""
    if age < MINIMUM_AGE:
        raise AgeException(f"Age must be at least {MINIMUM_AGE}. You entered {age}.")
    
    elif age > 120:
        raise ValueError("Age cannot be greater than 120.")

def main()->None:
    """Main function to execute age validation."""
    age: int = int(input("Enter your age: "))

    try:
        validate_age(age)
        print("Age verified successfully.")

    except AgeException as error:
        print(f"Error: {error}")

    except ValueError as error:
        print(f"Error: {error}")
        
"""Executes the program only when this file is run directly."""
if __name__ == "__main__":
    main()