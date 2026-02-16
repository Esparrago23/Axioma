from pydantic import BaseModel, EmailStr

class UserCreateDTO(BaseModel):
    username: str
    email: EmailStr
    password: str

class LoginDTO(BaseModel):
    email: EmailStr
    password: str