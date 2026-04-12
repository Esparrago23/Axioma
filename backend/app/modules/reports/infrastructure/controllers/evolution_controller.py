from typing import List, Optional
from app.modules.reports.application.get_evolutions import GetEvolutionsUseCase
from app.modules.reports.application.create_evolution import CreateEvolutionUseCase
from app.modules.reports.application.vote_evolution import VoteEvolutionUseCase
from app.modules.reports.infrastructure.dtos import (
    CreateEvolutionDTO, EvolutionVoteDTO, EvolutionResponseDTO,
)
from app.modules.reports.domain.entities import ReportEvolution


def _to_dto(evo: ReportEvolution) -> EvolutionResponseDTO:
    return EvolutionResponseDTO(
        id=evo.id,
        report_id=evo.report_id,
        user_id=evo.user_id,
        type=evo.type.value,
        description=evo.description,
        photo_url=evo.photo_url,
        credibility_score=evo.credibility_score,
        status=evo.status.value,
        is_valid=evo.is_valid,
        user_vote=evo.user_vote,
        user_latitude=evo.user_latitude,
        user_longitude=evo.user_longitude,
        created_at=evo.created_at,
    )


class GetEvolutionsController:
    def __init__(self, uc: GetEvolutionsUseCase):
        self.uc = uc

    def run(self, report_id: int, current_user_id: Optional[int] = None) -> List[EvolutionResponseDTO]:
        return [_to_dto(e) for e in self.uc.execute(report_id, current_user_id)]


class CreateEvolutionController:
    def __init__(self, uc: CreateEvolutionUseCase):
        self.uc = uc

    def run(self, report_id: int, user_id: int, dto: CreateEvolutionDTO) -> EvolutionResponseDTO:
        return _to_dto(self.uc.execute(report_id, user_id, dto))


class VoteEvolutionController:
    def __init__(self, uc: VoteEvolutionUseCase):
        self.uc = uc

    def run(self, evolution_id: int, user_id: int, dto: EvolutionVoteDTO) -> EvolutionResponseDTO:
        return self.uc.execute(evolution_id, user_id, dto)
