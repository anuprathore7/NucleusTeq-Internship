"""
Uses functions from the utility module.
"""

from utility_functions import (
    convert_to_uppercase,
    calculate_string_length
)


def main() -> None:
    """
    Program entry point.
    """

    user_name: str = "Anup"

    print(convert_to_uppercase(user_name))
    print(calculate_string_length(user_name))


if __name__ == "__main__":
    main()