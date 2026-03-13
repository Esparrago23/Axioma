from fastapi import HTTPException

from app.modules.users.domain.entities import User
from app.modules.users.domain.repository import UserRepository


class UpdateFcmTokenUseCase:
    def __init__(self, repository: UserRepository):
        self.repository = repository

    def execute(
        self,
        user_id: int,
        has_fcm_token: bool,
        fcm_token: str | None,
        has_location: bool,
        last_latitude: float | None,
        last_longitude: float | None,
    ) -> User:
        user = self.repository.get_by_id(user_id)
        if not user:
            raise HTTPException(status_code=404, detail="Usuario no encontrado")

        if not has_fcm_token and not has_location:
            raise HTTPException(status_code=400, detail="Debes enviar fcm_token o ubicacion")

        if has_fcm_token:
            user.fcm_token = fcm_token

            if fcm_token is None:
                user.last_latitude = None
                user.last_longitude = None
                return self.repository.update(user)

        if has_location:
            if user.fcm_token is None:
                raise HTTPException(status_code=400, detail="No puedes guardar ubicacion sin un fcm_token registrado")
            user.last_latitude = last_latitude
            user.last_longitude = last_longitude

        return self.repository.update(user)