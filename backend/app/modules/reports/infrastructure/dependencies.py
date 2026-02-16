from fastapi import Depends
from sqlmodel import Session
from app.core.database import get_session
from app.modules.reports.infrastructure.persistence.sql_repository import SQLReportRepository

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

def get_reports_repo(session: Session = Depends(get_session)) -> SQLReportRepository:
    return SQLReportRepository(session)

def get_create_uc(repo=Depends(get_reports_repo)): return CreateReportUseCase(repo)
def get_feed_uc(repo=Depends(get_reports_repo)): return GetFeedUseCase(repo)
def get_detail_uc(repo=Depends(get_reports_repo)): return GetReportDetailUseCase(repo)
def get_update_uc(repo=Depends(get_reports_repo)): return UpdateReportUseCase(repo)
def get_delete_uc(repo=Depends(get_reports_repo)): return DeleteReportUseCase(repo)
def get_vote_uc(repo=Depends(get_reports_repo)): return VoteReportUseCase(repo)

def get_create_controller(uc=Depends(get_create_uc)): return CreateReportController(uc)
def get_feed_controller(uc=Depends(get_feed_uc)): return FeedController(uc)
def get_detail_controller(uc=Depends(get_detail_uc)): return ReportDetailController(uc)
def get_update_controller(uc=Depends(get_update_uc)): return UpdateReportController(uc)
def get_delete_controller(uc=Depends(get_delete_uc)): return DeleteReportController(uc)
def get_vote_controller(uc=Depends(get_vote_uc)): return VoteReportController(uc)