from fastapi import HTTPException, status
from app.modules.comments.domain.entities import Comment
from app.modules.comments.domain.repository import CommentRepository
from app.modules.comments.infrastructure.dtos import CreateCommentDTO

MAX_COMMENT_LENGTH = 500


class CreateCommentUseCase:
    def __init__(self, repo: CommentRepository):
        self.repo = repo

    def execute(self, report_id: int, user_id: int, dto: CreateCommentDTO) -> Comment:
        content = dto.content.strip()
        if not content:
            raise HTTPException(status_code=status.HTTP_422_UNPROCESSABLE_ENTITY, detail="El comentario no puede estar vacío")
        if len(content) > MAX_COMMENT_LENGTH:
            raise HTTPException(status_code=status.HTTP_422_UNPROCESSABLE_ENTITY, detail=f"El comentario no puede superar {MAX_COMMENT_LENGTH} caracteres")

        comment = Comment(report_id=report_id, user_id=user_id, content=content)
        return self.repo.save(comment)
