
""" This function counts and displays the total even and odd numbers in a list. """
def count_even_and_odd_numbers(number_list: list[int]) -> None:

    even_count: int = 0
    odd_count: int = 0

    for number in range(len(number_list)):

        if number % 2 == 0:
            even_count += 1

        else:
            odd_count += 1

    print(f"Total Even Numbers: {even_count}")

    print(f"Total Odd Numbers: {odd_count}")


""" Executes the program only when this file is run directly. """
if __name__ == "__main__":

    number_list: list[int] = [10,15,20,25,30,35,40]

    count_even_and_odd_numbers(number_list)