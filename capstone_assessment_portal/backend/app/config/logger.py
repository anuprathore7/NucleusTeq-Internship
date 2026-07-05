import logging
import sys

"""
Central logging configuration for the entire application.
"""


def setup_logging() -> None:
    """
    Configures the root logger with a consistent format and level.
    Called once from lifespan.py on application startup.
    """

    # log format — every line shows:
    # timestamp - module name - log level - message
    log_format = "%(asctime)s - %(name)s - %(levelname)s - %(message)s"

    # date format inside the timestamp
    date_format = "%Y-%m-%d %H:%M:%S"

    logging.basicConfig(
        level=logging.INFO,         
        format=log_format,
        datefmt=date_format,
        handlers=[
            logging.StreamHandler(sys.stdout)
        ]
    )


def get_logger(name: str) -> logging.Logger:
    """
    Returns a logger for the given module name.
    """
    return logging.getLogger(name)