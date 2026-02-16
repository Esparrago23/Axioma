from typing import List
from app.modules.reports.domain.repository import ReportRepository
from app.modules.reports.domain.entities import Report

class GetFeedUseCase:
    def __init__(self, repository: ReportRepository):
        self.repository = repository

    def execute(self, lat: float, long: float) -> List[Report]:
        # Radio fijo de 10km por regla de negocio
        return self.repository.get_nearby(lat, long, radius_km=10.0)