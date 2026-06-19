"""
Simulate downloading multiple files
using threads.
"""

import threading
import time


def download_file(file_name: str) -> None:
    """
    Simulate a file download.
    """

    print(f"Downloading {file_name}")

    time.sleep(2)

    print(f"{file_name} downloaded")


def main() -> None:

    file_names: list[str] = [
        "file_1.pdf",
        "file_2.pdf",
        "file_3.pdf"
    ]

    threads: list[threading.Thread] = []

    for file_name in file_names:

        thread = threading.Thread(
            target=download_file,
            args=(file_name,)
        )

        threads.append(thread)

        thread.start()

    for thread in threads:
        thread.join()

"""Executes the program only when this file is run directly."""
if __name__ == "__main__":
    main()