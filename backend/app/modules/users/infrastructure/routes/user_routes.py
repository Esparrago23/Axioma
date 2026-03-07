from pathlib import Path
from uuid import uuid4
from fastapi import APIRouter, Depends, File, HTTPException, Request, Response, UploadFile, status
from app.modules.users.infrastructure.dtos import UserResponseDTO, UserUpdateDTO
from app.modules.users.infrastructure.dependencies import (
    get_user_controller, 
    update_user_controller, 
    delete_user_controller
)
from app.modules.auth.infrastructure.dependencies import get_current_user
from app.modules.auth.domain.entities import User
import shutil

router = APIRouter(prefix="/users", tags=["Users"])
APP_DIR = Path(__file__).resolve().parents[4]
PROFILE_PICTURES_DIR = APP_DIR / "static" / "profile_pictures"


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
    request: Request,
    photo: UploadFile = File(...),
    controller = Depends(update_user_controller),
    current_user: User = Depends(get_current_user)
):
    if not photo.content_type or not photo.content_type.startswith("image/"):
        raise HTTPException(status_code=400, detail="El archivo debe ser una imagen")

    PROFILE_PICTURES_DIR.mkdir(parents=True, exist_ok=True)
    extension = Path(photo.filename or "profile.jpg").suffix or ".jpg"
    filename = f"{current_user.id}_{uuid4().hex}{extension}"
    file_path = PROFILE_PICTURES_DIR / filename

    try:
        with file_path.open("wb") as buffer:
            shutil.copyfileobj(photo.file, buffer)
    except OSError:
        raise HTTPException(status_code=500, detail="No se pudo guardar la imagen")
    finally:
        photo.file.close()

    base_url = str(request.base_url).rstrip("/")
    public_url = f"{base_url}/static/profile_pictures/{filename}"

    return controller.run(
        current_user.id,
        UserUpdateDTO(profile_picture_url=public_url)
    )

@router.delete("/me", status_code=status.HTTP_204_NO_CONTENT)
def delete_my_profile(
    controller = Depends(delete_user_controller),
    current_user: User = Depends(get_current_user)
):
    controller.run(current_user.id)
    return Response(status_code=status.HTTP_204_NO_CONTENT)