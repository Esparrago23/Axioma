from app.modules.notifications.domain.repository import NotificationRepository


class GetUserNotificationsUseCase:
    def __init__(self, repository: NotificationRepository):
        self.repository = repository

    def execute(self, user_id: int, offset: int, limit: int):
        return self.repository.list_by_user(user_id=user_id, offset=offset, limit=limit)