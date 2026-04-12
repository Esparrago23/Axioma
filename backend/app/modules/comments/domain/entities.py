from datetime import datetime
from pydantic import BaseModel


class Comment(BaseModel):
    id: int | None = None
    report_id: int
    user_id: int
    content: str
    created_at: datetime = datetime.utcnow()
