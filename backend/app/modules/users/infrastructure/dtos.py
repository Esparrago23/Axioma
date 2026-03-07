from typing import Optional
from pydantic import BaseModel
from datetime import datetime

class UserUpdateDTO(BaseModel):
    username: Optional[str] = None
    full_name: Optional[str] = None
    profile_picture_url: Optional[str] = None

class UserResponseDTO(BaseModel):
    id: int
    username: str
    email: str
    reputation_score: int
    full_name: Optional[str] = None
    profile_picture_url: Optional[str] = None
    created_at: datetime
