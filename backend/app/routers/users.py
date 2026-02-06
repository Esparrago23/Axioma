from typing import List
from fastapi import APIRouter, HTTPException, Query, status
from sqlmodel import select
from app.models import User
from app.schemas import UserRead, UserUpdate 
from app.deps import SessionDep, UserDep, get_password_hash

router = APIRouter()
@router.get("/", response_model=List[UserRead])
def read_all_users(
    session: SessionDep,
    offset: int = 0,
    limit: int = Query(default=100, le=100)
):
    """
    Lista todos los usuarios. 
    Limitado a 100.
    """
    users = session.exec(select(User).offset(offset).limit(limit)).all()
    return users

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

@router.delete("/me", status_code=status.HTTP_204_NO_CONTENT)
def delete_user_me(session: SessionDep, current_user: UserDep):
    """
    Rage Quit: Borra tu propia cuenta y todos tus datos asociados.
    No hay vuelta atrás, Bug.
    """
    session.delete(current_user)
    session.commit()
    return None

@router.delete("/{user_id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_user_by_id(user_id: int, session: SessionDep, current_user: UserDep):

    user_to_delete = session.get(User, user_id)
    
    if not user_to_delete:
        raise HTTPException(status_code=404, detail="Usuario no encontrado")
        
    session.delete(user_to_delete)
    session.commit()
    
    return None