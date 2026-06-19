
""" This function reverses a list and displays the reversed list. """
def reverse_list(number_list: list[int]) -> None:

    reversed_list: list[int] = number_list[::-1]

    print(f"Reversed List: {reversed_list}")


""" Executes the program only when this file is run directly. """
if __name__ == "__main__":

    number_list: list[int] = [10,20,30,40,50]

    reverse_list(number_list)