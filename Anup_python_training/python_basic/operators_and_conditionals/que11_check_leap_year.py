""" This function checks whether year is a leap year or not. """
def check_leap_year(year: int) -> None:
    """This is the logic for checking leap year."""
    if (year % 400 == 0) or (year % 4 == 0 and year % 100 != 0):
        print(f"{year} is a Leap Year")

    else:
        print(f"{year} is not a Leap Year")


""" Executes the program only when this file is run directly. """
if __name__ == "__main__":

    year: int = int(input("Enter a year: "))
    check_leap_year(year)