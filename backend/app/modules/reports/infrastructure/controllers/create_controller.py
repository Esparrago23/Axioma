from app.modules.reports.application.create_report import CreateReportUseCase
from app.modules.reports.infrastructure.dtos import CreateReportDTO

class CreateReportController:
    def __init__(self, use_case: CreateReportUseCase):
        self.use_case = use_case

    def run(self, dto: CreateReportDTO, user_id: int):
        return self.use_case.execute(
            dto.title, dto.description, dto.latitude, dto.longitude, dto.category, user_id
        )
    

    