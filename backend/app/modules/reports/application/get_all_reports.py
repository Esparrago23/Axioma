from typing import List
from app.modules.reports.domain.repository import ReportRepository
from app.modules.reports.domain.entities import Report

class GetAllReportsUseCase:
    def __init__(self, repository: ReportRepository):
        self.repository = repository

    def execute(self, offset: int, limit: int) -> List[Report]:
        return self.repository.get_all(offset, limit)