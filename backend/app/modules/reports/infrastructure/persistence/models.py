from datetime import datetime
from sqlmodel import Field, SQLModel
from sqlalchemy import Column, Integer, ForeignKey, UniqueConstraint
from app.modules.reports.domain.entities import ReportStatus, EvolutionStatus

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

class ReportEvolutionModel(SQLModel, table=True):
    __tablename__ = "report_evolutions"
    id: int | None = Field(default=None, primary_key=True)
    report_id: int = Field(sa_column=Column(Integer, ForeignKey("reports.id", ondelete="CASCADE"), nullable=False, index=True))
    user_id: int = Field(index=True)
    type: str
    description: str
    photo_url: str | None = None
    credibility_score: int = Field(default=0)
    status: str = Field(default=EvolutionStatus.PENDING.value)
    is_valid: bool = Field(default=True)
    user_latitude: float
    user_longitude: float
    created_at: datetime = Field(default_factory=datetime.utcnow)

class EvolutionVoteModel(SQLModel, table=True):
    __tablename__ = "evolution_votes"
    __table_args__ = (UniqueConstraint("user_id", "evolution_id", name="uq_evolution_vote"),)
    id: int | None = Field(default=None, primary_key=True)
    user_id: int
    evolution_id: int = Field(sa_column=Column(Integer, ForeignKey("report_evolutions.id", ondelete="CASCADE"), nullable=False, index=True))
    vote_value: int  # 1 o -1

