from app.core.firebase_client import FirebaseClient
from app.modules.notifications.domain.entities import Notification, NotificationType
from app.modules.notifications.domain.repository import NotificationRepository
from app.modules.reports.domain.entities import ReportEvolution, Report


class SendEvolutionConfirmedNotificationUseCase:
    def __init__(self, repository: NotificationRepository, firebase_client: FirebaseClient):
        self.repository = repository
        self.firebase_client = firebase_client

    def execute(self, evolution: ReportEvolution, report: Report, voter_ids: list[int]) -> int:
        recipient_ids = set(voter_ids)
        recipient_ids.add(report.user_id)
        recipient_ids.discard(evolution.user_id)

        recipients = self.repository.find_recipients_by_user_ids(list(recipient_ids))
        if not recipients:
            return 0

        type_labels = {
            "WORSENED":  "Empeoró",
            "IMPROVING": "Mejorando",
            "RESOLVED":  "Resuelto",
            "ACTIVE":    "Sigue activo",
            "ESCALATED": "Escaló",
        }
        label = type_labels.get(evolution.type.value, evolution.type.value)

        title = f"Actualización confirmada: {label}"
        message = f"{report.title} — {evolution.description}"
        notification_type = NotificationType.EVOLUTION_CONFIRMED.value

        notifications = [
            Notification(
                user_id=r.user_id,
                title=title,
                message=message,
                type=notification_type,
                report_id=report.id,
            )
            for r in recipients
        ]
        self.repository.save_many(notifications)

        tokens = [r.fcm_token for r in recipients]
        max_batch = 500
        for start in range(0, len(tokens), max_batch):
            self.firebase_client.send_push_notification(
                tokens=tokens[start: start + max_batch],
                title=title,
                body=message,
                data={
                    "type": notification_type,
                    "report_id": str(report.id),
                    "evolution_id": str(evolution.id),
                },
            )
        return len(notifications)
