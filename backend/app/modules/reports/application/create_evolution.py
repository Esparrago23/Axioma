from math import radians, cos, sin, asin, sqrt
from fastapi import HTTPException, status
from app.modules.reports.domain.entities import ReportEvolution, EvolutionType
from app.modules.reports.domain.repository import ReportRepository
from app.modules.reports.infrastructure.dtos import CreateEvolutionDTO

PROXIMITY_RADIUS_KM = 1.0


def _haversine_km(lat1: float, lon1: float, lat2: float, lon2: float) -> float:
    R = 6371.0
    dlat = radians(lat2 - lat1)
    dlon = radians(lon2 - lon1)
    a = sin(dlat / 2) ** 2 + cos(radians(lat1)) * cos(radians(lat2)) * sin(dlon / 2) ** 2
    return R * 2 * asin(sqrt(a))


class CreateEvolutionUseCase:
    def __init__(self, repo: ReportRepository):
        self.repo = repo

    def execute(self, report_id: int, user_id: int, dto: CreateEvolutionDTO) -> ReportEvolution:
        report = self.repo.get_by_id(report_id)
        if report is None:
            raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Reporte no encontrado")

        distance = _haversine_km(
            dto.user_latitude, dto.user_longitude,
            report.latitude, report.longitude,
        )
        if distance > PROXIMITY_RADIUS_KM:
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail="Debes estar a menos de 1 km del reporte para crear una actualización",
            )

        try:
            evo_type = EvolutionType(dto.type)
        except ValueError:
            raise HTTPException(status_code=status.HTTP_422_UNPROCESSABLE_ENTITY, detail="Tipo de evolución inválido")

        evolution = ReportEvolution(
            report_id=report_id,
            user_id=user_id,
            type=evo_type,
            description=dto.description,
            photo_url=dto.photo_url,
            user_latitude=dto.user_latitude,
            user_longitude=dto.user_longitude,
        )
        return self.repo.save_evolution(evolution)
