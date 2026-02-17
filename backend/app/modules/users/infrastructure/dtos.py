from pydantic import BaseModel
from datetime import datetime

# Para actualizar, solo permitimos cambiar el username por ahora
class UserUpdateDTO(BaseModel):
    username: str | None = None

# Para mostrar al usuario (SIN password)
class UserResponseDTO(BaseModel):
    id: int
    username: str
    email: str
    reputation_score: int
    created_at: datetime