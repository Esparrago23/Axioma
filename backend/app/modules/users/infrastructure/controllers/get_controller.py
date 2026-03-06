from app.modules.users.application.get_user import GetUserUseCase

class GetUserController:
    def __init__(self, use_case: GetUserUseCase):
        self.use_case = use_case

    def run(self, user_id: int):
        return self.use_case.execute(user_id)