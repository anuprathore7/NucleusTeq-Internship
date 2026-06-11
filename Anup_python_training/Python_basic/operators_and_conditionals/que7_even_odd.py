""" This function checks whether a given number is even or odd. """
def check_even_odd(number : int) -> bool:
    """This is logic for checking even or odd"""
    if number % 2==0:
        return True
    else:
        return False
""" Executes the program only when this file is run directly. """
if __name__ == "__main__":
    number : int = int(input("Enter a number: "))
    result =check_even_odd(number)
    if result:
        print(f"{number} is an even number.")
    else:
        print(f"{number} is an odd number.")


