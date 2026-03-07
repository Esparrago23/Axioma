from datetime import datetime, timedelta
from math import cos, radians
from typing import List, Optional
from sqlmodel import Session, select
from sqlalchemy import and_, desc, func
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

    def get_nearby(
        self,
        lat: float,
        long: float,
        radius_km: float,
        sort: str,
        offset: int,
        limit: int
    ) -> List[Report]:
        # Cheap geospatial prefilter that lets DB indexes reduce candidate rows before exact distance.
        lat_delta = radius_km / 111.0
        safe_cos = max(abs(cos(radians(lat))), 1e-6)
        long_delta = min(180.0, radius_km / (111.0 * safe_cos))

        # Haversine distance in SQL (km) so filtering/pagination stays in DB.
        inner = (
            func.cos(func.radians(lat)) * func.cos(func.radians(ReportModel.latitude))
            * func.cos(func.radians(ReportModel.longitude) - func.radians(long))
            + func.sin(func.radians(lat)) * func.sin(func.radians(ReportModel.latitude))
        )
        clamped_inner = func.least(1.0, func.greatest(-1.0, inner))
        distance_km = 6371.0 * func.acos(clamped_inner)

        statement = select(ReportModel).where(
            and_(
                ReportModel.status == ReportStatus.ACTIVE.value,
                ReportModel.latitude >= lat - lat_delta,
                ReportModel.latitude <= lat + lat_delta,
                ReportModel.longitude >= long - long_delta,
                ReportModel.longitude <= long + long_delta,
                distance_km <= radius_km,
            )
        )

        if sort == "relevant":
            cutoff = datetime.utcnow() - timedelta(hours=48)
            statement = statement.where(ReportModel.created_at >= cutoff).order_by(
                desc(ReportModel.credibility_score),
                desc(ReportModel.created_at),
            )
        else:
            statement = statement.order_by(desc(ReportModel.created_at))

        statement = statement.offset(offset).limit(limit)
        results = self.session.exec(statement).all()
        return [self._to_domain(r) for r in results]
    
    def get_all(self, offset: int, limit: int) -> List[Report]:
        statement = (
            select(ReportModel)
            .where(ReportModel.status == ReportStatus.ACTIVE.value)
            .order_by(desc(ReportModel.created_at))
            .offset(offset)
            .limit(limit)
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
    
