"""
Custom iterator that returns numbers from 1 to N.
"""


class NumberIterator:
    """
    Generates numbers from 1 to the given limit.
    """

    def __init__(self, limit: int) -> None:
        self.current_number: int = 1
        self.limit: int = limit

    def __iter__(self):
        return self
    """Returns the next number in the sequence or raises StopIteration when done.
        """
    def __next__(self) -> int:

        if self.current_number > self.limit:
            raise StopIteration

        number: int = self.current_number
        self.current_number += 1

        return number

"""Main function to demonstrate the NumberIterator."""
def main() -> None:

    input_no = int(input("Enter a number: "))
    numbers = NumberIterator(input_no)

    for number in numbers:
        print(number)

"""Executes the program only when this file is run directly."""
if __name__ == "__main__":
    main()