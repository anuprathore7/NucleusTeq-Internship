"""
Read a number from a file and print its square.
"""
def read_number_from_file(file_path: str) -> int:
    try:
        with open(file_path, "r") as file:
            number: int = int(file.read())

    except ValueError:
        print("File contains invalid data.")
        return 0

    except FileNotFoundError:
        print("File not found.")
        return 0

    else:
        print(f"Square: {number ** 2}")
        return number

    finally:
        print("Program execution completed.")

"""Executes the program only when this file is run directly."""
if __name__ == "__main__":
    result: int = read_number_from_file("number.txt")
    print(f"Number from file: {result}")