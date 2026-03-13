from datetime import datetime
from pydantic import BaseModel
from typing import Optional
from app.modules.reports.domain.entities import CategoryEnum, ReportStatus

class CreateReportDTO(BaseModel):
    title: str
    description: str
    latitude: float
    longitude: float
    category: str
    photo_url: Optional[str] = None
    
class ReportResponseDTO(BaseModel):
    id: int
    title: str
    description: str
    category: str
    latitude: float
    longitude: float
    photo_url: Optional[str]
    credibility_score: int
    status: str
    user_id: int
    user_vote: int = 0
    created_at: datetime

class UpdateReportDTO(BaseModel):
    title: str
    description: str
    photo_url: Optional[str] = None

class VoteDTO(BaseModel):
    vote_value: int

class ReportPhotoUploadResponseDTO(BaseModel):
    photo_url: str