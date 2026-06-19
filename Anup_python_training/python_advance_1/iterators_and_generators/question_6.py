"""
Comparison of iterators and generators in Python.
"""
class NumberIterator:
    """
    Iterator example.
    """

    def __init__(self) -> None:
        self.current_number: int = 1

    def __iter__(self):
        return self

    def __next__(self) -> int:

        if self.current_number > 3:
            raise StopIteration

        value: int = self.current_number
        self.current_number += 1

        return value


def number_generator():
    """
    Generator example.
    """

    yield 1
    yield 2
    yield 3


def main() -> None:

    print("Iterator Example")

    iterator = NumberIterator()

    for number in iterator:
        print(number)

    print("\nGenerator Example")

    generator = number_generator()

    for number in generator:
        print(number)


if __name__ == "__main__":
    main()