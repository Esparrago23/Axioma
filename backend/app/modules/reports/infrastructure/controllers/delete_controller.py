from app.modules.reports.application.delete_report import DeleteReportUseCase

class DeleteReportController:
    def __init__(self, use_case: DeleteReportUseCase):
        self.use_case = use_case

    def run(self, report_id: int, user_id: int):
        return self.use_case.execute(id=report_id, uid=user_id)