from app.modules.reports.application.get_report_detail import GetReportDetailUseCase

class ReportDetailController:
    def __init__(self, use_case: GetReportDetailUseCase):
        self.use_case = use_case

    def run(self, report_id: int, user_id: int): # <-- Recibe el user_id
        return self.use_case.execute(id=report_id, current_user_id=user_id)