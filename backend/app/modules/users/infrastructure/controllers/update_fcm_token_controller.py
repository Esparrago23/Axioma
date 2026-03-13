from app.modules.users.application.update_fcm_token import UpdateFcmTokenUseCase
from app.modules.users.infrastructure.dtos import UpdateFcmTokenDTO


class UpdateFcmTokenController:
    def __init__(self, use_case: UpdateFcmTokenUseCase):
        self.use_case = use_case

    def run(self, user_id: int, dto: UpdateFcmTokenDTO):
        return self.use_case.execute(
            user_id=user_id,
            has_fcm_token=dto.has_fcm_token,
            fcm_token=dto.fcm_token,
            has_location=dto.has_location,
            last_latitude=dto.last_latitude,
            last_longitude=dto.last_longitude,
        )