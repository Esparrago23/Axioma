from fastapi import HTTPException
from app.modules.reports.domain.repository import ReportRepository

class GetReportDetailUseCase:
    def __init__(self, repo: ReportRepository): 
        self.repo = repo

    def execute(self, id: int, current_user_id: int): # <-- Recibimos el ID del usuario
        # Le pedimos al repo el reporte y le pasamos quién lo está consultando
        report = self.repo.get_by_id(id, current_user_id=current_user_id)
        
        if not report: 
            raise HTTPException(status_code=404, detail="Reporte no encontrado")
        
        return report