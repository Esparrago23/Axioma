from datetime import datetime

from sqlmodel import Field, SQLModel


class NotificationModel(SQLModel, table=True):
    __tablename__ = "notifications"

    id: int | None = Field(default=None, primary_key=True)
    user_id: int = Field(index=True)
    title: str
    message: str
    type: str = Field(index=True)
    is_read: bool = Field(default=False, index=True)
    report_id: int | None = Field(default=None, index=True)
    created_at: datetime = Field(default_factory=datetime.utcnow, index=True)