
""" This function creates a file and writes a name into it. """
def write_name_to_file() -> None:

    with open("NucleusTeq-Internship/Anup_python_training/Python_basic/file_handling/student_details.txt","w") as file:

        file.write("Anup Rathore")


""" Executes the program only when this file is run directly. """
if __name__ == "__main__":

    write_name_to_file()

    print("Name written successfully.")