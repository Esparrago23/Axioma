from fastapi import Depends
from sqlmodel import Session
from app.core.database import get_session
from app.modules.users.infrastructure.persistence.sql_repository import SQLUserRepository

# Use Cases
from app.modules.users.application.get_user import GetUserUseCase
from app.modules.users.application.update_user import UpdateUserUseCase
from app.modules.users.application.delete_user import DeleteUserUseCase

# Controllers
from app.modules.users.infrastructure.controllers.get_controller import GetUserController
from app.modules.users.infrastructure.controllers.update_controller import UpdateUserController
from app.modules.users.infrastructure.controllers.delete_controller import DeleteUserController

# 1. Repo
def get_user_repo(session: Session = Depends(get_session)) -> SQLUserRepository:
    return SQLUserRepository(session)

# 2. Use Cases
def get_user_uc(repo=Depends(get_user_repo)): return GetUserUseCase(repo)
def update_user_uc(repo=Depends(get_user_repo)): return UpdateUserUseCase(repo)
def delete_user_uc(repo=Depends(get_user_repo)): return DeleteUserUseCase(repo)

# 3. Controllers
def get_user_controller(uc=Depends(get_user_uc)): return GetUserController(uc)
def update_user_controller(uc=Depends(update_user_uc)): return UpdateUserController(uc)
def delete_user_controller(uc=Depends(delete_user_uc)): return DeleteUserController(uc)