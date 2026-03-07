from datetime import datetime, timedelta

from fastapi import HTTPException, status
from app.modules.auth.domain.repository import UserRepository
from app.core.config import settings
from app.core.security import verify_password, create_access_token, create_refresh_token, hash_token
from app.modules.auth.domain.entities import RefreshTokenSession
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
        refresh_token = create_refresh_token(subject=user.id)

        self.repository.save_refresh_session(
            RefreshTokenSession(
                user_id=user.id,
                token_hash=hash_token(refresh_token),
                expires_at=datetime.utcnow() + timedelta(days=settings.REFRESH_TOKEN_EXPIRE_DAYS),
            )
        )
        
        return TokenResponseDTO(
            access_token=access_token,
            refresh_token=refresh_token,
            token_type="bearer",
            user_id=user.id,
            username=user.username,
            reputation=user.reputation_score
        )