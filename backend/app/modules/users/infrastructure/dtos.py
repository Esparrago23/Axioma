from typing import Optional
from pydantic import BaseModel
from datetime import datetime

class UserUpdateDTO(BaseModel):
    username: str | None = None
    password: Optional[str] = None

class UserResponseDTO(BaseModel):
    id: int
    username: str
    email: str
    reputation_score: int
    created_at: datetime