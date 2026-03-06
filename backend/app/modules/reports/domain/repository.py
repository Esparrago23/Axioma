from abc import ABC, abstractmethod
from typing import List, Optional
from app.modules.reports.domain.entities import Report, Vote

class ReportRepository(ABC):
    @abstractmethod
    def save(self, report: Report) -> Report:
        pass

    @abstractmethod
    def get_by_id(self, id: int) -> Optional[Report]:
        pass

    @abstractmethod
    def get_nearby(self, lat: float, long: float, radius_km: float) -> List[Report]:
        pass
    @abstractmethod
    def get_all(self, offset: int, limit: int) -> List[Report]: 
        pass

    @abstractmethod
    def delete(self, report_id: int) -> bool:
        pass

    @abstractmethod
    def save_vote(self, vote: Vote) -> Vote:
        pass
    
    @abstractmethod
    def get_vote(self, user_id: int, report_id: int) -> Optional[Vote]:
        pass