from fastapi import Depends
from sqlmodel import Session
from app.core.database import get_session
from app.modules.reports.infrastructure.persistence.sql_repository import SQLReportRepository
from app.core.storage.dependencies import get_storage_repository
from app.modules.notifications.infrastructure.dependencies import get_send_push_uc

# --- Importamos Use Cases ---
from app.modules.reports.application.create_report import CreateReportUseCase
from app.modules.reports.application.get_feed import GetFeedUseCase
from app.modules.reports.application.get_report_detail import GetReportDetailUseCase
from app.modules.reports.application.update_report import UpdateReportUseCase
from app.modules.reports.application.delete_report import DeleteReportUseCase
from app.modules.reports.application.vote_report import VoteReportUseCase

from app.modules.reports.infrastructure.controllers.create_controller import CreateReportController
from app.modules.reports.infrastructure.controllers.feed_controller import FeedController
from app.modules.reports.infrastructure.controllers.detail_controller import ReportDetailController
from app.modules.reports.infrastructure.controllers.update_controller import UpdateReportController
from app.modules.reports.infrastructure.controllers.delete_controller import DeleteReportController
from app.modules.reports.infrastructure.controllers.vote_controller import VoteReportController
from app.modules.reports.application.get_all_reports import GetAllReportsUseCase
from app.modules.reports.infrastructure.controllers.get_all_controller import GetAllReportsController

from app.modules.reports.application.get_my_reports import GetMyReportsUseCase
from app.modules.reports.infrastructure.controllers.get_my_reports_controller import GetMyReportsController




def get_reports_repo(session: Session = Depends(get_session)) -> SQLReportRepository:
    return SQLReportRepository(session)

def get_create_uc(repo=Depends(get_reports_repo), send_push_uc=Depends(get_send_push_uc)): return CreateReportUseCase(repo, send_push_uc)
def get_feed_uc(repo=Depends(get_reports_repo)): return GetFeedUseCase(repo)
def get_detail_uc(repo=Depends(get_reports_repo)): return GetReportDetailUseCase(repo)
# (⭐ NOTA: Borré get_update_uc de aquí porque lo inyectas abajo manualmente con el storage)
def get_delete_uc(repo=Depends(get_reports_repo)): return DeleteReportUseCase(repo)
def get_vote_uc(repo=Depends(get_reports_repo)): return VoteReportUseCase(repo)
def get_all_reports_uc(repo=Depends(get_reports_repo)): return GetAllReportsUseCase(repo)

def get_create_controller(uc=Depends(get_create_uc)): return CreateReportController(uc)
def get_feed_controller(uc=Depends(get_feed_uc)): return FeedController(uc)
def get_detail_controller(uc=Depends(get_detail_uc)): return ReportDetailController(uc)
# (⭐ NOTA: Borré la versión corta de get_update_controller de aquí)
def get_delete_controller(uc=Depends(get_delete_uc)): return DeleteReportController(uc)
def get_vote_controller(uc=Depends(get_vote_uc)): return VoteReportController(uc)
def get_all_controller(uc=Depends(get_all_reports_uc)): return GetAllReportsController(uc)

# Este es el ÚNICO get_update_controller que debe existir
def get_update_controller(
    session = Depends(get_session),
    storage_repo = Depends(get_storage_repository)
):
    repo = SQLReportRepository(session)
    use_case = UpdateReportUseCase(repo, storage_repo)
    return UpdateReportController(use_case)

def get_my_reports_uc(repo=Depends(get_reports_repo)): return GetMyReportsUseCase(repo)
def get_my_reports_controller(uc=Depends(get_my_reports_uc)): return GetMyReportsController(uc)