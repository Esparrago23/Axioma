from app.modules.auth.domain.repository import UserRepository
from app.modules.auth.domain.entities import User
from app.core.security import get_password_hash 
from fastapi import HTTPException, status

class RegisterUserUseCase:
    def __init__(self, repository: UserRepository):
        self.repository = repository

    def execute(self, username: str, email: str, password: str) -> User:
        if self.repository.get_by_email(email):
            raise HTTPException(status_code=400, detail="El email ya está registrado")
        if self.repository.get_by_username(username):
            raise HTTPException(status_code=400, detail="El usuario ya existe")

        hashed_pwd = get_password_hash(password)

        new_user = User(
            username=username, 
            email=email, 
            hashed_password=hashed_pwd
        )

        return self.repository.save(new_user)