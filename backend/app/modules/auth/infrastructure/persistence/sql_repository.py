from typing import Optional
from sqlmodel import Session, select
from app.modules.auth.domain.entities import User
from app.modules.auth.domain.repository import UserRepository
from app.modules.auth.infrastructure.persistence.models import UserModel

class SQLUserRepository(UserRepository):
    def __init__(self, session: Session):
        self.session = session

    def save(self, user: User) -> User:
        user_db = UserModel(**user.model_dump())
        self.session.add(user_db)
        self.session.commit()
        self.session.refresh(user_db)
        return User(**user_db.model_dump())

    def get_by_email(self, email: str) -> Optional[User]:
        statement = select(UserModel).where(UserModel.email == email)
        result = self.session.exec(statement).first()
        return User(**result.model_dump()) if result else None

    def get_by_username(self, username: str) -> Optional[User]:
        statement = select(UserModel).where(UserModel.username == username)
        result = self.session.exec(statement).first()
        return User(**result.model_dump()) if result else None