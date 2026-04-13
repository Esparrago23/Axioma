from typing import List, Optional
from sqlmodel import Session, select
from sqlalchemy import desc
from app.modules.comments.domain.entities import Comment
from app.modules.comments.domain.repository import CommentRepository
from app.modules.comments.infrastructure.persistence.models import CommentModel


class SQLCommentRepository(CommentRepository):
    def __init__(self, session: Session):
        self.session = session

    def save(self, comment: Comment) -> Comment:
        model = CommentModel(**comment.model_dump())
        self.session.add(model)
        self.session.commit()
        self.session.refresh(model)
        return self._to_domain(model)

    def get_by_report(self, report_id: int) -> List[Comment]:
        statement = (
            select(CommentModel)
            .where(CommentModel.report_id == report_id)
            .order_by(CommentModel.created_at)
        )
        return [self._to_domain(r) for r in self.session.exec(statement).all()]

    def get_by_id(self, comment_id: int) -> Optional[Comment]:
        model = self.session.get(CommentModel, comment_id)
        return self._to_domain(model) if model else None

    def delete(self, comment_id: int) -> bool:
        model = self.session.get(CommentModel, comment_id)
        if model:
            self.session.delete(model)
            self.session.commit()
            return True
        return False

    def _to_domain(self, model: CommentModel) -> Comment:
        return Comment(**model.model_dump())
