from abc import ABC, abstractmethod
from typing import Optional
from app.modules.auth.domain.entities import User

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