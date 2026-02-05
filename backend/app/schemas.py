from typing import Optional
from datetime import datetime
from sqlmodel import SQLModel
from app.models import CategoryEnum, ReportStatus, VoteType


class LoginRequest(SQLModel):
    email: str
    password: str

class TokenResponse(SQLModel):
    access_token: str
    token_type: str = "bearer"
    user_id: int
    username: str
    reputation: int

# --- REPORT SCHEMAS ---

class ReportCreate(SQLModel):
    title: str
    description: str
    category: CategoryEnum
    latitude: float
    longitude: float
    photo_url: Optional[str] = None


# Esto es lo que consume Mapbox.
class ReportRead(SQLModel):
    id: int
    title: str
    description: str
    category: CategoryEnum
    latitude: float
    longitude: float
    photo_url: Optional[str]
    credibility_score: int
    status: ReportStatus
    created_at: datetime
    


class VoteCreate(SQLModel):
    vote_value: VoteType

class VoteResponse(SQLModel):
    new_score: int
    report_status: ReportStatus