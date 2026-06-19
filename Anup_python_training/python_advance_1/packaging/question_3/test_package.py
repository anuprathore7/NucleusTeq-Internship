"""
Using modules from a package.
"""

from employee_package.employee import (
    get_employee_name
)

from employee_package.department import (
    get_department_name
)


def main() -> None:
    """
    Program entry point.
    """

    print(get_employee_name())
    print(get_department_name())


if __name__ == "__main__":
    main()