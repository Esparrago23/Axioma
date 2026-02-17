from fastapi import HTTPException, status
from app.modules.auth.domain.repository import UserRepository
from app.core.security import verify_password, create_access_token
from app.modules.auth.infrastructure.dtos import TokenResponseDTO 

class LoginUserUseCase:
    def __init__(self, repository: UserRepository):
        self.repository = repository

    def execute(self, email: str, password: str) -> TokenResponseDTO:
        user = self.repository.get_by_email(email)
        
        if not user or not verify_password(password, user.hashed_password):
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="Credenciales incorrectas",
                headers={"WWW-Authenticate": "Bearer"},
            )
        
        access_token = create_access_token(subject=user.id)
        
        return TokenResponseDTO(
            access_token=access_token,
            token_type="bearer",
            user_id=user.id,
            username=user.username,
            reputation=user.reputation_score
        )