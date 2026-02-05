from typing import List, Optional
from fastapi import APIRouter, HTTPException, Query, status
from sqlmodel import select
from app.models import User, Report, Vote, VoteType, ReportStatus
from app.schemas import (
    ReportCreate, ReportRead, 
    VoteCreate, VoteResponse, 
    LoginRequest, TokenResponse
)
from app.deps import (
    SessionDep, UserDep, 
    get_password_hash, verify_password, create_access_token
)

router = APIRouter()

# --- AUTH ---

@router.post("/auth/register", response_model=TokenResponse)
def register(user_data: LoginRequest, session: SessionDep):
    # 1. Validar si existe
    existing_user = session.exec(select(User).where(User.email == user_data.email)).first()
    if existing_user:
        raise HTTPException(status_code=400, detail="El correo ya está registrado")
    
    # 2. Crear usuario
    new_user = User(
        email=user_data.email, 
        username=user_data.email.split("@")[0], # Username temporal
        hashed_password=get_password_hash(user_data.password)
    )
    session.add(new_user)
    session.commit()
    session.refresh(new_user)
    
    # 3. Generar token
    access_token = create_access_token(data={"sub": new_user.email})
    return TokenResponse(
        access_token=access_token, 
        user_id=new_user.id, 
        username=new_user.username,
        reputation=new_user.reputation_score
    )

@router.post("/auth/login", response_model=TokenResponse)
def login(user_data: LoginRequest, session: SessionDep):
    user = session.exec(select(User).where(User.email == user_data.email)).first()
    if not user or not verify_password(user_data.password, user.hashed_password):
        raise HTTPException(status_code=400, detail="Credenciales incorrectas")
    
    access_token = create_access_token(data={"sub": user.email})
    return TokenResponse(
        access_token=access_token, 
        user_id=user.id, 
        username=user.username,
        reputation=user.reputation_score
    )

# --- REPORTS ---

@router.post("/reports", response_model=ReportRead)
def create_report(report_data: ReportCreate, session: SessionDep, current_user: UserDep):
    # Lógica de Negocio: Si tienes mala reputación, no puedes reportar.
    if current_user.reputation_score < -10:
        raise HTTPException(status_code=403, detail="Tu reputación es demasiado baja para reportar.")

    new_report = Report.model_validate(report_data)
    new_report.user_id = current_user.id
    session.add(new_report)
    session.commit()
    session.refresh(new_report)
    return new_report

@router.get("/reports", response_model=List[ReportRead])
def get_reports(
    lat: float, 
    long: float, 
    session: SessionDep,
    radius_meters: float = 2000 # 2km por defecto
):
    # Bounding Box Simple (Optimización para usar Índices)
    # 1 grado de latitud ~= 111km. 
    delta_deg = radius_meters / 111000.0
    
    statement = (
        select(Report)
        .where(Report.status == ReportStatus.ACTIVE)
        .where(Report.latitude >= lat - delta_deg)
        .where(Report.latitude <= lat + delta_deg)
        .where(Report.longitude >= long - delta_deg)
        .where(Report.longitude <= long + delta_deg)
    )
    results = session.exec(statement).all()
    return results

# --- VOTING (THE TRUTH ENGINE) ---

@router.post("/reports/{report_id}/vote", response_model=VoteResponse)
def vote_report(report_id: int, vote_in: VoteCreate, session: SessionDep, current_user: UserDep):
    report = session.get(Report, report_id)
    if not report:
        raise HTTPException(status_code=404, detail="Reporte no encontrado")

    # 1. Verificar si ya votó
    existing_vote = session.exec(
        select(Vote)
        .where(Vote.user_id == current_user.id)
        .where(Vote.report_id == report_id)
    ).first()

    if existing_vote:
        # Si ya votó lo mismo, error. Si cambió de opinión, actualizamos.
        if existing_vote.vote_value == vote_in.vote_value:
             raise HTTPException(status_code=400, detail="Ya votaste esto")
        else:
            # Revertir el voto anterior en el score
            report.credibility_score -= existing_vote.vote_value 
            existing_vote.vote_value = vote_in.vote_value
            session.add(existing_vote)
    else:
        # Nuevo voto
        new_vote = Vote(user_id=current_user.id, report_id=report_id, vote_value=vote_in.vote_value)
        session.add(new_vote)
    
    # 2. Actualizar Score del Reporte
    report.credibility_score += vote_in.vote_value
    
    # 3. Reglas de Negocio (Soft Delete y Reputación)
    if report.credibility_score < -5:
        report.status = ReportStatus.HIDDEN
        # Penalizar al autor del reporte falso
        author = session.get(User, report.user_id)
        if author:
            author.reputation_score -= 2
            session.add(author)
    
    session.add(report)
    session.commit()
    
    return VoteResponse(new_score=report.credibility_score, report_status=report.status)