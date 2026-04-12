from typing import List
from fastapi import APIRouter, Depends, Response, status

from app.modules.auth.infrastructure.dependencies import get_current_user
from app.modules.comments.infrastructure.dependencies import (
    get_comments_uc, get_create_comment_uc, get_delete_comment_uc,
)
from app.modules.comments.infrastructure.dtos import CreateCommentDTO, CommentResponseDTO

router = APIRouter(prefix="/reports/{report_id}/comments", tags=["Comments"])


@router.get("/", response_model=List[CommentResponseDTO])
def get_comments(
    report_id: int,
    uc=Depends(get_comments_uc),
    user=Depends(get_current_user),
):
    comments = uc.execute(report_id)
    return [CommentResponseDTO(**c.model_dump()) for c in comments]


@router.post("/", response_model=CommentResponseDTO, status_code=status.HTTP_201_CREATED)
def create_comment(
    report_id: int,
    data: CreateCommentDTO,
    uc=Depends(get_create_comment_uc),
    user=Depends(get_current_user),
):
    comment = uc.execute(report_id=report_id, user_id=user.id, dto=data)
    return CommentResponseDTO(**comment.model_dump())


@router.delete("/{comment_id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_comment(
    report_id: int,
    comment_id: int,
    uc=Depends(get_delete_comment_uc),
    user=Depends(get_current_user),
):
    uc.execute(comment_id=comment_id, user_id=user.id)
    return Response(status_code=status.HTTP_204_NO_CONTENT)
