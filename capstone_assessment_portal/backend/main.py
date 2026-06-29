from contextlib import asynccontextmanager

from fastapi import FastAPI

from app.config.database import client
from app.constants.url_prefix import URL_PREFIX, API_VERSION
from app.routes.auth_routes import router as auth_router


@asynccontextmanager
async def lifespan(app: FastAPI):
    """
    Startup: ping MongoDB to confirm connection is alive.
    Shutdown: close the connection pool cleanly.
    """
    try:
        await client.admin.command("ping")
        print("MongoDB Connected Successfully")
    except Exception as error:
        print(f"MongoDB Connection Failed: {error}")

    yield  # app runs here

    client.close()
    print("MongoDB Connection Closed")

app = FastAPI(
    title="Assessment Portal",
    version="1.0.0",
    lifespan=lifespan
)
# Register auth routes under /assessment/v1/auth
app.include_router(auth_router,prefix=URL_PREFIX + API_VERSION ) # "/assessment" + "/v1" = "/assessment/v1"

@app.get("/")
async def health_check():
    """Health check — confirms API is running."""
    return {"message": "Assessment Portal Running"}