from typing import List
from fastapi import APIRouter, HTTPException, Query
from sqlmodel import select
from app.models import User
from app.schemas import UserRead, UserUpdate 
from app.deps import SessionDep, UserDep, get_password_hash

router = APIRouter()

@router.get("/me", response_model=UserRead)
def read_user_me(current_user: UserDep):
    """Devuelve el perfil del usuario logueado."""
    return current_user

@router.patch("/me", response_model=UserRead)
def update_user_me(user_update: UserUpdate, session: SessionDep, current_user: UserDep):
    """Actualiza campos parciales del usuario logueado."""
    user_data = user_update.model_dump(exclude_unset=True)
    
    if "password" in user_data and user_data["password"]:
        hashed = get_password_hash(user_data["password"])
        current_user.hashed_password = hashed
        del user_data["password"] 

    current_user.sqlmodel_update(user_data)
    session.add(current_user)
    session.commit()
    session.refresh(current_user)
    return current_user