from datetime import datetime
from typing import Optional
from pydantic import BaseModel, EmailStr

class User(BaseModel):
    id: Optional[int] = None
    username: str
    email: EmailStr
    hashed_password: str
    reputation_score: int = 10
    fcm_token: Optional[str] = None
    last_latitude: Optional[float] = None
    last_longitude: Optional[float] = None
    profile_picture_url: Optional[str] = None
    full_name: Optional[str] = None
    created_at: datetime = datetime.utcnow()