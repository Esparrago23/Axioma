from datetime import datetime, timedelta, timezone

from fastapi import HTTPException, status

from app.core.config import settings
from app.core.security import create_access_token, create_refresh_token, decode_token, hash_token
from app.modules.auth.domain.entities import RefreshTokenSession
from app.modules.auth.domain.repository import UserRepository
from app.modules.auth.infrastructure.dtos import TokenResponseDTO


class RefreshSessionUseCase:
    def __init__(self, repository: UserRepository):
        self.repository = repository

    def execute(self, refresh_token: str, device_name: str | None = None) -> TokenResponseDTO:
        try:
            payload = decode_token(refresh_token)
            if payload.get("type") != "refresh":
                raise ValueError("invalid_token_type")
            user_id = int(payload.get("sub"))
        except Exception as exc:
            raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Refresh token invalido") from exc

        token_hash = hash_token(refresh_token)
        session = self.repository.get_refresh_session(token_hash)
        if not session or session.revoked_at is not None:
            raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Sesion no valida")

        now = datetime.now(timezone.utc)
        expires_at = session.expires_at.replace(tzinfo=timezone.utc) if session.expires_at.tzinfo is None else session.expires_at
        if expires_at <= now:
            self.repository.revoke_refresh_session(token_hash)
            raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Refresh token expirado")

        user = self.repository.get_by_id(user_id)
        if not user:
            raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Usuario no encontrado")

        self.repository.revoke_refresh_session(token_hash)

        new_refresh_token = create_refresh_token(subject=user.id)
        new_refresh_hash = hash_token(new_refresh_token)
        new_expires_at = datetime.utcnow() + timedelta(days=settings.REFRESH_TOKEN_EXPIRE_DAYS)

        self.repository.save_refresh_session(
            RefreshTokenSession(
                user_id=user.id,
                token_hash=new_refresh_hash,
                expires_at=new_expires_at,
                device_name=device_name or session.device_name,
                last_used_at=datetime.utcnow(),
            )
        )

        access_token = create_access_token(subject=user.id)
        return TokenResponseDTO(
            access_token=access_token,
            refresh_token=new_refresh_token,
            token_type="bearer",
            user_id=user.id,
            username=user.username,
            reputation=user.reputation_score,
        )
