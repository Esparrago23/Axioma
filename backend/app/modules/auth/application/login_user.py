from app.modules.auth.domain.repository import UserRepository
from app.core.security import verify_password, create_access_token
from fastapi import HTTPException, status
from pydantic import BaseModel

class TokenResponse(BaseModel):
    access_token: str
    token_type: str

class LoginUserUseCase:
    def __init__(self, repository: UserRepository):
        self.repository = repository

    def execute(self, email: str, password: str) -> TokenResponse:
        user = self.repository.get_by_email(email)
        
        if not user or not verify_password(password, user.hashed_password):
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="Credenciales incorrectas",
                headers={"WWW-Authenticate": "Bearer"},
            )
        
        # Crear token
        access_token = create_access_token(subject=user.id)
        return TokenResponse(access_token=access_token, token_type="bearer")