from fastapi import HTTPException
from app.modules.users.domain.repository import UserRepository
from app.modules.users.domain.entities import User

class GetUserUseCase:
    def __init__(self, repository: UserRepository):
        self.repository = repository

    def execute(self, user_id: int) -> User:
        user = self.repository.get_by_id(user_id)
        if not user:
            raise HTTPException(status_code=404, detail="Usuario no encontrado")
        return user