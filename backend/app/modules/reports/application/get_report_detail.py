from fastapi import HTTPException
from app.modules.reports.domain.repository import ReportRepository
class GetReportDetailUseCase:
    def __init__(self, repo: ReportRepository): self.repo = repo
    def execute(self, id: int):
        report = self.repo.get_by_id(id)
        if not report: raise HTTPException(status_code=404, detail="Reporte no encontrado")
        return report