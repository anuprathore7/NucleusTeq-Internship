""" This function reverses a number using a loop and prints the reversed number. """
def reverse_number(number: int) -> None:
    """This variable will hold the reversed number."""
    reversed_number: int = 0
    """ Loop continues until the number becomes 0. """
    while number > 0:
        """ Extract the last digit of the number. """
        last_digit: int = number % 10
        """ Append the last digit to the reversed number. """
        reversed_number = (reversed_number * 10) + last_digit
        """ Remove the last digit from the number. """
        number //= 10

    print(f"The reversed number is {reversed_number}")


""" Executes the program only when this file is run directly. """
if __name__ == "__main__":

    number: int = int(input("Enter a number: "))

    reverse_number(number)