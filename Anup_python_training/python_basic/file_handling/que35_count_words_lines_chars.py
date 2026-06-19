
""" This function reads a file and displays line, word, and character counts. """
def count_file_contents() -> None:

    with open("NucleusTeq-Internship/Anup_python_training/Python_basic/file_handling/student_details.txt","r", ) as file:

        file_content: str = file.read()

    total_lines: int = len(file_content.splitlines())

    total_words: int = len(file_content.split())

    total_characters: int = len(file_content)

    print(f"Total Lines: {total_lines}")

    print(f"Total Words: {total_words}")

    print(f"Total Characters: {total_characters}")


""" Executes the program only when this file is run directly. """
if __name__ == "__main__":

    count_file_contents()