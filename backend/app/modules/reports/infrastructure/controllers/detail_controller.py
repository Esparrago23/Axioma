from app.modules.reports.application.get_report_detail import GetReportDetailUseCase

class ReportDetailController:
    def __init__(self, use_case: GetReportDetailUseCase):
        self.use_case = use_case

    def run(self, report_id: int):
        return self.use_case.execute(id=report_id)