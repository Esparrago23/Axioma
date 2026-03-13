from fastapi import APIRouter, Depends
from app.modules.auth.infrastructure.dtos import (
    LoginDTO,
    LogoutRequestDTO,
    RefreshTokenRequestDTO,
    TokenResponseDTO,
    UserCreateDTO,
    UserResponseDTO,
)
from app.modules.auth.infrastructure.dependencies import (
    get_login_controller,
    get_logout_controller,
    get_refresh_controller,
    get_register_controller,
)

router = APIRouter(prefix="/auth", tags=["Auth"])

@router.post("/register", status_code=201, response_model=UserResponseDTO)
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


@router.post("/refresh", response_model=TokenResponseDTO)
def refresh_session(
    data: RefreshTokenRequestDTO,
    controller = Depends(get_refresh_controller)
):
    return controller.run(data)


@router.post("/logout")
def logout_session(
    data: LogoutRequestDTO,
    controller = Depends(get_logout_controller)
):
    return controller.run(data)