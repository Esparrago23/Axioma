from abc import ABC, abstractmethod

from app.modules.notifications.domain.entities import Notification, NotificationRecipient


class NotificationRepository(ABC):
    @abstractmethod
    def save_many(self, notifications: list[Notification]) -> list[Notification]:
        pass

    @abstractmethod
    def list_by_user(self, user_id: int, offset: int, limit: int) -> list[Notification]:
        pass

    @abstractmethod
    def mark_as_read(self, notification_id: int, user_id: int) -> Notification | None:
        pass

    @abstractmethod
    def find_recipients_nearby(
        self,
        latitude: float,
        longitude: float,
        radius_km: float,
        exclude_user_id: int,
    ) -> list[NotificationRecipient]:
        pass

    @abstractmethod
    def find_recipients_by_user_ids(self, user_ids: list[int]) -> list[NotificationRecipient]:
        pass