"""It is a built-in Python module that provides access to information and functionality related to the Python interpreter and the system running it.
"""
import sys

""" This function prints the version of Python currently installed on the system. """
def display_python_version() -> None:
    print(sys.version)

"""Executes the program only when this file is run directly """
if __name__ == "__main__":
    display_python_version()