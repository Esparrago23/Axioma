from fastapi import HTTPException, status

from app.core.security import decode_token, hash_token
from app.modules.auth.domain.repository import UserRepository


class LogoutSessionUseCase:
    def __init__(self, repository: UserRepository):
        self.repository = repository

    def execute(self, refresh_token: str) -> None:
        try:
            payload = decode_token(refresh_token)
            if payload.get("type") != "refresh":
                raise ValueError("invalid_token_type")
        except Exception as exc:
            raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Refresh token invalido") from exc

        token_hash = hash_token(refresh_token)
        self.repository.revoke_refresh_session(token_hash)
