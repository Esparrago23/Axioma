from fastapi import HTTPException
from app.modules.users.domain.repository import UserRepository
from app.modules.users.domain.entities import User

class UpdateUserUseCase:
    def __init__(self, repository: UserRepository):
        self.repository = repository

    def execute(
        self,
        user_id: int,
        username: str | None,
        full_name: str | None,
        profile_picture_url: str | None
    ) -> User:
        user = self.repository.get_by_id(user_id)
        if not user:
            raise HTTPException(status_code=404, detail="Usuario no encontrado")

        if username is not None and username != user.username:
            existing_user = self.repository.get_by_username(username)
            if existing_user and existing_user.id != user_id:
                raise HTTPException(status_code=409, detail="El username ya esta en uso")
            user.username = username

        if full_name is not None:
            user.full_name = full_name

        if profile_picture_url is not None:
            user.profile_picture_url = profile_picture_url
            
        return self.repository.update(user)