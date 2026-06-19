"""
Employee DataFrame operations.

This module demonstrates how to create and
manipulate a Pandas DataFrame using employee data.
"""

from typing import Any

import pandas as pd

from Python_advance_2.constants import BONUS_PERCENTAGE

def create_employee_dataframe() -> pd.DataFrame:
    """
    Create an employee DataFrame.

    Returns:
        pd.DataFrame: Employee information.
    """
    employee_data: dict[str, list[Any]] = {
        "Name": ["Rahul", "Priya", "Amit", "Anuj"],
        "Age": [25, 30, 28, 35],
        "Department": ["HR", "IT", "Finance", "IT"],
        "Salary": [30000, 50000, 45000, 60000]
    }

    return pd.DataFrame(employee_data)


def display_first_two_rows(employee_dataframe: pd.DataFrame) -> None:
    """
    Display the first two rows.

    Args:
        employee_dataframe: Employee DataFrame.
    """
    print("\nFirst Two Rows")
    print(employee_dataframe.head(2))


def display_summary_statistics(employee_dataframe: pd.DataFrame) -> None:
    """
    Display summary statistics.

    Args:
        employee_dataframe: Employee DataFrame.
    """
    print("\nSummary Statistics")
    print(employee_dataframe.describe())


def display_it_employees(employee_dataframe: pd.DataFrame) -> None:
    """
    Display employees belonging to IT department.

    Args:
        employee_dataframe: Employee DataFrame.
    """
    it_employees = employee_dataframe[
        employee_dataframe["Department"] == "IT"
    ]

    print("\nIT Employees")
    print(it_employees)


def add_bonus_column(employee_dataframe: pd.DataFrame) -> pd.DataFrame:
    """
    Add bonus column based on salary.

    Args:
        employee_dataframe: Employee DataFrame.

    Returns:
        pd.DataFrame: Updated DataFrame.
    """
    employee_dataframe["Bonus"] = (
        employee_dataframe["Salary"] * BONUS_PERCENTAGE
    )

    return employee_dataframe


def main() -> None:
    """
    Execute employee DataFrame operations.
    """
    employee_dataframe = create_employee_dataframe()

    print("\nEmployee DataFrame")
    print(employee_dataframe)

    display_first_two_rows(employee_dataframe)

    display_summary_statistics(employee_dataframe)

    display_it_employees(employee_dataframe)

    updated_dataframe = add_bonus_column(employee_dataframe)

    print("\nDataFrame With Bonus Column")
    print(updated_dataframe)


if __name__ == "__main__":
    main()