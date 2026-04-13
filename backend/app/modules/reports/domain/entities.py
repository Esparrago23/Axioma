from enum import Enum
from datetime import datetime
from pydantic import BaseModel
from typing import Optional

class CategoryEnum(str, Enum):
    INFRAESTRUCTURA = "INFRAESTRUCTURA"
    SEGURIDAD = "SEGURIDAD"
    SANITIZACION = "SANITIZACION"
    VANDALISMO = "VANDALISMO"
    SOCIAL = "SOCIAL"

class ReportStatus(str, Enum):
    ACTIVE = "ACTIVE"
    HIDDEN = "HIDDEN"
    RESOLVED = "RESOLVED"

class Report(BaseModel):
    id: int | None = None
    title: str
    description: str
    category: CategoryEnum
    latitude: float
    longitude: float
    photo_url: Optional[str] = None
    credibility_score: int = 0
    status: ReportStatus = ReportStatus.ACTIVE
    user_id: int
    user_vote: int = 0
    created_at: datetime = datetime.utcnow()

    def calculate_reputation(self, vote_value: int):
        self.credibility_score += vote_value
        if self.credibility_score <= -5:
            self.status = ReportStatus.HIDDEN

class Vote(BaseModel):
    id: int | None = None
    user_id: int
    report_id: int
    vote_value: int  # 1, 0 o -1

# --- Evoluciones ---

EVOLUTION_CONFIRMATION_THRESHOLD = 5
EVOLUTION_REJECTION_THRESHOLD = -5

class EvolutionType(str, Enum):
    WORSENED  = "WORSENED"
    IMPROVING = "IMPROVING"
    RESOLVED  = "RESOLVED"
    ACTIVE    = "ACTIVE"
    ESCALATED = "ESCALATED"

class EvolutionStatus(str, Enum):
    PENDING   = "PENDING"
    CONFIRMED = "CONFIRMED"
    REJECTED  = "REJECTED"

class ReportEvolution(BaseModel):
    id: int | None = None
    report_id: int
    user_id: int
    type: EvolutionType
    description: str
    photo_url: Optional[str] = None
    credibility_score: int = 0
    status: EvolutionStatus = EvolutionStatus.PENDING
    is_valid: bool = True
    user_latitude: float
    user_longitude: float
    user_vote: int = 0
    created_at: datetime = datetime.utcnow()

    def apply_vote(self, vote_value: int) -> None:
        self.credibility_score += vote_value
        if self.credibility_score <= EVOLUTION_REJECTION_THRESHOLD:
            self.status = EvolutionStatus.REJECTED
            self.is_valid = False
        elif self.credibility_score >= EVOLUTION_CONFIRMATION_THRESHOLD:
            self.status = EvolutionStatus.CONFIRMED

class EvolutionVote(BaseModel):
    id: int | None = None
    user_id: int
    evolution_id: int
    vote_value: int