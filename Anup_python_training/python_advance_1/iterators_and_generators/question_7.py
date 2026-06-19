"""
Process a large dataset using a generator
instead of storing everything in memory.
"""


def generate_numbers(limit: int):
    """
    Generate numbers one at a time.
    """

    for number in range(limit):
        yield number


def main() -> None:

    total_sum: int = 0

    # Numbers are produced only when needed
    for number in generate_numbers(1_000_000):
        total_sum += number

    print(f"Total Sum: {total_sum}")


if __name__ == "__main__":
    main()