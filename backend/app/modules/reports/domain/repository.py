from abc import ABC, abstractmethod
from typing import List, Optional
from app.modules.reports.domain.entities import Report, Vote, ReportEvolution, EvolutionVote

class ReportRepository(ABC):
    @abstractmethod
    def save(self, report: Report) -> Report: pass

    @abstractmethod
    def get_nearby(self, lat: float, long: float, radius_km: float, sort: str, offset: int, limit: int) -> List[Report]: pass

    @abstractmethod
    def get_all(self, offset: int, limit: int) -> List[Report]: pass

    @abstractmethod
    def get_by_id(self, id: int, current_user_id: Optional[int] = None) -> Optional[Report]: pass

    @abstractmethod
    def delete(self, report_id: int) -> bool: pass

    @abstractmethod
    def save_vote(self, vote: Vote) -> Vote: pass

    @abstractmethod
    def get_vote(self, user_id: int, report_id: int) -> Optional[Vote]: pass

    @abstractmethod
    def delete_vote(self, user_id: int, report_id: int) -> bool: pass

    @abstractmethod
    def save_evolution(self, evolution: ReportEvolution) -> ReportEvolution: pass

    @abstractmethod
    def get_evolutions(self, report_id: int, current_user_id: Optional[int] = None) -> List[ReportEvolution]: pass

    @abstractmethod
    def get_evolution_by_id(self, evolution_id: int) -> Optional[ReportEvolution]: pass

    @abstractmethod
    def save_evolution_vote(self, vote: EvolutionVote) -> EvolutionVote: pass

    @abstractmethod
    def get_evolution_vote(self, user_id: int, evolution_id: int) -> Optional[EvolutionVote]: pass

    @abstractmethod
    def delete_evolution_vote(self, user_id: int, evolution_id: int) -> bool: pass

    @abstractmethod
    def get_report_voter_ids(self, report_id: int) -> List[int]: pass

    @abstractmethod
    def delete_evolution(self, evolution_id: int) -> bool: pass

    @abstractmethod
    def get_user_pending_evolution(self, report_id: int, user_id: int) -> Optional[ReportEvolution]: pass

    @abstractmethod
    def delete_other_pending_evolutions(self, report_id: int, exclude_evolution_id: int) -> None: pass