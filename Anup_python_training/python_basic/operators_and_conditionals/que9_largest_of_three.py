""" This function finds and prints the largest number among three given numbers. """
def find_largest_number_among_three(first_number: int, second_number: int, third_number: int) -> None:
    """Max is used to find the largest number among three numbers."""
    largest_number: int = max(first_number,second_number,third_number)

    print(f"The largest number is {largest_number}")


""" Executes the program only when this file is run directly. """
if __name__ == "__main__":

    first_number: int = int(input("Enter first number: "))
    second_number: int = int(input("Enter second number: "))
    third_number: int = int(input("Enter third number: "))

    find_largest_number_among_three(first_number,second_number,third_number)