from app.core.config import settings
from app.core.firebase_client import FirebaseClient
from app.modules.notifications.domain.entities import Notification, NotificationType
from app.modules.notifications.domain.repository import NotificationRepository
from app.modules.reports.domain.entities import Report


class SendPushNotificationUseCase:
    def __init__(self, repository: NotificationRepository, firebase_client: FirebaseClient):
        self.repository = repository
        self.firebase_client = firebase_client

    def execute(self, report: Report) -> int:
        recipients = self.repository.find_recipients_nearby(
            latitude=report.latitude,
            longitude=report.longitude,
            radius_km=settings.NOTIFICATIONS_RADIUS_KM,
            exclude_user_id=report.user_id,
        )
        if not recipients:
            return 0

        title = "Nuevo reporte cerca de ti"
        message = report.title
        notification_type = NotificationType.NEW_NEARBY_REPORT.value

        notifications = [
            Notification(
                user_id=recipient.user_id,
                title=title,
                message=message,
                type=notification_type,
                report_id=report.id,
            )
            for recipient in recipients
        ]
        self.repository.save_many(notifications)

        tokens = [recipient.fcm_token for recipient in recipients]
        max_tokens_per_batch = 500
        for start in range(0, len(tokens), max_tokens_per_batch):
            batch_tokens = tokens[start : start + max_tokens_per_batch]
            self.firebase_client.send_push_notification(
                tokens=batch_tokens,
                title=title,
                body=message,
                data={
                    "type": notification_type,
                    "report_id": str(report.id),
                    "category": report.category.value,
                },
            )
        return len(notifications)