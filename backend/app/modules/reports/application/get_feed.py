from typing import List
from app.modules.reports.domain.repository import ReportRepository
from app.modules.reports.domain.entities import Report

class GetFeedUseCase:
    def __init__(self, repository: ReportRepository):
        self.repository = repository

    def execute(
        self,
        lat: float,
        long: float,
        radius_km: float = 15.0,
        sort: str = "recent",
        offset: int = 0,
        limit: int = 50
    ) -> List[Report]:
        return self.repository.get_nearby(
            lat=lat,
            long=long,
            radius_km=radius_km,
            sort=sort,
            offset=offset,
            limit=limit
        )