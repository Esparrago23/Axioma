from app.modules.reports.application.create_report import CreateReportUseCase
from app.modules.reports.infrastructure.dtos import CreateReportDTO

class CreateReportController:
    def __init__(self, use_case: CreateReportUseCase):
        self.use_case = use_case

    def run(self, dto: CreateReportDTO, user_id: int):
        return self.use_case.execute(
            title=dto.title,
            desc=dto.description,
            lat=dto.latitude,
            long=dto.longitude,
            cat=dto.category,
            user_id=user_id,
            photo_url=dto.photo_url
        )