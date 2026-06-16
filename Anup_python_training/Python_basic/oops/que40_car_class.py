
""" This class stores information about a car. """
class Car:

    def __init__(self,car_brand,car_model) -> None:

        self.car_brand = car_brand
        self.car_model = car_model

    """ This method displays car information. """
    def display_car_details(self) -> None:

        print(f"Car Brand: {self.car_brand}")

        print(f"Car Model: {self.car_model}")


""" Executes the program only when this file is run directly. """
if __name__ == "__main__":

    car = Car("Toyota","Fortuner")

    car.display_car_details()