from math import cos, radians

from sqlalchemy import and_, desc, func
from sqlmodel import Session, select

from app.modules.auth.infrastructure.persistence.models import UserModel
from app.modules.notifications.domain.entities import Notification, NotificationRecipient
from app.modules.notifications.domain.repository import NotificationRepository
from app.modules.notifications.infrastructure.persistence.models import NotificationModel


class SQLNotificationRepository(NotificationRepository):
    def __init__(self, session: Session):
        self.session = session

    def save_many(self, notifications: list[Notification]) -> list[Notification]:
        notification_models = [NotificationModel(**notification.model_dump()) for notification in notifications]
        self.session.add_all(notification_models)
        self.session.commit()

        for notification_model in notification_models:
            self.session.refresh(notification_model)

        return [self._to_domain(notification_model) for notification_model in notification_models]

    def list_by_user(self, user_id: int, offset: int, limit: int) -> list[Notification]:
        statement = (
            select(NotificationModel)
            .where(NotificationModel.user_id == user_id)
            .order_by(desc(NotificationModel.created_at))
            .offset(offset)
            .limit(limit)
        )
        results = self.session.exec(statement).all()
        return [self._to_domain(result) for result in results]

    def mark_as_read(self, notification_id: int, user_id: int) -> Notification | None:
        statement = select(NotificationModel).where(
            NotificationModel.id == notification_id,
            NotificationModel.user_id == user_id,
        )
        notification_db = self.session.exec(statement).first()
        if notification_db is None:
            return None

        notification_db.is_read = True
        self.session.add(notification_db)
        self.session.commit()
        self.session.refresh(notification_db)
        return self._to_domain(notification_db)

    def find_recipients_nearby(
        self,
        latitude: float,
        longitude: float,
        radius_km: float,
        exclude_user_id: int,
    ) -> list[NotificationRecipient]:
        lat_delta = radius_km / 111.0
        safe_cos = max(abs(cos(radians(latitude))), 1e-6)
        long_delta = min(180.0, radius_km / (111.0 * safe_cos))

        inner = (
            func.cos(func.radians(latitude)) * func.cos(func.radians(UserModel.last_latitude))
            * func.cos(func.radians(UserModel.last_longitude) - func.radians(longitude))
            + func.sin(func.radians(latitude)) * func.sin(func.radians(UserModel.last_latitude))
        )
        clamped_inner = func.least(1.0, func.greatest(-1.0, inner))
        distance_km = 6371.0 * func.acos(clamped_inner)

        statement = select(UserModel.id, UserModel.fcm_token).where(
            and_(
                UserModel.id != exclude_user_id,
                UserModel.fcm_token.is_not(None),
                UserModel.last_latitude.is_not(None),
                UserModel.last_longitude.is_not(None),
                UserModel.last_latitude >= latitude - lat_delta,
                UserModel.last_latitude <= latitude + lat_delta,
                UserModel.last_longitude >= longitude - long_delta,
                UserModel.last_longitude <= longitude + long_delta,
                distance_km <= radius_km,
            )
        )
        rows = self.session.exec(statement).all()
        return [
            NotificationRecipient(user_id=row[0], fcm_token=row[1])
            for row in rows
            if row[1]
        ]

    def _to_domain(self, model: NotificationModel) -> Notification:
        return Notification(**model.model_dump())