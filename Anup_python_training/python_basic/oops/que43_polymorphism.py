
""" This class represents a Dog. """
class Dog:

    """ This method displays the sound made by a dog. """
    def make_sound(self) -> None:

        print("Dog barks" )


""" This class represents a Cat. """
class Cat:

    """ This method displays the sound made by a cat. """
    def make_sound(self) -> None:

        print("Cat meows")


""" This function calls the make_sound method. """
def animal_sound(animal) -> None:

    animal.make_sound()


""" Executes the program only when this file is run directly. """
if __name__ == "__main__":

    dog = Dog()

    cat = Cat()

    animal_sound(dog)

    animal_sound(cat)