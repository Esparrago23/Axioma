from app.core.storage.repository import StorageRepository
from app.core.storage.s3_repository import S3StorageRepository


def get_storage_repository() -> StorageRepository:
    return S3StorageRepository()
