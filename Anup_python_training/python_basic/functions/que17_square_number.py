
""" This function calculates the square of a number and returns the result. """
def calculate_square(number: int) -> int:

    square_result: int = number * number

    return square_result


""" Executes the program only when this file is run directly. """
if __name__ == "__main__":

    number: int = int(input("Enter a number: "))

    result: int = calculate_square(number)

    print(f"Square of {number} is {result}")