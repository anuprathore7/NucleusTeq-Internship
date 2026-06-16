
""" This function appends additional data to an existing file. """
def append_data_to_file() -> None:

    with open("NucleusTeq-Internship/Anup_python_training/Python_basic/file_handling/student_details.txt","a") as file:

        file.write("\nPython Training Assignment")


""" Executes the program only when this file is run directly. """
if __name__ == "__main__":

    append_data_to_file()

    print("Data appended successfully.")