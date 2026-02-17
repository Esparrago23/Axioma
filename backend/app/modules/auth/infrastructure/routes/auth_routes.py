from fastapi import APIRouter, Depends
from app.modules.auth.infrastructure.dtos import UserCreateDTO, LoginDTO
from app.modules.auth.infrastructure.dependencies import get_register_controller, get_login_controller

router = APIRouter(prefix="/auth", tags=["Auth"])

@router.post("/register", status_code=201)
def register(
    data: UserCreateDTO,
    controller = Depends(get_register_controller)
):
    return controller.run(data)

@router.post("/login")
def login(
    data: LoginDTO,
    controller = Depends(get_login_controller)
):
    return controller.run(data)