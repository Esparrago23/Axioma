import json
import logging
from pathlib import Path

import firebase_admin
from firebase_admin import credentials, messaging

from app.core.config import settings

logger = logging.getLogger(__name__)


class FirebaseClient:
    def __init__(self) -> None:
        self._app = None
        self._is_initialized = False
        self._initialize()

    @property
    def is_initialized(self) -> bool:
        return self._is_initialized

    def _initialize(self) -> None:
        if firebase_admin._apps:
            self._app = firebase_admin.get_app()
            self._is_initialized = True
            return

        try:
            cred = self._build_credentials()
        except ValueError as exc:
            logger.warning("Firebase deshabilitado: %s", exc)
            return
        except Exception:
            logger.exception("No se pudieron cargar las credenciales de Firebase")
            return

        try:
            self._app = firebase_admin.initialize_app(cred)
            self._is_initialized = True
        except Exception:
            logger.exception("No se pudo inicializar Firebase Admin SDK")

    def _build_credentials(self) -> credentials.Base:
        if settings.FIREBASE_CREDENTIALS_JSON:
            return credentials.Certificate(json.loads(settings.FIREBASE_CREDENTIALS_JSON))

        if settings.FIREBASE_CREDENTIALS_FILE:
            cred_path = Path(settings.FIREBASE_CREDENTIALS_FILE)
            if not cred_path.is_file():
                raise ValueError("FIREBASE_CREDENTIALS_FILE no existe o no es un archivo")
            return credentials.Certificate(str(cred_path))

        raise ValueError("Faltan FIREBASE_CREDENTIALS_JSON o FIREBASE_CREDENTIALS_FILE")

    def send_push_notification(
        self,
        tokens: list[str],
        title: str,
        body: str,
        data: dict[str, str] | None = None,
    ) -> dict[str, int]:
        clean_tokens = [token for token in tokens if token]
        if not clean_tokens:
            return {"success": 0, "failure": 0}

        if not self._is_initialized:
            logger.warning("Push omitido: Firebase no esta inicializado")
            return {"success": 0, "failure": len(clean_tokens)}

        message = messaging.MulticastMessage(
            tokens=clean_tokens,
            notification=messaging.Notification(title=title, body=body),
            data=data or {},
        )

        try:
            response = messaging.send_each_for_multicast(message, app=self._app)
        except Exception:
            logger.exception("Error enviando push notification via Firebase")
            return {"success": 0, "failure": len(clean_tokens)}

        return {
            "success": response.success_count,
            "failure": response.failure_count,
        }


firebase_client = FirebaseClient()


def get_firebase_client() -> FirebaseClient:
    return firebase_client