from app.modules.auth.application.refresh_session import RefreshSessionUseCase
from app.modules.auth.infrastructure.dtos import RefreshTokenRequestDTO


class RefreshSessionController:
    def __init__(self, use_case: RefreshSessionUseCase):
        self.use_case = use_case

    def run(self, data: RefreshTokenRequestDTO):
        return self.use_case.execute(
            refresh_token=data.refresh_token,
            device_name=data.device_name,
        )
