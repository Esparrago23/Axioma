from typing import List, Optional
from app.modules.reports.domain.repository import ReportRepository
from app.modules.reports.domain.entities import Report

class GetMyReportsUseCase:
    def __init__(self, repo: ReportRepository):
        self.repo = repo

    def execute(self, user_id: int, search: Optional[str] = None) -> List[Report]:
        # Asegúrate de que tu interfaz ReportRepository tenga este método definido
        return self.repo.get_by_user(user_id=user_id, search=search)