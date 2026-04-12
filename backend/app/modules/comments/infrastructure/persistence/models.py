from datetime import datetime
from sqlmodel import Field, SQLModel
from sqlalchemy import Column, Integer, ForeignKey


class CommentModel(SQLModel, table=True):
    __tablename__ = "report_comments"
    id: int | None = Field(default=None, primary_key=True)
    report_id: int = Field(sa_column=Column(Integer, ForeignKey("reports.id", ondelete="CASCADE"), nullable=False, index=True))
    user_id: int = Field(index=True)
    content: str
    created_at: datetime = Field(default_factory=datetime.utcnow)
