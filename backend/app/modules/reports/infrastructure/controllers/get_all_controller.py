from app.modules.reports.application.get_all_reports import GetAllReportsUseCase

class GetAllReportsController:
    def __init__(self, use_case: GetAllReportsUseCase):
        self.use_case = use_case

    def run(self, offset: int, limit: int):
        return self.use_case.execute(offset, limit)