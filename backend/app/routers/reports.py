from typing import List
from fastapi import APIRouter, HTTPException, Query, status
from sqlmodel import select
from app.models import Report, ReportStatus, Vote, User
from app.schemas import ReportCreate, ReportRead, ReportUpdate, VoteCreate, VoteResponse
from app.deps import SessionDep, UserDep

router = APIRouter()

@router.post("/", response_model=ReportRead)
def create_report(report_data: ReportCreate, session: SessionDep, current_user: UserDep):
    if current_user.reputation_score < -10:
        raise HTTPException(status_code=403, detail="Reputación insuficiente para reportar.")
    
    new_report = Report.model_validate(report_data)
    new_report.user_id = current_user.id
    session.add(new_report)
    session.commit()
    session.refresh(new_report)
    return new_report

@router.get("/", response_model=List[ReportRead])
def get_reports(
    session: SessionDep,
    lat: float, 
    long: float, 
    radius_meters: float = 2000
):
    delta_deg = radius_meters / 111000.0
    statement = (
        select(Report)
        .where(Report.status == ReportStatus.ACTIVE)
        .where(Report.latitude >= lat - delta_deg)
        .where(Report.latitude <= lat + delta_deg)
        .where(Report.longitude >= long - delta_deg)
        .where(Report.longitude <= long + delta_deg)
    )
    return session.exec(statement).all()

@router.get("/{report_id}", response_model=ReportRead)
def get_report_detail(report_id: int, session: SessionDep):
    report = session.get(Report, report_id)
    if not report:
        raise HTTPException(status_code=404, detail="Reporte no encontrado")
    return report

@router.delete("/{report_id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_report(report_id: int, session: SessionDep, current_user: UserDep):
    report = session.get(Report, report_id)
    if not report:
        raise HTTPException(status_code=404, detail="Reporte no encontrado")
    
    if report.user_id != current_user.id:
        raise HTTPException(status_code=403, detail="No tienes permiso para borrar este reporte")
    
    session.delete(report)
    session.commit()
    return None


@router.post("/{report_id}/vote", response_model=VoteResponse)
def vote_report(report_id: int, vote_in: VoteCreate, session: SessionDep, current_user: UserDep):
    report = session.get(Report, report_id)
    if not report:
        raise HTTPException(status_code=404, detail="Reporte no encontrado")

    existing_vote = session.exec(
        select(Vote)
        .where(Vote.user_id == current_user.id)
        .where(Vote.report_id == report_id)
    ).first()

    if existing_vote:
        if existing_vote.vote_value == vote_in.vote_value:
             raise HTTPException(status_code=400, detail="Ya votaste esto")
        else:
            report.credibility_score -= existing_vote.vote_value 
            existing_vote.vote_value = vote_in.vote_value
            session.add(existing_vote)
    else:
        new_vote = Vote(user_id=current_user.id, report_id=report_id, vote_value=vote_in.vote_value)
        session.add(new_vote)
    
    report.credibility_score += vote_in.vote_value
    
    if report.credibility_score < -5:
        report.status = ReportStatus.HIDDEN
    
    session.add(report)
    session.commit()
    
    return VoteResponse(new_score=report.credibility_score, report_status=report.status)