"""This is a function which is responsible for creating tuple and displaying the created tuple."""

def create_tuple_and_display()->None:
    """ This variable will hold the created tuple. """
    my_tuple : tuple = (10 , "Anup" , 3.14 , True)

    print(f"Created tuple: {my_tuple}")
    
    """ Accessing and displaying each element of the tuple. """
    print(f"First element of the tuple: {my_tuple[0]}")
    print(f"Second element of the tuple: {my_tuple[1]}")
    print(f"Third element of the tuple: {my_tuple[2]}")
    print(f"Fourth element of the tuple: {my_tuple[3]}")

""" Executes the program only when this file is run directly. """
if __name__ == "__main__":
    create_tuple_and_display()