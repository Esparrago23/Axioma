from datetime import datetime
from pydantic import BaseModel
from typing import Optional

from backend.app.modules.reports.domain.entities import CategoryEnum, ReportStatus

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
    category: CategoryEnum
    latitude: float
    longitude: float
    photo_url: Optional[str] = None 
    credibility_score: int
    status: ReportStatus
    user_id: int
    created_at: datetime

class UpdateReportDTO(BaseModel):
    title: Optional[str] = None
    description: Optional[str] = None

class VoteDTO(BaseModel):
    vote_value: str  