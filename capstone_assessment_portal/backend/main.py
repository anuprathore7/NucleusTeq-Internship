from contextlib import asynccontextmanager

from fastapi import FastAPI

from app.config.database import client
from app.constants.url_prefix import URL_PREFIX


@asynccontextmanager
async def lifespan(app: FastAPI):
    """
    Runs once when application starts
    and once when application stops.
    """

    try:
        await client.admin.command("ping")
        print("MongoDB Connected Successfully")

    except Exception as error:
        print(f"MongoDB Connection Failed: {error}")

    yield

    client.close()

"""Initialize app using fastapi and add title for project"""
app = FastAPI(
    title="Assessment Portal",
    version="1.0.0",
    lifespan=lifespan
)


@app.get("/" )
async def health_check():
    """
    Health check endpoint.
    """

    return {
        "message": "Assessment Portal Running"
    }