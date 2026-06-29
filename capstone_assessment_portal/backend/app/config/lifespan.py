from contextlib import asynccontextmanager
from fastapi import FastAPI
from app.config.database import client

"""
Manages the FastAPI application lifecycle.
"""

@asynccontextmanager
async def lifespan(app: FastAPI):
    """
    Application startup and shutdown handler.
    """

    # ── STARTUP ──────────────────────────────────────────────────
    try:
        # ping MongoDB to verify connection is working
        # client.admin.command("ping") is the standard MongoDB health check
        await client.admin.command("ping")
        print("MongoDB Connected Successfully")

    except Exception as error:
        # log the error but don't crash
        # server still starts — MongoDB might recover
        print(f"MongoDB Connection Failed: {error}")

    # app runs here — handles all incoming requests
    yield

    # ── SHUTDOWN ─────────────────────────────────────────────────
    # close all connections in the pool when server stops
    client.close()
    print("MongoDB Connection Closed")