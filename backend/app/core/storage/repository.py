from abc import ABC, abstractmethod
from typing import BinaryIO


class StorageRepository(ABC):
    @abstractmethod
    def upload_file(self, file_obj: BinaryIO, object_name: str, content_type: str) -> str:
        """Upload a file and return a public URL."""
        raise NotImplementedError

    @abstractmethod
    def delete_file(self, object_name: str) -> None:
        """Delete an object from storage if it exists."""
        raise NotImplementedError
