""" This function prints numbers from 1 to 100 using a for loop. """
def print_numbers_from_1_to_100() -> None:

    for number in range(1, 101):
        print(number)


""" Executes the program only when this file is run directly. """
if __name__ == "__main__":

    print_numbers_from_1_to_100()