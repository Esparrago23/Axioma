from pydantic import BaseModel, EmailStr
from typing import Optional
class UserCreateDTO(BaseModel):
    username: Optional[str] = None
    email: EmailStr
    password: str

class LoginDTO(BaseModel):
    email: EmailStr
    password: str

class TokenResponseDTO(BaseModel):
    access_token: str
    token_type: str = "bearer"
    user_id: int       
    username: str      
    reputation: int
    
class UserResponseDTO(BaseModel):
    id: int
    username: str
    email: str
    reputation_score: int