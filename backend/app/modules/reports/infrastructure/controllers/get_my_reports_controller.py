from typing import Optional

class GetMyReportsController:
    def __init__(self, use_case):
        self.use_case = use_case

    def run(self, user_id: int, search: Optional[str] = None):
        return self.use_case.execute(user_id=user_id, search=search)