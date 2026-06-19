"""
Demonstrate the use of join().
"""

import threading
import time


def perform_task() -> None:
    """
    Simulate some work.
    """

    print("Task started")

    time.sleep(3)

    print("Task completed")


def main() -> None:

    worker_thread = threading.Thread(
        target=perform_task
    )

    worker_thread.start()

    # Main thread waits here
    worker_thread.join()

    print("Program finished")

"""Executes the program only when this file is run directly."""
if __name__ == "__main__":
    main()