""" This function checks whether a given number is prime or not. """
def check_prime_number(number: int) -> None:
    """IT is  index which holds the count of divisors of the number."""
    index : int = 0
    """ Loop to check for divisors from 2 to the number itself. """
    for divisor in range(2, number+1):
        if number % divisor == 0:
            index += 1
    """ If index is 1, it means the number has only one divisor (itself), hence it's prime. """
    if index == 1:
        print(f"{number} is a prime number.")
    else:
        print(f"{number} is not a prime number.")


""" Executes the program only when this file is run directly. """
if __name__ == "__main__":

    number: int = int(input("Enter a number: "))

    check_prime_number(number)