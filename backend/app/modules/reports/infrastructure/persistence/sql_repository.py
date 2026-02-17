from math import radians, cos, sin, asin, sqrt
from typing import List, Optional
from sqlmodel import Session, select
from app.modules.reports.domain.entities import Report, Vote, CategoryEnum, ReportStatus
from app.modules.reports.domain.repository import ReportRepository
from app.modules.reports.infrastructure.persistence.models import ReportModel, VoteModel

class SQLReportRepository(ReportRepository):
    def __init__(self, session: Session):
        self.session = session

    def save(self, report: Report) -> Report:
        data = report.model_dump()
        data["category"] = report.category.value
        data["status"] = report.status.value
        
        if report.id: # Update
            report_db = self.session.get(ReportModel, report.id)
            for key, value in data.items():
                setattr(report_db, key, value)
        else: # Insert
            report_db = ReportModel(**data)
            self.session.add(report_db)
            
        self.session.commit()
        self.session.refresh(report_db)
        return self._to_domain(report_db)

    def get_by_id(self, id: int) -> Optional[Report]:
        report_db = self.session.get(ReportModel, id)
        return self._to_domain(report_db) if report_db else None

    def get_nearby(self, lat: float, long: float, radius_km: float) -> List[Report]:
        statement = select(ReportModel).where(ReportModel.status == ReportStatus.ACTIVE.value)
        results = self.session.exec(statement).all()
        
        nearby = []
        for r in results:
            if self._haversine(lat, long, r.latitude, r.longitude) <= radius_km:
                nearby.append(self._to_domain(r))
        return nearby
    
    def get_all(self, offset: int, limit: int) -> List[Report]:
        statement = (
            select(ReportModel)
            .where(ReportModel.status == ReportStatus.ACTIVE.value)
            .offset(offset)
            .limit(limit)
            .order_by(ReportModel.created_at.desc())
        )
        results = self.session.exec(statement).all()
        return [self._to_domain(r) for r in results]

    def delete(self, report_id: int) -> bool:
        report_db = self.session.get(ReportModel, report_id)
        if report_db:
            self.session.delete(report_db)
            self.session.commit()
            return True
        return False

    def save_vote(self, vote: Vote) -> Vote:
        vote_db = VoteModel(**vote.model_dump())
        self.session.add(vote_db)
        self.session.commit()
        self.session.refresh(vote_db)
        return Vote(**vote_db.model_dump())

    def get_vote(self, user_id: int, report_id: int) -> Optional[Vote]:
        statement = select(VoteModel).where(VoteModel.user_id == user_id, VoteModel.report_id == report_id)
        result = self.session.exec(statement).first()
        return Vote(**result.model_dump()) if result else None

    def _to_domain(self, model: ReportModel) -> Report:
        return Report(**model.model_dump(exclude={"category", "status"}), category=CategoryEnum(model.category), status=ReportStatus(model.status))
    
    def _haversine(self, lat1, lon1, lat2, lon2):
        R = 6371  # km
        dlat, dlon = radians(lat2 - lat1), radians(lon2 - lon1)
        a = sin(dlat/2)**2 + cos(radians(lat1)) * cos(radians(lat2)) * sin(dlon/2)**2
        return 2 * R * asin(sqrt(a))