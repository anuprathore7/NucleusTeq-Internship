
""" This function copies content from a source file to a destination file. """
def copy_file_content() -> None:

    with open("NucleusTeq-Internship/Anup_python_training/Python_basic/file_handling/student_details.txt","r")as source_file:

        file_content: str = source_file.read()

    with open("NucleusTeq-Internship/Anup_python_training/Python_basic/file_handling/backup_student_details.txt","w") as destination_file:

        destination_file.write(
            file_content
        )


""" Executes the program only when this file is run directly. """
if __name__ == "__main__":

    copy_file_content()

    print("File copied successfully.")