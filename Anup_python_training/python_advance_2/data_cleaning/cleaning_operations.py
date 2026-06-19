"""
Data Cleaning Operations.

This module demonstrates how to identify
and handle missing values using Pandas.
"""

import pandas as pd


def create_employee_dataframe() -> pd.DataFrame:
    """
    Create a DataFrame containing
    missing values.

    Returns:
        pd.DataFrame: Employee DataFrame.
    """
    employee_data: dict[str, list] = {
        "Name": ["Rahul", "Priya", "Anuj"],
        "Age": [25, None, 29],
        "Salary": [30000, 40000, None]
    }

    return pd.DataFrame(employee_data)


def detect_missing_values(employee_dataframe: pd.DataFrame) -> None:
    """
    Display missing values in the DataFrame.

    Args:
        employee_dataframe:
            Employee DataFrame.
    """
    print("\nMissing Values")
    print(employee_dataframe.isnull())


def replace_missing_age(employee_dataframe: pd.DataFrame) -> pd.DataFrame:
    """
    Replace missing age values
    with the mean age.

    Args:
        employee_dataframe:
            Employee DataFrame.

    Returns:
        pd.DataFrame:
            Updated DataFrame.
    """
    average_age = employee_dataframe["Age"].mean()

    employee_dataframe["Age"] = (
        employee_dataframe["Age"].fillna(average_age)
    )

    return employee_dataframe


def replace_missing_salary(employee_dataframe: pd.DataFrame) -> pd.DataFrame:
    """
    Replace missing salary values with zero.

    Args:
        employee_dataframe:
            Employee DataFrame.

    Returns:
        pd.DataFrame:
            Updated DataFrame.
    """
    employee_dataframe["Salary"] = (
        employee_dataframe["Salary"].fillna(0)
    )

    return employee_dataframe


def main() -> None:
    """
    Execute data cleaning operations.
    """
    employee_dataframe = create_employee_dataframe()

    print("Original DataFrame")
    print(employee_dataframe)

    detect_missing_values(employee_dataframe)

    employee_dataframe = replace_missing_age(
        employee_dataframe
    )

    employee_dataframe = replace_missing_salary(
        employee_dataframe
    )

    print("\nCleaned DataFrame")
    print(employee_dataframe)


if __name__ == "__main__":
    main()