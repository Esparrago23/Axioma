from typing import Annotated
from fastapi import Depends, HTTPException, status
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials 
from jose import JWTError, jwt
from sqlmodel import Session

from app.core.config import settings
from app.core.database import get_session

from app.modules.auth.infrastructure.persistence.sql_repository import SQLUserRepository
from app.modules.auth.domain.entities import User

from app.modules.auth.application.register_user import RegisterUserUseCase
from app.modules.auth.application.login_user import LoginUserUseCase
from app.modules.auth.application.refresh_session import RefreshSessionUseCase
from app.modules.auth.application.logout_session import LogoutSessionUseCase
from app.modules.auth.infrastructure.controllers.register_controller import RegisterController
from app.modules.auth.infrastructure.controllers.login_controller import LoginController
from app.modules.auth.infrastructure.controllers.refresh_controller import RefreshSessionController
from app.modules.auth.infrastructure.controllers.logout_controller import LogoutSessionController


security = HTTPBearer()

def get_auth_repo(session: Session = Depends(get_session)) -> SQLUserRepository:
    return SQLUserRepository(session)

def get_register_uc(repo = Depends(get_auth_repo)): return RegisterUserUseCase(repo)
def get_login_uc(repo = Depends(get_auth_repo)): return LoginUserUseCase(repo)
def get_refresh_uc(repo = Depends(get_auth_repo)): return RefreshSessionUseCase(repo)
def get_logout_uc(repo = Depends(get_auth_repo)): return LogoutSessionUseCase(repo)
def get_register_controller(uc = Depends(get_register_uc)): return RegisterController(uc)
def get_login_controller(uc = Depends(get_login_uc)): return LoginController(uc)
def get_refresh_controller(uc = Depends(get_refresh_uc)): return RefreshSessionController(uc)
def get_logout_controller(uc = Depends(get_logout_uc)): return LogoutSessionController(uc)


def get_user_from_access_token(token: str, repo: SQLUserRepository) -> User:
    credentials_exception = HTTPException(
        status_code=status.HTTP_401_UNAUTHORIZED,
        detail="No se pudieron validar las credenciales",
        headers={"WWW-Authenticate": "Bearer"},
    )

    try:
        payload = jwt.decode(token, settings.SECRET_KEY, algorithms=[settings.ALGORITHM])
        if payload.get("type") != "access":
            raise credentials_exception

        user_id_str: str = payload.get("sub")
        if user_id_str is None:
            raise credentials_exception

        user_id = int(user_id_str)
    except (JWTError, ValueError):
        raise credentials_exception

    user = repo.get_by_id(user_id)
    if user is None:
        raise credentials_exception

    return user

def get_current_user(
    token_auth: HTTPAuthorizationCredentials = Depends(security), 
    repo: SQLUserRepository = Depends(get_auth_repo)
) -> User:
    return get_user_from_access_token(token_auth.credentials, repo)

UserDep = Annotated[User, Depends(get_current_user)]