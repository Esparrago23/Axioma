from fastapi import FastAPI
from contextlib import asynccontextmanager
from pathlib import Path
from fastapi.staticfiles import StaticFiles

from app.core.database import init_db
from app.modules.auth.infrastructure.routes.auth_routes import router as auth_router
from app.modules.users.infrastructure.routes.user_routes import router as users_router
from app.modules.reports.infrastructure.routes.report_routes import router as reports_router

APP_DIR = Path(__file__).resolve().parent
STATIC_DIR = APP_DIR / "static"

@asynccontextmanager
async def lifespan(app: FastAPI):
    init_db()
    yield

app = FastAPI(
    title="Axioma API",
    description="Backend Modular para Axioma (Hexagonal Architecture)",
    version="3.0.0",
    lifespan=lifespan
)


app.include_router(auth_router)    
app.include_router(users_router)  
app.include_router(reports_router) 
app.mount("/static", StaticFiles(directory=str(STATIC_DIR)), name="static")

@app.get("/")
def read_root():
    return {
        "message": "Axioma API is running. Don't panic.",
        "architecture": "Hexagonal ",
        "status": "Online "
    }