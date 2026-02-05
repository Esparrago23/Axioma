from fastapi import FastAPI
from contextlib import asynccontextmanager
from sqlmodel import SQLModel
from app.database import engine 
from app.routes import router

@asynccontextmanager
async def lifespan(app: FastAPI):
    SQLModel.metadata.create_all(engine)
    yield
app = FastAPI(
    title="Axioma API",
    description="Backend para Axioma",
    version="1.0.0",
    lifespan=lifespan
)
app.include_router(router)

@app.get("/")
def read_root():
    return {"message": "Axioma API is running. Don't panic."}