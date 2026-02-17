from fastapi import HTTPException
from app.modules.users.domain.repository import UserRepository

class DeleteUserUseCase:
    def __init__(self, repository: UserRepository):
        self.repository = repository

    def execute(self, user_id: int):
        if not self.repository.delete(user_id):
            raise HTTPException(status_code=404, detail="Usuario no encontrado")