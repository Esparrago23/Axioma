from fastapi import FastAPI
from contextlib import asynccontextmanager
from sqlmodel import SQLModel

from app.core.database import engine
from app.modules.auth.infrastructure.routes.auth_routes import router as auth_router
from app.modules.users.infrastructure.routes.user_routes import router as users_router
from app.modules.reports.infrastructure.routes.report_routes import router as reports_router

@asynccontextmanager
async def lifespan(app: FastAPI):
    SQLModel.metadata.create_all(engine)
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

@app.get("/")
def read_root():
    return {
        "message": "Axioma API is running. Don't panic.",
        "architecture": "Hexagonal ",
        "status": "Online "
    }