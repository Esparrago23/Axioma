from fastapi import HTTPException
from app.modules.reports.domain.repository import ReportRepository
from app.modules.reports.domain.entities import Vote

class VoteReportUseCase:
    def __init__(self, repo: ReportRepository): 
        self.repo = repo

    def execute(self, rid: int, uid: int, is_up: bool):
        report = self.repo.get_by_id(rid)
        if not report: 
            raise HTTPException(404, "Reporte no encontrado")
        
        existing_vote = self.repo.get_vote(uid, rid)
        new_val = 1 if is_up else -1
        
        # LÓGICA DE TOGGLE
        if existing_vote:
            if existing_vote.vote_value == new_val:
                # 1. ELIMINAR VOTO (Si pica el mismo botón que ya estaba activo)
                report.calculate_reputation(-new_val)
                self.repo.delete_vote(uid, rid) # Necesitas este método en tu repo
                final_v = 0
            else:
                # 2. CAMBIAR VOTO (De Real a Falso o viceversa)
                # Restamos el viejo (-1 o 1) y sumamos el nuevo
                diff = new_val - existing_vote.vote_value 
                report.calculate_reputation(diff)
                existing_vote.vote_value = new_val
                self.repo.save_vote(existing_vote)
                final_v = new_val
        else:
            # 3. VOTO NUEVO
            report.calculate_reputation(new_val)
            # Creamos el objeto Vote de SQLAlchemy
            new_vote = Vote(user_id=uid, report_id=rid, vote_value=new_val)
            self.repo.save_vote(new_vote)
            final_v = new_val

        self.repo.save(report)
        return {
            "new_score": report.credibility_score, 
            "user_vote": final_v,
            "report_status": report.status 
        }