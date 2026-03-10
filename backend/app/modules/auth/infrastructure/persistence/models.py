from datetime import datetime
from typing import Optional
from sqlmodel import Field, SQLModel
from sqlalchemy import Column, Integer, ForeignKey

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


class RefreshTokenModel(SQLModel, table=True):
    __tablename__ = "refresh_tokens"

    id: Optional[int] = Field(default=None, primary_key=True)
    user_id: int = Field(sa_column=Column(Integer, ForeignKey("users.id", ondelete="CASCADE"), nullable=False, index=True))
    token_hash: str = Field(unique=True, index=True)
    expires_at: datetime
    device_name: Optional[str] = None
    created_at: datetime = Field(default_factory=datetime.utcnow)
    last_used_at: datetime = Field(default_factory=datetime.utcnow)
    revoked_at: Optional[datetime] = None