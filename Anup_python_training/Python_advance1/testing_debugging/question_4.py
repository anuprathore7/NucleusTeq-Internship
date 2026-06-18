"""
Inspect variable values inside a loop.
"""

import pdb


def main() -> None:

    numbers: list[int] = [10, 20, 30, 40]

    for number in numbers:

        # Pause execution during every iteration
        pdb.set_trace()

        result = number * 2

        print(result)


if __name__ == "__main__":
    main()