from abc import ABC, abstractmethod
from typing import List, Optional
from app.modules.comments.domain.entities import Comment


class CommentRepository(ABC):
    @abstractmethod
    def save(self, comment: Comment) -> Comment: pass

    @abstractmethod
    def get_by_report(self, report_id: int) -> List[Comment]: pass

    @abstractmethod
    def get_by_id(self, comment_id: int) -> Optional[Comment]: pass

    @abstractmethod
    def delete(self, comment_id: int) -> bool: pass
