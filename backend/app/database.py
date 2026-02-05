from sqlmodel import create_engine, SQLModel

DATABASE_URL = ""

engine = create_engine(DATABASE_URL, echo=True)