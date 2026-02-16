from app.modules.reports.application.vote_report import VoteReportUseCase
from app.modules.reports.infrastructure.dtos import VoteDTO

class VoteReportController:
    def __init__(self, use_case: VoteReportUseCase):
        self.use_case = use_case

    def run(self, report_id: int, user_id: int, dto: VoteDTO):
        is_upvote = dto.vote_value.upper() == "UP"
        return self.use_case.execute(rid=report_id, uid=user_id, is_up=is_upvote)