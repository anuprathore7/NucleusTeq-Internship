
""" This function performs basic set operations and displays the results. """
def perform_set_operations() -> None:

    first_set: set[int] = {10,20,30,40}

    second_set: set[int] = {30,40,50,60}

    print(f"Union: {first_set.union(second_set)}")
    print(f"Intersection: {first_set.intersection(second_set)}")

    print( f"Difference: {first_set.difference(second_set)}")


""" Executes the program only when this file is run directly. """
if __name__ == "__main__":
    perform_set_operations()