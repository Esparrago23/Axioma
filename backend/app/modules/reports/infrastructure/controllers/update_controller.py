from app.modules.reports.application.update_report import UpdateReportUseCase
from app.modules.reports.infrastructure.dtos import UpdateReportDTO

class UpdateReportController:
    def __init__(self, use_case: UpdateReportUseCase):
        self.use_case = use_case

    def run(self, report_id: int, user_id: int, dto: UpdateReportDTO):
        return self.use_case.execute(
            id=report_id,
            uid=user_id,
            title=dto.title,
            desc=dto.description
        )