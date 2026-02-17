from typing import Optional
from sqlmodel import Session
from app.modules.users.domain.repository import UserRepository
from app.modules.users.domain.entities import User
from app.modules.users.infrastructure.persistence.models import UserModel

class SQLUserRepository(UserRepository):
    def __init__(self, session: Session):
        self.session = session

    def get_by_id(self, id: int) -> Optional[User]:
        user_db = self.session.get(UserModel, id)
        return self._to_domain(user_db) if user_db else None

    def update(self, user: User) -> User:
        user_db = self.session.get(UserModel, user.id)
        if user_db:
            # Actualizamos campos
            user_db.username = user.username
            user_db.reputation_score = user.reputation_score
            # Nota: No actualizamos password aquí (eso iría en otro método específico si quisieras)
            
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