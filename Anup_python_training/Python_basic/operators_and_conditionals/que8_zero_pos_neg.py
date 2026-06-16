""" This function determines whether a number is positive, negative, or zero. """
def check_number_type(number: int) -> None:

    if number > 0:
        print(f"{number} is a Positive Number")

    elif number < 0:
        print(f"{number} is a Negative Number")

    else:
        print("The number is Zero")


""" Executes the program only when this file is run directly. """
if __name__ == "__main__":

    number: int = int(input("Enter a number: "))
    check_number_type(number)