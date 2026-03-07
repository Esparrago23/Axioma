from sqlmodel import SQLModel, Session, create_engine
from sqlalchemy import text
from app.core.config import settings

# echo=True es útil para ver los SQL logs en desarrollo (bórralo en prod)
engine = create_engine(settings.DATABASE_URL, echo=True)

def get_session():
    """Dependencia para obtener sesión de DB en los endpoints"""
    with Session(engine) as session:
        yield session

def init_db():
    """Función para crear las tablas al iniciar la app"""
    SQLModel.metadata.create_all(engine)

    if engine.dialect.name != "postgresql":
        return

    # Patch de esquema para entornos existentes sin migraciones formales.
    with engine.begin() as conn:
        conn.execute(
            text(
                """
                ALTER TABLE users
                ADD COLUMN IF NOT EXISTS profile_picture_url VARCHAR;
                """
            )
        )
        conn.execute(
            text(
                """
                ALTER TABLE users
                ADD COLUMN IF NOT EXISTS full_name VARCHAR;
                """
            )
        )