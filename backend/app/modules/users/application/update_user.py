from fastapi import HTTPException
from app.modules.users.domain.repository import UserRepository
from app.modules.users.domain.entities import User

class UpdateUserUseCase:
    def __init__(self, repository: UserRepository):
        self.repository = repository

    def execute(self, user_id: int, new_username: str | None) -> User:
        user = self.repository.get_by_id(user_id)
        if not user:
            raise HTTPException(status_code=404, detail="Usuario no encontrado")
        
        # Lógica de actualización
        if new_username:
            user.username = new_username
            
        return self.repository.update(user)