""" This function calculates and prints the factorial of a given number. """
def calculate_factorial(number: int) -> None:

    factorial_result: int = 1

    for current_number in range(1, number + 1):
        factorial_result *= current_number

    print(f"Factorial of {number} is {factorial_result}")


""" Executes the program only when this file is run directly. """
if __name__ == "__main__":

    number: int = int(input("Enter a number: "))

    calculate_factorial(number)