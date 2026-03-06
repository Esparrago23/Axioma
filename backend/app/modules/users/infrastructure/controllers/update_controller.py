from app.modules.users.application.update_user import UpdateUserUseCase
from app.modules.users.infrastructure.dtos import UserUpdateDTO

class UpdateUserController:
    def __init__(self, use_case: UpdateUserUseCase):
        self.use_case = use_case

    def run(self, user_id: int, dto: UserUpdateDTO):
        return self.use_case.execute(user_id, dto.username)