from datetime import datetime
from enum import Enum

from pydantic import BaseModel


class NotificationType(str, Enum):
    NEW_NEARBY_REPORT = "NEW_NEARBY_REPORT"


class NotificationRecipient(BaseModel):
    user_id: int
    fcm_token: str


class Notification(BaseModel):
    id: int | None = None
    user_id: int
    title: str
    message: str
    type: str
    is_read: bool = False
    report_id: int | None = None
    created_at: datetime = datetime.utcnow()