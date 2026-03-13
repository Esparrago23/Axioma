from fastapi import HTTPException

from app.modules.notifications.domain.repository import NotificationRepository


class MarkNotificationReadUseCase:
    def __init__(self, repository: NotificationRepository):
        self.repository = repository

    def execute(self, notification_id: int, user_id: int):
        notification = self.repository.mark_as_read(notification_id=notification_id, user_id=user_id)
        if notification is None:
            raise HTTPException(status_code=404, detail="Notificacion no encontrada")
        return notification