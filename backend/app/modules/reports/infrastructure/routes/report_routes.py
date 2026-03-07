from pathlib import Path
from uuid import uuid4
from fastapi import APIRouter, Depends, File, HTTPException, Query, Request, Response, UploadFile, status
from typing import List
import shutil

from app.modules.reports.infrastructure.dtos import CreateReportDTO, ReportPhotoUploadResponseDTO, ReportResponseDTO, UpdateReportDTO, VoteDTO

from app.modules.reports.infrastructure.dependencies import (
    get_create_controller,
    get_feed_controller,
    get_detail_controller,
    get_update_controller,
    get_delete_controller,
    get_vote_controller,
    get_all_controller
)


from app.modules.auth.infrastructure.dependencies import get_current_user

router = APIRouter(prefix="/reports", tags=["Reports"])
APP_DIR = Path(__file__).resolve().parents[4]
REPORT_PICTURES_DIR = APP_DIR / "static" / "report_pictures"

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
    controller = Depends(get_create_controller),
    user = Depends(get_current_user)
):
    return controller.run(dto=data, user_id=user.id)

@router.post("/photo", response_model=ReportPhotoUploadResponseDTO)
def upload_report_photo(
    request: Request,
    photo: UploadFile = File(...),
    user = Depends(get_current_user)
):
    if not photo.content_type or not photo.content_type.startswith("image/"):
        raise HTTPException(status_code=400, detail="El archivo debe ser una imagen")

    REPORT_PICTURES_DIR.mkdir(parents=True, exist_ok=True)
    extension = Path(photo.filename or "report.jpg").suffix or ".jpg"
    filename = f"{user.id}_{uuid4().hex}{extension}"
    file_path = REPORT_PICTURES_DIR / filename

    try:
        with file_path.open("wb") as buffer:
            shutil.copyfileobj(photo.file, buffer)
    except OSError:
        raise HTTPException(status_code=500, detail="No se pudo guardar la imagen")
    finally:
        photo.file.close()

    base_url = str(request.base_url).rstrip("/")
    public_url = f"{base_url}/static/report_pictures/{filename}"
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

@router.get("/{id}")
def get_report_detail(
    id: int,
    controller = Depends(get_detail_controller)
):
    return controller.run(report_id=id)

@router.patch("/{id}")
def update_report(
    id: int,
    data: UpdateReportDTO,
    controller = Depends(get_update_controller),
    user = Depends(get_current_user)
):
    return controller.run(report_id=id, user_id=user.id, dto=data)

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
    controller = Depends(get_vote_controller),
    user = Depends(get_current_user)
):
    return controller.run(report_id=id, user_id=user.id, dto=data)