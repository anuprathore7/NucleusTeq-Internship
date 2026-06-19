"""This"""
def dict_creation()->None:
    ""
    student_dict : dict = {"name": "Anup", "age": 21, "grade": "A"}

    print(f"Created dictionary: {student_dict}")

    print(f"Name : {student_dict['name']}")
    print(f"Age : {student_dict['age']}")
    print(f"Grade : {student_dict['grade']}")


""" Executes the program only when this file is run directly. """
if __name__ == "__main__":
    dict_creation()