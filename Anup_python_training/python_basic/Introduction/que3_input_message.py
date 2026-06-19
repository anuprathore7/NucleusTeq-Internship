
"""This function is used to get the input of user and print the message in formatted string"""
def get_user_information() -> None:
    """Here we are using input function to get the input of user and storing it in the variable user_name and user_age. """
    user_name : str = input("Please enter your name: ")
    user_age : int = int(input("Please enter your age: "))

    """This is a formatted string that is used to print the message with the user input. """
    print(f"Hello, My name is {user_name} and I am {user_age} years old.")

"""Executes the program only when this file is run directly """
if __name__ == "__main__":
    get_user_information()