"""
Generate Fibonacci numbers using recursion.
"""


def fibonacci(position: int) -> int:
    """Base Case"""
    if position <= 1:
        return position

    """Recursive Case"""
    return (
        fibonacci(position - 1)
        + fibonacci(position - 2)
    )


def main() -> None:
    """Generate Fibonacci numbers using recursion."""
    for position in range(10):
        print(fibonacci(position))


if __name__ == "__main__":
    main()