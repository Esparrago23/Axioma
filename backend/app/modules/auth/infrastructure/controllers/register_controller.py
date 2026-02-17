from app.modules.auth.application.register_user import RegisterUserUseCase
from app.modules.auth.infrastructure.dtos import UserCreateDTO

class RegisterController:
    def __init__(self, use_case: RegisterUserUseCase):
        self.use_case = use_case

    def run(self, data: UserCreateDTO):
        username_final = data.username or data.email.split("@")[0]
        
        return self.use_case.execute(username_final, data.email, data.password)