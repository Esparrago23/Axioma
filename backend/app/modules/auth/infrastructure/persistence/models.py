from datetime import datetime
from typing import Optional
from sqlmodel import Field, SQLModel

class UserModel(SQLModel, table=True):
    __tablename__ = "users" # Nombre explícito de la tabla

    id: Optional[int] = Field(default=None, primary_key=True)
    username: str = Field(index=True, unique=True)
    email: str = Field(index=True, unique=True)
    hashed_password: str
    reputation_score: int = Field(default=10)
    profile_picture_url: Optional[str] = Field(default=None)
    full_name: Optional[str] = Field(default=None)
    created_at: datetime = Field(default_factory=datetime.utcnow)