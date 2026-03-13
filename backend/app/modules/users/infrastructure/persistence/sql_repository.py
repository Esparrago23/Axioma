from typing import Optional
from sqlmodel import Session, select
from app.modules.users.domain.repository import UserRepository
from app.modules.users.domain.entities import User
from app.modules.auth.infrastructure.persistence.models import UserModel

class SQLUserRepository(UserRepository):
    def __init__(self, session: Session):
        self.session = session

    def get_by_id(self, id: int) -> Optional[User]:
        user_db = self.session.get(UserModel, id)
        return self._to_domain(user_db) if user_db else None

    def get_by_username(self, username: str) -> Optional[User]:
        statement = select(UserModel).where(UserModel.username == username)
        user_db = self.session.exec(statement).first()
        return self._to_domain(user_db) if user_db else None

    def update(self, user: User) -> User:
        user_db = self.session.get(UserModel, user.id)
        if user_db:
            user_db.username = user.username
            user_db.full_name = user.full_name
            user_db.profile_picture_url = user.profile_picture_url
            user_db.fcm_token = user.fcm_token
            user_db.last_latitude = user.last_latitude
            user_db.last_longitude = user.last_longitude
            
            self.session.add(user_db)
            self.session.commit()
            self.session.refresh(user_db)
            return self._to_domain(user_db)
        return user

    def delete(self, user_id: int) -> bool:
        user_db = self.session.get(UserModel, user_id)
        if user_db:
            self.session.delete(user_db)
            self.session.commit()
            return True
        return False

    def _to_domain(self, model: UserModel) -> User:
        # Convertimos el modelo de BD a Entidad de Dominio
        return User(**model.model_dump())