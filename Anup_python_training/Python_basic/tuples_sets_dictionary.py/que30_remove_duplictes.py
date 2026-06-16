
""" This function removes duplicate elements from a list. """
def remove_duplicates() -> None:

    number_list: list[int] = [10,20,20,30,40,40,50]
    """ Using set to remove duplicates and converting it back to a list. """
    unique_numbers: list[int] = list(set(number_list))

    print(f"List after removing duplicates: {unique_numbers}")


""" Executes the program only when this file is run directly. """
if __name__ == "__main__":

    remove_duplicates()