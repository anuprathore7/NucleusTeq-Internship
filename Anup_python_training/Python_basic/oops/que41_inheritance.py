
""" This class stores basic person information. """
class Person:

    def __init__(self,person_name,person_age) -> None:

        self.person_name = person_name
        self.person_age = person_age


""" This class inherits properties from Person class. """
class Employee(Person):

    def __init__(self,person_name,person_age,employee_id) -> None:

        super().__init__(person_name,person_age)
        self.employee_id = employee_id

    """ This method displays employee details. """
    def display_employee_details(self) -> None:

        print(f"Employee Name: {self.person_name}")

        print(f"Employee Age: {self.person_age}")

        print(f"Employee ID: {self.employee_id}")


""" Executes the program only when this file is run directly. """
if __name__ == "__main__":

    employee = Employee("Anup",21,101)

    employee.display_employee_details()