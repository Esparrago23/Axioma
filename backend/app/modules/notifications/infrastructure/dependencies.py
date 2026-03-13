from fastapi import Depends
from sqlmodel import Session

from app.core.database import get_session
from app.core.firebase_client import get_firebase_client
from app.modules.notifications.application.get_user_notifications import GetUserNotificationsUseCase
from app.modules.notifications.application.mark_notification_read import MarkNotificationReadUseCase
from app.modules.notifications.application.send_push_notification import SendPushNotificationUseCase
from app.modules.notifications.infrastructure.controllers.get_notifications_controller import GetNotificationsController
from app.modules.notifications.infrastructure.controllers.mark_read_controller import MarkReadController
from app.modules.notifications.infrastructure.persistence.sql_repository import SQLNotificationRepository


def get_notifications_repo(session: Session = Depends(get_session)) -> SQLNotificationRepository:
    return SQLNotificationRepository(session)


def get_get_notifications_uc(repo=Depends(get_notifications_repo)):
    return GetUserNotificationsUseCase(repo)


def get_mark_read_uc(repo=Depends(get_notifications_repo)):
    return MarkNotificationReadUseCase(repo)


def get_send_push_uc(
    repo=Depends(get_notifications_repo),
    firebase_client=Depends(get_firebase_client),
):
    return SendPushNotificationUseCase(repo, firebase_client)


def get_notifications_controller(uc=Depends(get_get_notifications_uc)):
    return GetNotificationsController(uc)


def get_mark_read_controller(uc=Depends(get_mark_read_uc)):
    return MarkReadController(uc)