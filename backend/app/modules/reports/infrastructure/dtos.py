from pydantic import BaseModel
from typing import Optional

class CreateReportDTO(BaseModel):
    title: str
    description: str
    latitude: float
    longitude: float
    category: str

class UpdateReportDTO(BaseModel):
    title: Optional[str] = None
    description: Optional[str] = None

class VoteDTO(BaseModel):
    vote_value: str  # "UP" o "DOWN"