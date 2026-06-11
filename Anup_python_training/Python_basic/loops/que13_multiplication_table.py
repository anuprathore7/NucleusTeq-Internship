""" This function prints the multiplication table of a given number. """
def print_multiplication_table(number: int) -> None:

    for multiplier in range(1, 11):
        print(f"{number} x {multiplier} = {number * multiplier}")


""" Executes the program only when this file is run directly. """
if __name__ == "__main__":

    number: int = int(input("Enter a number: "))

    print_multiplication_table(number)