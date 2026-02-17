from app.modules.reports.domain.repository import ReportRepository
from app.modules.reports.domain.entities import Report, CategoryEnum

class CreateReportUseCase:
    def __init__(self, repository: ReportRepository):
        self.repository = repository

    def execute(self, title: str, desc: str, lat: float, long: float, cat: str, user_id: int, photo_url: str | None) -> Report:
        new_report = Report(
            title=title,
            description=desc,
            latitude=lat,
            longitude=long,
            category=CategoryEnum(cat),
            user_id=user_id,
            photo_url=photo_url
        )
        return self.repository.save(new_report)