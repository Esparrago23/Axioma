from app.modules.reports.application.get_feed import GetFeedUseCase

class FeedController:
    def __init__(self, use_case: GetFeedUseCase):
        self.use_case = use_case

    def run(self, lat: float, long: float):
        return self.use_case.execute(lat=lat, long=long)