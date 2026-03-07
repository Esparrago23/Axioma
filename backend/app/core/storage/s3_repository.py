from typing import BinaryIO
import logging

import boto3
from botocore.config import Config
from botocore.exceptions import BotoCoreError, ClientError

from app.core.config import settings
from app.core.storage.repository import StorageRepository

logger = logging.getLogger(__name__)


class S3StorageRepository(StorageRepository):
    def __init__(self) -> None:
        self.s3_client = boto3.client(
            "s3",
            endpoint_url=settings.AWS_ENDPOINT_URL,
            aws_access_key_id=settings.AWS_ACCESS_KEY_ID,
            aws_secret_access_key=settings.AWS_SECRET_ACCESS_KEY,
            region_name=settings.AWS_REGION,
            config=Config(s3={"addressing_style": "path"}),
        )
        self.bucket_name = settings.AWS_BUCKET_NAME
        self._ensure_bucket_exists()

    def _ensure_bucket_exists(self) -> None:
        try:
            self.s3_client.head_bucket(Bucket=self.bucket_name)
            return
        except ClientError as exc:
            code = str(exc.response.get("Error", {}).get("Code", ""))
            if code not in {"404", "NoSuchBucket", "NotFound"}:
                raise RuntimeError("Error validating storage bucket") from exc

        try:
            self.s3_client.create_bucket(Bucket=self.bucket_name)
        except ClientError as exc:
            code = str(exc.response.get("Error", {}).get("Code", ""))
            if code not in {"BucketAlreadyOwnedByYou", "BucketAlreadyExists"}:
                raise RuntimeError("Error creating storage bucket") from exc

    def upload_file(self, file_obj: BinaryIO, object_name: str, content_type: str) -> str:
        try:
            self.s3_client.upload_fileobj(
                file_obj,
                self.bucket_name,
                object_name,
                ExtraArgs={"ContentType": content_type},
            )
        except (ClientError, BotoCoreError) as exc:
            logger.exception("Storage upload failed for object '%s'", object_name)
            raise RuntimeError("Error uploading file to S3-compatible storage") from exc

        public_base_url = settings.AWS_PUBLIC_ENDPOINT_URL.rstrip("/")
        return f"{public_base_url}/{self.bucket_name}/{object_name}"

    def delete_file(self, object_name: str) -> None:
        try:
            self.s3_client.delete_object(Bucket=self.bucket_name, Key=object_name)
        except (ClientError, BotoCoreError) as exc:
            logger.warning("Storage delete failed for object '%s': %s", object_name, exc)
