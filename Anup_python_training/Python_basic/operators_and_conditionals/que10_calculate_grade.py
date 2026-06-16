""" This function calculates and displays the grade based on the marks entered by the user. """
def calculate_grade(student_marks: int) -> None:

    if student_marks >= 90:
        print("Grade: A")

    elif student_marks >= 75:
        print("Grade: B")

    elif student_marks >= 30:
        print("Grade: C")

    else:
        print("Grade: Fail")


""" Executes the program only when this file is run directly. """
if __name__ == "__main__":

    student_marks: int = int(input("Enter your marks: "))
    calculate_grade(student_marks)