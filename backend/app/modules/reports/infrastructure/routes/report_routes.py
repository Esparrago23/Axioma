from fastapi import APIRouter, Depends, Response, status
from typing import List

from app.modules.reports.infrastructure.dtos import CreateReportDTO, UpdateReportDTO, VoteDTO

from app.modules.reports.infrastructure.dependencies import (
    get_create_controller,
    get_feed_controller,
    get_detail_controller,
    get_update_controller,
    get_delete_controller,
    get_vote_controller
)


from app.modules.auth.infrastructure.dependencies import get_current_user

router = APIRouter(prefix="/reports", tags=["Reports"])

@router.post("/", status_code=status.HTTP_201_CREATED)
def create_report(
    data: CreateReportDTO,
    controller = Depends(get_create_controller),
    user = Depends(get_current_user)
):
    return controller.run(dto=data, user_id=user.id)

@router.get("/")
def get_feed(
    lat: float, 
    long: float,
    controller = Depends(get_feed_controller)
):
    return controller.run(lat=lat, long=long)

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