from app.modules.reports.application.get_feed import GetFeedUseCase

class FeedController:
    def __init__(self, use_case: GetFeedUseCase):
        self.use_case = use_case

    def run(
        self,
        lat: float,
        long: float,
        radius_km: float,
        sort: str,
        offset: int,
        limit: int
    ):
        return self.use_case.execute(
            lat=lat,
            long=long,
            radius_km=radius_km,
            sort=sort,
            offset=offset,
            limit=limit
        )