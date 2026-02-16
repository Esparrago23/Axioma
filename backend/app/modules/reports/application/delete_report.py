from fastapi import HTTPException
from app.modules.reports.domain.repository import ReportRepository
class DeleteReportUseCase:
    def __init__(self, repo: ReportRepository): self.repo = repo
    def execute(self, id: int, uid: int):
        report = self.repo.get_by_id(id)
        if not report: raise HTTPException(404, "No encontrado")
        if report.user_id != uid: raise HTTPException(403, "No eres el dueño")
        self.repo.delete(id)