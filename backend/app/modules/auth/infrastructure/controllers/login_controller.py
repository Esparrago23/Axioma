from app.modules.auth.application.login_user import LoginUserUseCase
from app.modules.auth.infrastructure.dtos import LoginDTO

class LoginController:
    def __init__(self, use_case: LoginUserUseCase):
        self.use_case = use_case

    def run(self, data: LoginDTO):
        return self.use_case.execute(data.email, data.password)