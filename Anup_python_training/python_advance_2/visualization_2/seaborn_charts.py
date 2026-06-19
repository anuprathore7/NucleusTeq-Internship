"""
Seaborn Visualizations.

This module demonstrates
advanced charts using Seaborn.
"""

from pathlib import Path

import matplotlib.pyplot as plt
import pandas as pd
import seaborn as sns

CHART_DIRECTORY = Path(__file__).parent / "charts"


def create_employee_dataframe() -> pd.DataFrame:
    """
    Create employee dataset.

    Returns:
        pd.DataFrame: Employee data.
    """
    employee_data = {
        "Name": ["Rahul", "Priya", "Amit", "Anuj"],
        "Age": [25, 30, 28, 35],
        "Department": ["HR", "IT", "Finance", "IT"],
        "Salary": [30000, 50000, 45000, 60000]
    }

    return pd.DataFrame(employee_data)


def create_department_salary_barplot(
    employee_dataframe: pd.DataFrame
) -> None:
    """
    Create a barplot showing
    department versus salary.
    """
    plt.figure(figsize=(8, 5))

    sns.barplot(
        data=employee_dataframe,
        x="Department",
        y="Salary"
    )

    plt.title("Department vs Salary")

    plt.savefig(
        CHART_DIRECTORY / "seaborn_barplot.png"
    )

    plt.close()


def create_salary_boxplot(
    employee_dataframe: pd.DataFrame
) -> None:
    """
    Create a boxplot showing
    salary distribution.
    """
    plt.figure(figsize=(8, 5))

    sns.boxplot(
        y=employee_dataframe["Salary"]
    )

    plt.title("Salary Distribution")

    plt.savefig(
        CHART_DIRECTORY / "salary_boxplot.png"
    )

    plt.close()


def create_correlation_heatmap(
    employee_dataframe: pd.DataFrame
) -> None:
    """
    Create a heatmap showing
    correlation between Age and Salary.
    """
    plt.figure(figsize=(6, 4))

    correlation_matrix = (
        employee_dataframe[
            ["Age", "Salary"]
        ].corr()
    )

    sns.heatmap(
        correlation_matrix,
        annot=True
    )

    plt.title("Correlation Heatmap")

    plt.savefig(
        CHART_DIRECTORY /
        "correlation_heatmap.png"
    )

    plt.close()


def main() -> None:
    """
    Execute all seaborn charts.
    """
    CHART_DIRECTORY.mkdir(
        exist_ok=True
    )

    employee_dataframe = (
        create_employee_dataframe()
    )

    create_department_salary_barplot(
        employee_dataframe
    )

    create_salary_boxplot(
        employee_dataframe
    )

    create_correlation_heatmap(
        employee_dataframe
    )

    print(
        "Seaborn charts generated successfully."
    )


if __name__ == "__main__":
    main()