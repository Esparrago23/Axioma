from abc import ABC, abstractmethod
from typing import Optional
from app.modules.auth.domain.entities import RefreshTokenSession, User

class UserRepository(ABC):
    @abstractmethod
    def save(self, user: User) -> User:
        pass

    @abstractmethod
    def get_by_email(self, email: str) -> Optional[User]:
        pass
    
    @abstractmethod
    def get_by_username(self, username: str) -> Optional[User]:
        pass
    @abstractmethod
    def get_by_id(self, id: int) -> Optional[User]:
        pass

    @abstractmethod
    def save_refresh_session(self, session: RefreshTokenSession) -> RefreshTokenSession:
        pass

    @abstractmethod
    def get_refresh_session(self, token_hash: str) -> Optional[RefreshTokenSession]:
        pass

    @abstractmethod
    def revoke_refresh_session(self, token_hash: str) -> None:
        pass