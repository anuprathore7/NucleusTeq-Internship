
""" This function returns the largest number from the given list. """
def find_maximum_number(numbers: list[int]) -> int:

    maximum_number: int = max(numbers)

    return maximum_number


""" Executes the program only when this file is run directly. """
if __name__ == "__main__":

    number_list: list[int] = [16,45,15,90,5]

    result: int = find_maximum_number(number_list)

    print(f"Maximum number is {result}")