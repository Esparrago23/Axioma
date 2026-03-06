from datetime import datetime
from typing import Optional
from pydantic import BaseModel, EmailStr

class User(BaseModel):
    id: Optional[int] = None
    username: str
    email: EmailStr
    hashed_password: str
    reputation_score: int = 10
    created_at: datetime = datetime.utcnow()