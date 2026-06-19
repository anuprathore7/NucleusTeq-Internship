""" This function creates variables of different data types and prints both their values and types. """
def print_data_types() -> None:

    student_age: int = 21
    student_CGPA: float = 82.4
    student_name: str = "Anup"
    is_eligible_for_placement: bool = True

    print(type(student_age))
    print(type(student_CGPA))
    print(type(student_name))
    print(type(is_eligible_for_placement))

"""Executes the program only when this file is run directly """
if __name__ == "__main__":
    print_data_types()