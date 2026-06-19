"""This is a function which is responsible for creating tuple and displaying the created tuple."""

def create_tuple_and_display()->None:
    """ This variable will hold the created tuple. """
    my_tuple : tuple = ("Anshika" , "Anup" , "Alice" , "Alfez")

    """ Converting tuple to list. """
    my_list : list = list(my_tuple)

    print(f"Created tuple: {my_tuple}")
    print(f"Type of tuple : {type(my_tuple)}")
    print(f"Converted list: {my_list}")
    print(f"Type of converted list: {type(my_list)}")

""" Executes the program only when this file is run directly. """
if __name__ == "__main__":
    create_tuple_and_display()