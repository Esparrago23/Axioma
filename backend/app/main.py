from fastapi import FastAPI
from contextlib import asynccontextmanager
from sqlmodel import SQLModel
from backend.app.core.database import engine
from app.routers import auth, reports, users

@asynccontextmanager
async def lifespan(app: FastAPI):
    SQLModel.metadata.create_all(engine)
    yield

app = FastAPI(
    title="Axioma API",
    description="Backend para Axioma",
    version="2.0.0", 
    lifespan=lifespan
)

app.include_router(auth.router, prefix="/auth", tags=["Auth"])
app.include_router(users.router, prefix="/users", tags=["Users"])
app.include_router(reports.router, prefix="/reports", tags=["Reports"])

@app.get("/")
def read_root():
    return {"message": "Axioma API is running. Don't panic."}