import asyncio
from uuid import uuid4
from fastapi import APIRouter, BackgroundTasks, Depends, File, HTTPException, Query, Response, UploadFile, WebSocket, WebSocketDisconnect, status
from typing import List
from starlette.websockets import WebSocketState

from app.modules.reports.infrastructure.dtos import CreateReportDTO, ReportPhotoUploadResponseDTO, ReportResponseDTO, UpdateReportDTO, VoteDTO
from app.core.storage.dependencies import get_storage_repository
from app.core.storage.repository import StorageRepository

from app.modules.reports.infrastructure.dependencies import (
    get_create_controller,
    get_feed_controller,
    get_detail_controller,
    get_my_reports_controller,
    get_update_controller,
    get_delete_controller,
    get_vote_controller,
    get_all_controller,
    get_reports_repo,
)


from app.modules.auth.infrastructure.dependencies import get_auth_repo, get_current_user, get_user_from_access_token
from app.modules.auth.infrastructure.persistence.sql_repository import SQLUserRepository
from app.modules.reports.infrastructure.realtime import ReportRealtimeEvent, reports_realtime_broker


router = APIRouter(prefix="/reports", tags=["Reports"])


def _build_report_payload(report) -> dict:
    if hasattr(report, "model_dump"):
        return report.model_dump(mode="json")
    return ReportResponseDTO.model_validate(report).model_dump(mode="json")


def _extract_websocket_token(websocket: WebSocket) -> str | None:
    authorization = websocket.headers.get("authorization")
    if authorization:
        scheme, _, credentials = authorization.partition(" ")
        if scheme.lower() == "bearer" and credentials:
            return credentials

    return websocket.query_params.get("token")

@router.get("/all", response_model=List[ReportResponseDTO])
def get_all_reports(
    offset: int = 0,
    limit: int = Query(default=50, le=100),
    controller = Depends(get_all_controller)
):
    return controller.run(offset=offset, limit=limit)

@router.post("/", status_code=status.HTTP_201_CREATED)
def create_report(
    data: CreateReportDTO,
    background_tasks: BackgroundTasks,
    controller = Depends(get_create_controller),
    user = Depends(get_current_user)
):
    report = controller.run(dto=data, user_id=user.id)
    background_tasks.add_task(
        reports_realtime_broker.publish,
        ReportRealtimeEvent.NEW_REPORT,
        {"report": _build_report_payload(report)}
    )
    return report

@router.post("/photo", response_model=ReportPhotoUploadResponseDTO)
def upload_report_photo(
    photo: UploadFile = File(...),
    storage_repo: StorageRepository = Depends(get_storage_repository),
    user = Depends(get_current_user)
):
    if not photo.content_type or not photo.content_type.startswith("image/"):
        raise HTTPException(status_code=400, detail="El archivo debe ser una imagen")

    filename = photo.filename or "report.jpg"
    extension = filename.rsplit(".", 1)[-1].lower() if "." in filename else "jpg"
    object_name = f"report_pictures/{user.id}_{uuid4().hex}.{extension}"

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

    return ReportPhotoUploadResponseDTO(photo_url=public_url)

@router.get("/")
def get_feed(
    lat: float,
    long: float,
    radius_km: float = Query(default=15.0, ge=1.0, le=50.0),
    sort: str = Query(default="recent", pattern="^(recent|relevant)$"),
    offset: int = Query(default=0, ge=0),
    limit: int = Query(default=50, ge=1, le=100),
    controller = Depends(get_feed_controller)
):
    return controller.run(
        lat=lat,
        long=long,
        radius_km=radius_km,
        sort=sort,
        offset=offset,
        limit=limit
    )


@router.get("/me/created", response_model=List[ReportResponseDTO])
def get_my_reports(
    search: str = Query(None, description="Buscar por título"),
    controller = Depends(get_my_reports_controller),
    user = Depends(get_current_user)
):
    return controller.run(user_id=user.id, search=search)


@router.get("/ws", include_in_schema=False)
def reports_ws_http_hint():
    raise HTTPException(
        status_code=426,
        detail="Este endpoint requiere WebSocket (ws:// o wss://), no HTTP GET normal",
    )


@router.get("/{id:int}")
def get_report_detail(
    id: int,
    controller = Depends(get_detail_controller),
    user = Depends(get_current_user) 
):
    return controller.run(report_id=id, user_id=user.id)

@router.delete("/{id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_report(
    id: int,
    controller = Depends(get_delete_controller),
    user = Depends(get_current_user)
):
    controller.run(report_id=id, user_id=user.id)
    return Response(status_code=status.HTTP_204_NO_CONTENT)

@router.post("/{id}/vote")
def vote_report(
    id: int,
    data: VoteDTO,
    background_tasks: BackgroundTasks,
    controller = Depends(get_vote_controller),
    reports_repo = Depends(get_reports_repo),
    user = Depends(get_current_user)
):
    result = controller.run(report_id=id, user_id=user.id, dto=data)
    report = reports_repo.get_by_id(id)
    if report is not None:
        background_tasks.add_task(
            reports_realtime_broker.publish,
            ReportRealtimeEvent.VOTE_UPDATE,
            {
                "report_id": report.id,
                "credibility_score": report.credibility_score,
                "status": report.status.value,
            }
        )
    return result

@router.patch("/{id}") 
def update_report(
    id: int,
    data: UpdateReportDTO,
    controller = Depends(get_update_controller),
    user = Depends(get_current_user)
):
    print(f"Editando reporte {id} por usuario {user.id}")
    return controller.run(report_id=id, user_id=user.id, dto=data)

@router.websocket("/ws")
async def reports_websocket(
    websocket: WebSocket,
    auth_repo: SQLUserRepository = Depends(get_auth_repo)
):
    token = _extract_websocket_token(websocket)
    if not token:
        await websocket.close(code=status.WS_1008_POLICY_VIOLATION, reason="Token requerido")
        return

    try:
        user = get_user_from_access_token(token, auth_repo)
    except HTTPException:
        await websocket.close(code=status.WS_1008_POLICY_VIOLATION, reason="Token invalido")
        return

    await websocket.accept()
    await websocket.send_json({"event": "CONNECTED", "payload": {"user_id": user.id}})

    queue = await reports_realtime_broker.subscribe()

    async def forward_events() -> None:
        while True:
            message = await queue.get()
            if websocket.application_state != WebSocketState.CONNECTED:
                break
            await websocket.send_text(message)

    forward_task = asyncio.create_task(forward_events())

    try:
        while True:
            await websocket.receive_text()
    except WebSocketDisconnect:
        pass
    finally:
        forward_task.cancel()
        await reports_realtime_broker.unsubscribe(queue)
        if websocket.application_state == WebSocketState.CONNECTED:
            await websocket.close()