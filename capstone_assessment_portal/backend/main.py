from fastapi import FastAPI

# lifespan now lives in its own file — senior's feedback addressed
from app.config.lifespan import lifespan
from app.constants.url_prefix import URL_PREFIX, API_VERSION
from app.routes.auth_routes import router as auth_router
from app.routes.category_routes import router as category_router
from app.routes.quiz_routes import router as quiz_router
from app.routes.question_routes import router as question_router

"""
main.py has exactly ONE job:
- Create the FastAPI application instance
- Register the lifespan handler
- Register all route modules
"""

app = FastAPI(
    title="Assessment Portal",
    version="1.0.0",
    lifespan=lifespan       # imported from app/config/lifespan.py
)

# ── Register all routers ──────────────────────────────────────────────
# Base prefix for all routes: /assessment/v1

app.include_router(auth_router, prefix=URL_PREFIX + API_VERSION)
app.include_router(category_router, prefix=URL_PREFIX + API_VERSION)
app.include_router(quiz_router, prefix=URL_PREFIX + API_VERSION)
app.include_router(question_router, prefix=URL_PREFIX + API_VERSION)


@app.get("/")
async def health_check():
    """
    Health check endpoint.
    Used to verify the server is running.
    Returns 200 if everything is fine.
    """
    return {"message": "Assessment Portal Running"}