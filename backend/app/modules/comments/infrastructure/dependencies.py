from fastapi import Depends
from sqlmodel import Session
from app.core.database import get_session
from app.modules.comments.infrastructure.persistence.sql_repository import SQLCommentRepository
from app.modules.comments.application.get_comments import GetCommentsUseCase
from app.modules.comments.application.create_comment import CreateCommentUseCase
from app.modules.comments.application.delete_comment import DeleteCommentUseCase


def get_comments_repo(session: Session = Depends(get_session)) -> SQLCommentRepository:
    return SQLCommentRepository(session)

def get_comments_uc(repo=Depends(get_comments_repo)): return GetCommentsUseCase(repo)
def get_create_comment_uc(repo=Depends(get_comments_repo)): return CreateCommentUseCase(repo)
def get_delete_comment_uc(repo=Depends(get_comments_repo)): return DeleteCommentUseCase(repo)
