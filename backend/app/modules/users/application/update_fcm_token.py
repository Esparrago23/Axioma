from fastapi import HTTPException

from app.modules.users.domain.entities import User
from app.modules.users.domain.repository import UserRepository


class UpdateFcmTokenUseCase:
    def __init__(self, repository: UserRepository):
        self.repository = repository

    def execute(
        self,
        user_id: int,
        fcm_token: str | None,
        last_latitude: float | None,
        last_longitude: float | None,
    ) -> User:
        user = self.repository.get_by_id(user_id)
        if not user:
            raise HTTPException(status_code=404, detail="Usuario no encontrado")

        user.fcm_token = fcm_token

        if fcm_token is None:
            user.last_latitude = None
            user.last_longitude = None
        else:
            user.last_latitude = last_latitude
            user.last_longitude = last_longitude

        return self.repository.update(user)