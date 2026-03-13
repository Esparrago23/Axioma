from app.modules.notifications.application.mark_notification_read import MarkNotificationReadUseCase


class MarkReadController:
    def __init__(self, use_case: MarkNotificationReadUseCase):
        self.use_case = use_case

    def run(self, notification_id: int, user_id: int):
        return self.use_case.execute(notification_id=notification_id, user_id=user_id)