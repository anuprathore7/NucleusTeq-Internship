"""
Demonstrates how to catch unexpected exceptions.
"""
def access_dict_key() -> None:
    """
    Attempts to access an invalid dictionary key.
    """
    try:
        student_marks: dict = {"Alice": 85, "Bob": 90, "Charlie": 78}
        print(student_marks["David"])

    except Exception as err:
        print(f"Something went wrong: {err}")

"""Executes the program only when this file is run directly."""
if __name__ == "__main__":
    access_dict_key()