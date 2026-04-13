from typing import List, Optional
from app.modules.reports.domain.entities import ReportEvolution
from app.modules.reports.domain.repository import ReportRepository


class GetEvolutionsUseCase:
    def __init__(self, repo: ReportRepository):
        self.repo = repo

    def execute(self, report_id: int, current_user_id: Optional[int] = None) -> List[ReportEvolution]:
        return self.repo.get_evolutions(report_id, current_user_id)
