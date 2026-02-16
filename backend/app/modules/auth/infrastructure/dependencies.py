from fastapi import Depends
from sqlmodel import Session
from app.core.database import get_session
from backend.app.modules.auth.infrastructure.persistence.sql_repository import SQLUserRepository
from app.modules.auth.application.register_user import RegisterUserUseCase
from app.modules.auth.application.login_user import LoginUserUseCase
from app.modules.auth.infrastructure.controllers.register_controller import RegisterController
from app.modules.auth.infrastructure.controllers.login_controller import LoginController

def get_auth_repo(session: Session = Depends(get_session)):
    return SQLUserRepository(session)

def get_register_uc(repo = Depends(get_auth_repo)):
    return RegisterUserUseCase(repo)

def get_login_uc(repo = Depends(get_auth_repo)):
    return LoginUserUseCase(repo)

def get_register_controller(uc = Depends(get_register_uc)):
    return RegisterController(uc)

def get_login_controller(uc = Depends(get_login_uc)):
    return LoginController(uc)