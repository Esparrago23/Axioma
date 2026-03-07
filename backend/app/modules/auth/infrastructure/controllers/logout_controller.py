from app.modules.auth.application.logout_session import LogoutSessionUseCase
from app.modules.auth.infrastructure.dtos import LogoutRequestDTO


class LogoutSessionController:
    def __init__(self, use_case: LogoutSessionUseCase):
        self.use_case = use_case

    def run(self, data: LogoutRequestDTO):
        self.use_case.execute(refresh_token=data.refresh_token)
        return {"message": "Sesion cerrada"}
