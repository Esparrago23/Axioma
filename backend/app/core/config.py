from pydantic_settings import BaseSettings, SettingsConfigDict

class Settings(BaseSettings):
    # Base de Datos
    POSTGRES_USER: str
    POSTGRES_PASSWORD: str
    POSTGRES_DB: str
    DATABASE_URL: str

    # Seguridad JWT
    SECRET_KEY: str
    ALGORITHM: str = "HS256" 
    ACCESS_TOKEN_EXPIRE_MINUTES: int = 30 

    # Object storage (S3 compatible)
    AWS_ACCESS_KEY_ID: str 
    AWS_SECRET_ACCESS_KEY: str 
    AWS_REGION: str 
    AWS_BUCKET_NAME: str 
    AWS_ENDPOINT_URL: str | None 
    AWS_PUBLIC_ENDPOINT_URL: str 
    
    # Configuración para leer el archivo .env automáticamente
    model_config = SettingsConfigDict(
        env_file=".env", 
        env_file_encoding="utf-8",
        extra="ignore" 
    )


settings = Settings()