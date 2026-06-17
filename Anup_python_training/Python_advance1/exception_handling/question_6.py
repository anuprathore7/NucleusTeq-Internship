""""""

def check_negative_number(number: int) -> None:
    """
    Raises a ValueError if the number is negative."""

    if number < 0:
        raise ValueError("Number cannot be negative")

def main() -> None:
    number : int = int(input("Enter a number: "))
    """Checking here the number is negative or not"""
    try:
        check_negative_number(number)
        print(f"Valid number: {number}")
    except ValueError as error:
        print(f"Error: {error}")

"""Executes the program only when this file is run directly."""
if __name__ == "__main__":
    main()
