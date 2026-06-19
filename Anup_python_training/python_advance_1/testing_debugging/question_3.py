"""
Demonstrates debugging using pdb.
"""

import pdb


def calculate_average(
    total_marks: int,
    total_subjects: int
) -> float:
    """
    Returns average marks.
    """

    pdb.set_trace()

    average = total_marks * total_subjects

    return average


def main() -> None:

    result = calculate_average(500, 5)

    print(result)


if __name__ == "__main__":
    main()