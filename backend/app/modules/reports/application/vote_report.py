from fastapi import HTTPException
from app.modules.reports.domain.repository import ReportRepository
from app.modules.reports.domain.entities import Vote
class VoteReportUseCase:
    def __init__(self, repo: ReportRepository): self.repo = repo
    def execute(self, rid: int, uid: int, is_up: bool):
        report = self.repo.get_by_id(rid)
        if not report: raise HTTPException(404, "Reporte no encontrado")
        if self.repo.get_vote(uid, rid): raise HTTPException(400, "Ya votaste")
        
        val = 1 if is_up else -1
        report.calculate_reputation(val)
        self.repo.save(report)
        self.repo.save_vote(Vote(user_id=uid, report_id=rid, vote_value=val))
        return {
            "new_score": report.credibility_score, 
            "report_status": report.status 
        }