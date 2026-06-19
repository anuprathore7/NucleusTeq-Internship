"""
Employee Data Analysis.

This module demonstrates how to use
Pandas GroupBy operations to analyze
employee data.
"""

from typing import Any

import pandas as pd


def create_employee_dataframe() -> pd.DataFrame:
    """
    Create an employee DataFrame.

    Returns:
        pd.DataFrame: Employee dataset.
    """
    employee_data: dict[str, list[Any]] = {
        "Name": ["Rahul", "Priya", "Amit", "Anuj"],
        "Age": [25, 30, 28, 35],
        "Department": ["HR", "IT", "Finance", "IT"],
        "Salary": [30000, 50000, 45000, 60000]
    }

    return pd.DataFrame(employee_data)


def calculate_average_salary(
    employee_dataframe: pd.DataFrame
) -> pd.Series:
    """
    Calculate average salary by department.

    Args:
        employee_dataframe: Employee DataFrame.

    Returns:
        pd.Series: Average salary by department.
    """
    return employee_dataframe.groupby(
        "Department"
    )["Salary"].mean()


def calculate_maximum_salary(
    employee_dataframe: pd.DataFrame
) -> pd.Series:
    """
    Calculate maximum salary by department.

    Args:
        employee_dataframe: Employee DataFrame.

    Returns:
        pd.Series: Maximum salary by department.
    """
    return employee_dataframe.groupby(
        "Department"
    )["Salary"].max()


def count_employees_by_department(
    employee_dataframe: pd.DataFrame
) -> pd.Series:
    """
    Count employees in each department.

    Args:
        employee_dataframe: Employee DataFrame.

    Returns:
        pd.Series: Employee count by department.
    """
    return employee_dataframe.groupby(
        "Department"
    )["Name"].count()


def main() -> None:
    """
    Execute employee analysis operations.
    """
    employee_dataframe = create_employee_dataframe()

    print("\nAverage Salary By Department")
    print(
        calculate_average_salary(
            employee_dataframe
        )
    )

    print("\nMaximum Salary By Department")
    print(
        calculate_maximum_salary(
            employee_dataframe
        )
    )

    print("\nEmployee Count By Department")
    print(
        count_employees_by_department(
            employee_dataframe
        )
    )


if __name__ == "__main__":
    main()