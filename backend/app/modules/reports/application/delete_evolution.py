from fastapi import HTTPException, status
from app.modules.reports.domain.entities import EvolutionStatus
from app.modules.reports.domain.repository import ReportRepository


class DeleteEvolutionUseCase:
    def __init__(self, repo: ReportRepository):
        self.repo = repo

    def execute(self, evolution_id: int, user_id: int) -> None:
        evolution = self.repo.get_evolution_by_id(evolution_id)
        if evolution is None:
            raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Evolución no encontrada")
        if evolution.user_id != user_id:
            raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="No tienes permiso")
        if evolution.status != EvolutionStatus.PENDING:
            raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Solo se pueden eliminar evoluciones pendientes")
        self.repo.delete_evolution(evolution_id)
