from contextlib import asynccontextmanager
from fastapi import FastAPI
import os

from app.config.database import client
from app.config.logger import setup_logging, get_logger

logger = get_logger(__name__)


@asynccontextmanager
async def lifespan(app: FastAPI):
    """
    Application startup and shutdown handler.
    Skips MongoDB ping during testing to avoid event loop conflicts.
    Motor connects lazily on first query — no manual ping needed in tests.
    """
    setup_logging()

    # PYTEST_CURRENT_TEST is set automatically by pytest on every test
    is_testing = "PYTEST_CURRENT_TEST" in os.environ

    if is_testing:
        logger.info("Test environment detected — skipping MongoDB ping")
    else:
        try:
            await client.admin.command("ping")
            logger.info("MongoDB connected successfully")
        except Exception as error:
            logger.error(f"MongoDB connection failed: {error}")

    yield

    if not is_testing:
        client.close()
        logger.info("MongoDB connection closed — server shutting down")