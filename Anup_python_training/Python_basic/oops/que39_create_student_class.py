
""" This module defines the Student class. """
class Student:
    """ Initializes the Student object with name, age, and city. """
    def __init__(self, name, age, city)->None:
        self.name = name
        self.age = age
        self.city = city

    def display_user_details(self)->None:
        print(f"Name: {self.name}")
        print(f"Age: {self.age}")
        print(f"City: {self.city}")


""" Executes the program only when this file is run directly."""
if __name__ == "__main__":
    student= Student("Anup", 25, "Indore")
    student.display_user_details()


