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
    photo_url: str | None = None
    credibility_score: int = 0
    status: ReportStatus = ReportStatus.ACTIVE
    user_id: int
    created_at: datetime = datetime.utcnow()

    def calculate_reputation(self, vote_value: int):
        """Regla de Negocio: Actualiza score y oculta si es muy bajo."""
        self.credibility_score += vote_value
        if self.credibility_score <= -5:
            self.status = ReportStatus.HIDDEN

class Vote(BaseModel):
    id: int | None = None
    user_id: int
    report_id: int
    vote_value: int  # 1 o -1