from typing import Optional, List
from datetime import datetime
from enum import Enum
from sqlmodel import Field, SQLModel, Relationship


class CategoryEnum(str, Enum):
    INFRAESTRUCTURA = "INFRAESTRUCTURA"
    SEGURIDAD = "SEGURIDAD"
    SANITIZACION = "SANITIZACION"
    VANDALISMO = "VANDALISMO"
    SOCIAL = "SOCIAL"

class ReportStatus(str, Enum):
    ACTIVE = "ACTIVE"
    HIDDEN = "HIDDEN"   
    RESOLVED = "RESOLVED"

class VoteType(int, Enum):
    UP = 1
    DOWN = -1

# --- MODELOS (Tablas) ---

class User(SQLModel, table=True):
    id: Optional[int] = Field(default=None, primary_key=True)
    username: str = Field(index=True, unique=True)
    email: str = Field(index=True, unique=True)
    hashed_password: str
    reputation_score: int = Field(default=10) # Empiezan con 10.
    created_at: datetime = Field(default_factory=datetime.utcnow)

    # Relaciones (La magia del ORM)
    reports: List["Report"] = Relationship(back_populates="user")
    votes: List["Vote"] = Relationship(back_populates="user")

class Report(SQLModel, table=True):
    id: Optional[int] = Field(default=None, primary_key=True)
    title: str
    description: str
    category: CategoryEnum
    latitude: float = Field(index=True) # Indexado porque la búsqueda geográfica es cara
    longitude: float = Field(index=True)
    photo_url: Optional[str] = None
    
    # Lógica de Negocio
    credibility_score: int = Field(default=0)
    status: ReportStatus = Field(default=ReportStatus.ACTIVE)
    created_at: datetime = Field(default_factory=datetime.utcnow)

    # Foreign Key
    user_id: Optional[int] = Field(default=None, foreign_key="user.id")
    
    # Relaciones
    user: Optional[User] = Relationship(back_populates="reports")
    votes: List["Vote"] = Relationship(back_populates="report")

class Vote(SQLModel, table=True):
    id: Optional[int] = Field(default=None, primary_key=True)
    vote_value: int # Usamos int para sumar fácil, validado por Enum en el Pydantic schema
    
    # Foreign Keys
    user_id: Optional[int] = Field(default=None, foreign_key="user.id")
    report_id: Optional[int] = Field(default=None, foreign_key="report.id")

    # Relaciones
    user: Optional[User] = Relationship(back_populates="votes")
    report: Optional[Report] = Relationship(back_populates="votes")