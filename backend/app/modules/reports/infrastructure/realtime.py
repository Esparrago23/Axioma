import asyncio
import json
from enum import Enum
from typing import Any

from fastapi.encoders import jsonable_encoder


class ReportRealtimeEvent(str, Enum):
    NEW_REPORT = "NEW_REPORT"
    VOTE_UPDATE = "VOTE_UPDATE"


class ReportsRealtimeBroker:
    def __init__(self) -> None:
        self._subscribers: set[asyncio.Queue[str]] = set()
        self._lock = asyncio.Lock()

    async def subscribe(self) -> asyncio.Queue[str]:
        queue: asyncio.Queue[str] = asyncio.Queue(maxsize=100)
        async with self._lock:
            self._subscribers.add(queue)
        return queue

    async def unsubscribe(self, queue: asyncio.Queue[str]) -> None:
        async with self._lock:
            self._subscribers.discard(queue)

    async def publish(self, event: ReportRealtimeEvent, payload: dict[str, Any]) -> None:
        message = json.dumps(
            jsonable_encoder({"event": event.value, "payload": payload})
        )
        async with self._lock:
            subscribers = tuple(self._subscribers)

        for queue in subscribers:
            if queue.full():
                try:
                    queue.get_nowait()
                except asyncio.QueueEmpty:
                    pass

            try:
                queue.put_nowait(message)
            except asyncio.QueueFull:
                continue


reports_realtime_broker = ReportsRealtimeBroker()