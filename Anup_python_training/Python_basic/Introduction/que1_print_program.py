
""" This function is used to print the string "welcome to python Training" when called. """
def for_print() -> str:
    return "welcome to python Training"

""" The main function is used to call the for_print function from another file so that it don't execute when the file is imported. """
if __name__ == "__main__":
    """ The print_string variable is used to store the string returned by the for_print function. """
    print_string = for_print()
    """ This is print function is used to print the string returned by the for_print function. """
    print(print_string)