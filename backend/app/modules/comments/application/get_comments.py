from typing import List
from app.modules.comments.domain.entities import Comment
from app.modules.comments.domain.repository import CommentRepository


class GetCommentsUseCase:
    def __init__(self, repo: CommentRepository):
        self.repo = repo

    def execute(self, report_id: int) -> List[Comment]:
        return self.repo.get_by_report(report_id)
