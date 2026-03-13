from app.modules.notifications.application.get_user_notifications import GetUserNotificationsUseCase


class GetNotificationsController:
    def __init__(self, use_case: GetUserNotificationsUseCase):
        self.use_case = use_case

    def run(self, user_id: int, offset: int, limit: int):
        return self.use_case.execute(user_id=user_id, offset=offset, limit=limit)