"""This is used to return swapped values"""
def swap_numbers(first_no : int , second_no : int) -> tuple:
    return second_no, first_no

"""Executes the program only when this file is run directly """
if __name__ == "__main__":

    first_no : int = 10
    second_no : int = 20
    """Here we are printing values before swapping. """
    print("Before Swapping")
    print(f"First Number: {first_no}, Second Number: {second_no}")

    """Here the actual swapping is happening."""
    first_no , second_no = swap_numbers(first_no, second_no)

    """Here we are printing values after swapping. """
    print("After Swapping")
    print(f"First Number: {first_no}, Second Number: {second_no}")
