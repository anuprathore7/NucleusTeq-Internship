
""" This function displays the sum, maximum value, sorted list, and unique elements from a list. """
def perform_list_operations(number_list: list[int]) -> None:

    total_sum: int = sum(number_list)

    maximum_number: int = max(number_list)

    sorted_numbers: list[int] = sorted(number_list)

    unique_numbers: list[int] = list(set(number_list))

    print(f"Sum of numbers: {total_sum}")

    print(f"Maximum number: {maximum_number}")

    print(f"Sorted list: {sorted_numbers}")

    print(f"List after removing duplicates: {unique_numbers}")


""" Executes the program only when this file is run directly. """
if __name__ == "__main__":

    number_list: list[int] = [10,25,30,15,25,40,50,10,60,70]

    perform_list_operations(number_list)