from fastapi import HTTPException, status
from app.modules.comments.domain.repository import CommentRepository


class DeleteCommentUseCase:
    def __init__(self, repo: CommentRepository):
        self.repo = repo

    def execute(self, comment_id: int, user_id: int) -> None:
        comment = self.repo.get_by_id(comment_id)
        if comment is None:
            raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Comentario no encontrado")
        if comment.user_id != user_id:
            raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="No puedes eliminar el comentario de otro usuario")
        self.repo.delete(comment_id)
