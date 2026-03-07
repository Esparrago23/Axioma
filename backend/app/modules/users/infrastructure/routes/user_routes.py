from uuid import uuid4
from urllib.parse import urlparse
from fastapi import APIRouter, Depends, File, HTTPException, Response, UploadFile, status
from app.modules.users.infrastructure.dtos import UserResponseDTO, UserUpdateDTO
from app.modules.users.infrastructure.dependencies import (
    get_user_controller, 
    update_user_controller, 
    delete_user_controller
)
from app.modules.auth.infrastructure.dependencies import get_current_user
from app.modules.auth.domain.entities import User
from app.core.config import settings
from app.core.storage.dependencies import get_storage_repository
from app.core.storage.repository import StorageRepository

router = APIRouter(prefix="/users", tags=["Users"])


def _extract_object_name_from_public_url(url: str | None) -> str | None:
    if not url:
        return None

    parsed = urlparse(url)
    path = parsed.path.lstrip("/")
    bucket_prefix = f"{settings.AWS_BUCKET_NAME}/"
    if path.startswith(bucket_prefix):
        object_name = path[len(bucket_prefix):]
        return object_name or None
    return None


@router.get("/me", response_model=UserResponseDTO)
def get_my_profile(
    current_user: User = Depends(get_current_user)
):
    return UserResponseDTO(
        id=current_user.id,
        username=current_user.username,
        email=current_user.email,
        reputation_score=current_user.reputation_score,
        full_name=current_user.full_name,
        profile_picture_url=current_user.profile_picture_url,
        created_at=current_user.created_at
    )

@router.patch("/me", response_model=UserResponseDTO)
def update_my_profile(
    data: UserUpdateDTO,
    controller = Depends(update_user_controller),
    current_user: User = Depends(get_current_user)
):
    return controller.run(current_user.id, data)

@router.post("/me/photo", response_model=UserResponseDTO)
def upload_my_profile_photo(
    photo: UploadFile = File(...),
    storage_repo: StorageRepository = Depends(get_storage_repository),
    controller = Depends(update_user_controller),
    current_user: User = Depends(get_current_user)
):
    if not photo.content_type or not photo.content_type.startswith("image/"):
        raise HTTPException(status_code=400, detail="El archivo debe ser una imagen")

    old_object_name = _extract_object_name_from_public_url(current_user.profile_picture_url)

    filename = photo.filename or "profile.jpg"
    extension = filename.rsplit(".", 1)[-1].lower() if "." in filename else "jpg"
    object_name = f"profile_pictures/{current_user.id}_{uuid4().hex}.{extension}"

    try:
        public_url = storage_repo.upload_file(
            file_obj=photo.file,
            object_name=object_name,
            content_type=photo.content_type,
        )
    except RuntimeError:
        raise HTTPException(status_code=500, detail="No se pudo subir la imagen")
    finally:
        photo.file.close()

    updated_user = controller.run(
        current_user.id,
        UserUpdateDTO(profile_picture_url=public_url)
    )

    if old_object_name and old_object_name != object_name:
        storage_repo.delete_file(old_object_name)

    return updated_user

@router.delete("/me", status_code=status.HTTP_204_NO_CONTENT)
def delete_my_profile(
    controller = Depends(delete_user_controller),
    current_user: User = Depends(get_current_user)
):
    controller.run(current_user.id)
    return Response(status_code=status.HTTP_204_NO_CONTENT)