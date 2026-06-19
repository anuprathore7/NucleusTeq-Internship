"""
Student Performance Analysis Project.

This project analyzes student marks and
study hours using Pandas. It also creates
visualizations using Matplotlib and Seaborn
to better understand student performance.
"""

from pathlib import Path

import matplotlib.pyplot as plt
import pandas as pd
import seaborn as sns

CHART_OUTPUT_DIRECTORY = Path(__file__).parent / "charts"


def create_student_dataframe() -> pd.DataFrame:
    """
    Create and return the student dataset
    used throughout this project.

    Returns:
        pd.DataFrame containing student
        names, marks and hours studied.
    """
    student_details = {
        "Name": ["Rahul", "Priya", "Siri", "Anuj"],
        "Marks": [70, 80, 90, 60],
        "Hours Studied": [2, 3, 5, 1]
    }

    return pd.DataFrame(student_details)


def add_student_performance_status(
    student_records: pd.DataFrame
) -> pd.DataFrame:
    """
    Add a performance status column.

    Students scoring more than 65 marks
    are marked as Pass, otherwise they
    are marked as Fail.

    Args:
        student_records:
            Original student dataset.

    Returns:
        Updated DataFrame with a
        Performance column.
    """
    student_records["Performance"] = (
        student_records["Marks"].apply(
            lambda student_marks:
            "Pass"
            if student_marks > 65
            else "Fail"
        )
    )

    return student_records


def create_hours_vs_marks_line_chart(
    student_records: pd.DataFrame
) -> None:
    """
    Generate a line chart showing the
    relationship between study hours
    and marks obtained.

    The chart is saved inside the
    charts directory.
    """
    plt.figure(figsize=(8, 5))

    plt.plot(
        student_records["Hours Studied"],
        student_records["Marks"],
        marker="o"
    )

    plt.title("Hours Studied vs Marks")
    plt.xlabel("Hours Studied")
    plt.ylabel("Marks Obtained")

    plt.savefig(
        CHART_OUTPUT_DIRECTORY
        / "hours_vs_marks_line_chart.png"
    )

    plt.close()


def create_study_vs_marks_scatter_plot(
    student_records: pd.DataFrame
) -> None:
    """
    Generate a scatter plot showing
    how study hours relate to marks.

    The chart is saved inside the
    charts directory.
    """
    plt.figure(figsize=(8, 5))

    plt.scatter(
        student_records["Hours Studied"],
        student_records["Marks"]
    )

    plt.title("Study Hours vs Marks")
    plt.xlabel("Hours Studied")
    plt.ylabel("Marks Obtained")

    plt.savefig(
        CHART_OUTPUT_DIRECTORY
        / "study_vs_marks_scatter_plot.png"
    )

    plt.close()


def create_performance_comparison_barplot(
    student_records: pd.DataFrame
) -> None:
    """
    Generate a bar plot comparing
    student performance categories
    against marks obtained.

    The chart is saved inside the
    charts directory.
    """
    plt.figure(figsize=(8, 5))

    sns.barplot(
        data=student_records,
        x="Performance",
        y="Marks"
    )

    plt.title("Performance vs Marks")

    plt.savefig(
        CHART_OUTPUT_DIRECTORY
        / "performance_barplot.png"
    )

    plt.close()


def main() -> None:
    """
    Run the complete student
    performance analysis workflow.
    
    """
    CHART_OUTPUT_DIRECTORY.mkdir(
        exist_ok=True
    )

    student_records = (
        create_student_dataframe()
    )

    student_records = (
        add_student_performance_status(
            student_records
        )
    )

    print("\nStudent Performance Dataset")
    print(student_records)

    create_hours_vs_marks_line_chart(
        student_records
    )

    create_study_vs_marks_scatter_plot(
        student_records
    )

    create_performance_comparison_barplot(
        student_records
    )

    print(
        "\nAll charts generated successfully."
    )


if __name__ == "__main__":
    main()