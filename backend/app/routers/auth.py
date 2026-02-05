from fastapi import APIRouter, HTTPException, status
from sqlmodel import select
from app.models import User
from app.schemas import LoginRequest, TokenResponse, UserRead 
from app.deps import SessionDep, get_password_hash, create_access_token, verify_password

router = APIRouter()

@router.post("/register", response_model=UserRead)
def register(user_data: LoginRequest, session: SessionDep):
    existing_user = session.exec(select(User).where(User.email == user_data.email)).first()
    if existing_user:
        raise HTTPException(status_code=400, detail="El correo ya está registrado")
    
    new_user = User(
        email=user_data.email, 
        username=user_data.email.split("@")[0], 
        hashed_password=get_password_hash(user_data.password)
    )
    session.add(new_user)
    session.commit()
    session.refresh(new_user)
    
    return new_user

@router.post("/login", response_model=TokenResponse)
def login(user_data: LoginRequest, session: SessionDep):
    user = session.exec(select(User).where(User.email == user_data.email)).first()
    if not user or not verify_password(user_data.password, user.hashed_password):
        raise HTTPException(status_code=400, detail="Credenciales incorrectas")
    
    access_token = create_access_token(data={"sub": user.email})
    return TokenResponse(
        access_token=access_token, 
        user_id=user.id, 
        username=user.username,
        reputation=user.reputation_score
    )