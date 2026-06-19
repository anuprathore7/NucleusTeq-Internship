"""
Demonstrates how an iterator works with a list.
"""

def print_list_elements() -> None:
    """
    Create an iterator from a list
    and print elements using next().
    """

    student_names: list = ["Anup","Rahul","Aman"]

    names_iter = iter(student_names)

    print(next(names_iter))  # Output: Anup
    print(next(names_iter))  # Output: Rahul
    print(next(names_iter))  # Output: Aman

"""Executes the program only when this file is run directly."""
if __name__ == "__main__":
    print_list_elements()

