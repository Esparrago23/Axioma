from fastapi import HTTPException, status
from app.modules.reports.domain.entities import EvolutionVote, EvolutionStatus, ReportStatus
from app.modules.reports.domain.repository import ReportRepository
from app.modules.reports.infrastructure.dtos import EvolutionVoteDTO, EvolutionResponseDTO
from app.modules.notifications.application.send_evolution_confirmed_notification import SendEvolutionConfirmedNotificationUseCase


class VoteEvolutionUseCase:
    def __init__(self, repo: ReportRepository, notify_uc: SendEvolutionConfirmedNotificationUseCase):
        self.repo = repo
        self.notify_uc = notify_uc

    def execute(self, evolution_id: int, user_id: int, dto: EvolutionVoteDTO) -> EvolutionResponseDTO:
        if dto.vote_value not in (1, -1):
            raise HTTPException(status_code=status.HTTP_422_UNPROCESSABLE_ENTITY, detail="vote_value debe ser 1 o -1")

        evolution = self.repo.get_evolution_by_id(evolution_id)
        if evolution is None or not evolution.is_valid:
            raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Evolución no encontrada")

        if evolution.user_id == user_id:
            raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="No puedes votar tu propia actualización")

        was_confirmed_before = evolution.status == EvolutionStatus.CONFIRMED

        existing = self.repo.get_evolution_vote(user_id, evolution_id)

        if existing and existing.vote_value == dto.vote_value:
            self.repo.delete_evolution_vote(user_id, evolution_id)
            evolution.apply_vote(-dto.vote_value)
        else:
            if existing:
                evolution.apply_vote(-existing.vote_value)
            evolution.apply_vote(dto.vote_value)
            self.repo.save_evolution_vote(EvolutionVote(
                user_id=user_id,
                evolution_id=evolution_id,
                vote_value=dto.vote_value,
            ))

        self.repo.save_evolution(evolution)

        if evolution.status == EvolutionStatus.CONFIRMED and not was_confirmed_before:
            if evolution.type.value == "RESOLVED":
                report = self.repo.get_by_id(evolution.report_id)
                if report and report.status == ReportStatus.ACTIVE:
                    report.status = ReportStatus.RESOLVED
                    self.repo.save(report)

            voter_ids = self.repo.get_report_voter_ids(evolution.report_id)
            report = self.repo.get_by_id(evolution.report_id)
            if report:
                self.notify_uc.execute(evolution=evolution, report=report, voter_ids=voter_ids)

        evolution.user_vote = dto.vote_value
        return EvolutionResponseDTO(
            id=evolution.id,
            report_id=evolution.report_id,
            user_id=evolution.user_id,
            type=evolution.type.value,
            description=evolution.description,
            photo_url=evolution.photo_url,
            credibility_score=evolution.credibility_score,
            status=evolution.status.value,
            is_valid=evolution.is_valid,
            user_vote=evolution.user_vote,
            user_latitude=evolution.user_latitude,
            user_longitude=evolution.user_longitude,
            created_at=evolution.created_at,
        )
