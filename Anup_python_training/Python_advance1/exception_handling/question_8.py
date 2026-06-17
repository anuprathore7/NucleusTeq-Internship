"""
Reads a file and handles FileNotFoundError.
"""

FILE_NAME: str = "student_data.txt"


def read_file(file_name: str) -> None:
    """Reads and displays file content."""

    try:
        with open(file_name, "r") as file:
            content: str = file.read()

        print(content)

    except FileNotFoundError:
        print(f"File '{file_name}' was not found.")

"""Executes the program only when this file is run directly."""
if __name__ == "__main__":
    read_file(FILE_NAME)