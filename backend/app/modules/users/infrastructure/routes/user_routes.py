from fastapi import APIRouter, Depends, Response, status
from app.modules.users.infrastructure.dtos import UserResponseDTO, UserUpdateDTO
from app.modules.users.infrastructure.dependencies import (
    get_user_controller, 
    update_user_controller, 
    delete_user_controller
)
from app.modules.auth.infrastructure.dependencies import get_current_user
from app.modules.auth.domain.entities import User

router = APIRouter(prefix="/users", tags=["Users"])


@router.get("/me", response_model=UserResponseDTO)
def get_my_profile(
    current_user: User = Depends(get_current_user)
):
    return UserResponseDTO(
        id=current_user.id,
        username=current_user.username,
        email=current_user.email,
        reputation_score=current_user.reputation_score,
        created_at=current_user.created_at
    )

@router.patch("/me", response_model=UserResponseDTO)
def update_my_profile(
    data: UserUpdateDTO,
    controller = Depends(update_user_controller),
    current_user: User = Depends(get_current_user)
):
    return controller.run(current_user.id, data)

@router.delete("/me", status_code=status.HTTP_204_NO_CONTENT)
def delete_my_profile(
    controller = Depends(delete_user_controller),
    current_user: User = Depends(get_current_user)
):
    controller.run(current_user.id)
    return Response(status_code=status.HTTP_204_NO_CONTENT)