
""" This function counts and displays character frequencies. """
def count_character_frequency(input_string: str) -> None:

    character_frequency: dict = {}
    """ Iterating through each character in the input string and counting its frequency. """
    for character in input_string:

        if character in character_frequency:
            """ If the character is already in the dictionary, increment its count. """
            character_frequency[character] += 1

        else:
            """ If the character is not in the dictionary, add it with a count of 1. """
            character_frequency[character] = 1

    print(character_frequency)


""" Executes the program only when this file is run directly. """
if __name__ == "__main__":

    input: str = input("Enter a string: ")

    count_character_frequency(input)