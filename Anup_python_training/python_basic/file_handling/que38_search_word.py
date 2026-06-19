
""" This function searches for a given word in a file. """
def search_word_in_file(search_word: str) -> None:

    with open("NucleusTeq-Internship/Anup_python_training/Python_basic/file_handling/student_details.txt","r") as file:

        file_content: str = file.read()

    if search_word in file_content:

        print(f'"{search_word}" found in the file.')

    else:

        print(f'"{search_word}" not found in the file.')


""" Executes the program only when this file is run directly. """
if __name__ == "__main__":

    search_word: str = input("Enter the word to search: ")

    search_word_in_file(search_word)