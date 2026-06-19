
""" This function greets a user with a default message. """
def greet_user(user_name: str = "Anup") -> None:

    print(f"Hello {user_name}, welcome to Python Training")


""" Executes the program only when this file is run directly. """
if __name__ == "__main__":

    greet_user()

    greet_user("Alfez")