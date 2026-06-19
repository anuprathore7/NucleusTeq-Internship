"""
Application-wide constants.

This module stores configuration values used
throughout the project to avoid hardcoding.
"""

# Employee DataFrame Constants
BONUS_PERCENTAGE: float = 0.10

# Student Mini Project Constants
PASSING_MARKS: int = 65

# Employee Dataset Columns
EMPLOYEE_COLUMNS: list[str] = [
    "Name",
    "Age",
    "Department",
    "Salary"
]

# Student Dataset Columns
STUDENT_COLUMNS: list[str] = [
    "Name",
    "Marks",
    "Hours Studied"
]