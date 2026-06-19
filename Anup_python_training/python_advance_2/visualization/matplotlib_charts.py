"""
Matplotlib Chart Visualizations.

This module demonstrates how to create
basic charts using Matplotlib.
"""

from pathlib import Path

import matplotlib.pyplot as plt

# Employee Data
DEPARTMENTS: list[str] = ["HR", "IT", "Finance"]
EMPLOYEE_COUNTS: list[int] = [5, 12, 7]

SALARIES: list[int] = [30000, 40000, 50000, 60000, 45000]

AGES: list[int] = [25, 30, 28, 35]
EMPLOYEE_SALARIES: list[int] = [30000, 50000, 45000, 60000]

# Charts Folder
CHART_DIRECTORY = Path(__file__).parent / "charts"


def create_bar_chart() -> None:
    """
    Create a bar chart showing employee
    count by department.
    """
    plt.figure(figsize=(8, 5))

    plt.bar(
        DEPARTMENTS,
        EMPLOYEE_COUNTS
    )

    plt.title("Employees by Department")
    plt.xlabel("Department")
    plt.ylabel("Employee Count")

    plt.savefig(
        CHART_DIRECTORY / "bar_chart.png"
    )

    plt.close()


def create_line_chart() -> None:
    """
    Create a line chart showing employee
    count by department.
    """
    plt.figure(figsize=(8, 5))

    plt.plot(
        DEPARTMENTS,
        EMPLOYEE_COUNTS,
        marker="o"
    )

    plt.title("Employee Trend")
    plt.xlabel("Department")
    plt.ylabel("Employee Count")

    plt.savefig(
        CHART_DIRECTORY / "line_chart.png"
    )

    plt.close()


def create_histogram() -> None:
    """
    Create a histogram showing
    salary distribution.
    """
    plt.figure(figsize=(8, 5))

    plt.hist(
        SALARIES,
        bins=5
    )

    plt.title("Salary Distribution")
    plt.xlabel("Salary")
    plt.ylabel("Frequency")

    plt.savefig(
        CHART_DIRECTORY / "histogram.png"
    )

    plt.close()


def create_scatter_plot() -> None:
    """
    Create a scatter plot showing
    age versus salary.
    """
    plt.figure(figsize=(8, 5))

    plt.scatter(
        AGES,
        EMPLOYEE_SALARIES
    )

    plt.title("Age vs Salary")
    plt.xlabel("Age")
    plt.ylabel("Salary")

    plt.savefig(
        CHART_DIRECTORY / "scatter_plot.png"
    )

    plt.close()


def main() -> None:
    """
    Execute all chart creation tasks.
    """
    CHART_DIRECTORY.mkdir(exist_ok=True)

    create_bar_chart()
    create_line_chart()
    create_histogram()
    create_scatter_plot()

    print("All charts generated successfully.")


if __name__ == "__main__":
    main()