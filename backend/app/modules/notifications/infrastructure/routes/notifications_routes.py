from fastapi import APIRouter, Depends, Query

from app.modules.auth.infrastructure.dependencies import get_current_user
from app.modules.auth.domain.entities import User
from app.modules.notifications.infrastructure.dependencies import (
    get_mark_read_controller,
    get_notifications_controller,
)
from app.modules.notifications.infrastructure.dtos import NotificationResponseDTO

router = APIRouter(prefix="/notifications", tags=["Notifications"])


@router.get("/", response_model=list[NotificationResponseDTO])
def get_notifications(
    offset: int = Query(default=0, ge=0),
    limit: int = Query(default=50, ge=1, le=100),
    controller=Depends(get_notifications_controller),
    current_user: User = Depends(get_current_user),
):
    return controller.run(user_id=current_user.id, offset=offset, limit=limit)


@router.patch("/{notification_id:int}/read", response_model=NotificationResponseDTO)
def mark_notification_read(
    notification_id: int,
    controller=Depends(get_mark_read_controller),
    current_user: User = Depends(get_current_user),
):
    return controller.run(notification_id=notification_id, user_id=current_user.id)