from datetime import datetime

from pydantic import BaseModel


class NotificationResponseDTO(BaseModel):
    id: int
    user_id: int
    title: str
    message: str
    type: str
    is_read: bool
    report_id: int | None = None
    created_at: datetime