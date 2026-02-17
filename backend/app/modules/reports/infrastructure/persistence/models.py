from datetime import datetime
from sqlmodel import Field, SQLModel
from sqlalchemy import Column, Integer, ForeignKey
from app.modules.reports.domain.entities import ReportStatus

# Tablas SQL puras
class ReportModel(SQLModel, table=True):
    __tablename__ = "reports"
    id: int | None = Field(default=None, primary_key=True)
    title: str
    description: str
    category: str
    latitude: float = Field(index=True)
    longitude: float = Field(index=True)
    photo_url: str | None = None
    credibility_score: int = Field(default=0)
    status: str = Field(default=ReportStatus.ACTIVE.value)
    user_id: int = Field(index=True)
    created_at: datetime = Field(default_factory=datetime.utcnow)

class VoteModel(SQLModel, table=True):
    __tablename__ = "votes"
    id: int | None = Field(default=None, primary_key=True)
    user_id: int
    report_id: int = Field(sa_column=Column(Integer, ForeignKey("reports.id", ondelete="CASCADE"), nullable=False))
    vote_value: int