"""
Program to handle ValueError when user enters a non-integer value.
"""
def get_integer() -> int:
    """
    take input an integer from the user.
    Returns:
        check if a valid integer is entered by the user or not 
    """
    try:
        number: int = int(input("Enter an integer: "))
        return number

    except ValueError:
        print("Invalid input. Please enter a valid integer.")
        return 0


"""Executes the program only when this file is run directly."""
if __name__ == "__main__":
    number: int = get_integer()
    print(f"Entered Number: {number}")