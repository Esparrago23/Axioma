from typing import Annotated
from fastapi import Depends, HTTPException, status
from fastapi.security import OAuth2PasswordBearer
from jose import JWTError, jwt
from sqlmodel import Session

from app.core.config import settings
from app.core.database import get_session

from app.modules.auth.infrastructure.persistence.sql_repository import SQLUserRepository
from app.modules.auth.domain.entities import User

from app.modules.auth.application.register_user import RegisterUserUseCase
from app.modules.auth.application.login_user import LoginUserUseCase
from app.modules.auth.infrastructure.controllers.register_controller import RegisterController
from app.modules.auth.infrastructure.controllers.login_controller import LoginController


oauth2_scheme = OAuth2PasswordBearer(tokenUrl="/auth/token")

def get_auth_repo(session: Session = Depends(get_session)) -> SQLUserRepository:
    return SQLUserRepository(session)

def get_register_uc(repo = Depends(get_auth_repo)): return RegisterUserUseCase(repo)
def get_login_uc(repo = Depends(get_auth_repo)): return LoginUserUseCase(repo)
def get_register_controller(uc = Depends(get_register_uc)): return RegisterController(uc)
def get_login_controller(uc = Depends(get_login_uc)): return LoginController(uc)

def get_current_user(
    token: str = Depends(oauth2_scheme),
    repo: SQLUserRepository = Depends(get_auth_repo)
) -> User:
    """
    Decodifica el token y recupera el usuario actual usando el Repositorio.
    """
    credentials_exception = HTTPException(
        status_code=status.HTTP_401_UNAUTHORIZED,
        detail="No se pudieron validar las credenciales",
        headers={"WWW-Authenticate": "Bearer"},
    )
    
    try:
        payload = jwt.decode(token, settings.SECRET_KEY, algorithms=[settings.ALGORITHM])
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

UserDep = Annotated[User, Depends(get_current_user)]