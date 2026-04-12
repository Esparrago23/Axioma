from datetime import datetime
from pydantic import BaseModel
from typing import Optional


class CreateCommentDTO(BaseModel):
    content: str


class CommentResponseDTO(BaseModel):
    id: int
    report_id: int
    user_id: int
    content: str
    created_at: datetime
