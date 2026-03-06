from app.modules.users.application.delete_user import DeleteUserUseCase

class DeleteUserController:
    def __init__(self, use_case: DeleteUserUseCase):
        self.use_case = use_case

    def run(self, user_id: int):
        return self.use_case.execute(user_id)