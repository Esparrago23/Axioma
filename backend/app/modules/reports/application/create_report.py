from app.modules.reports.domain.repository import ReportRepository
from app.modules.reports.domain.entities import Report, CategoryEnum
from app.modules.notifications.application.send_push_notification import SendPushNotificationUseCase

class CreateReportUseCase:
    def __init__(self, repository: ReportRepository, send_push_notification: SendPushNotificationUseCase):
        self.repository = repository
        self.send_push_notification = send_push_notification

    def execute(self, title: str, desc: str, lat: float, long: float, cat: str, user_id: int, photo_url: str | None) -> Report:
        new_report = Report(
            title=title,
            description=desc,
            latitude=lat,
            longitude=long,
            category=CategoryEnum(cat),
            user_id=user_id,
            photo_url=photo_url
        )
        saved_report = self.repository.save(new_report)
        self.send_push_notification.execute(saved_report)
        return saved_report