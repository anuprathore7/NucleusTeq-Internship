
""" This function merges two dictionaries and displays the result. """
def merge_dictionaries() -> None:

    first_student_details: dict = {"name": "Anup"}

    second_student_details: dict = {"course": "Python"}
    """ Merging the two dictionaries using dictionary unpacking. """
    merged_dictionary: dict = {**first_student_details,**second_student_details}

    print(f"Merged Dictionary: {merged_dictionary}")


""" Executes the program only when this file is run directly. """
if __name__ == "__main__":
    merge_dictionaries()