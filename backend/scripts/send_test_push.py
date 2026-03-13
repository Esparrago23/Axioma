import argparse
import json
import os
import sys

def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Envia una push notification de prueba usando el Firebase Admin SDK del backend.",
    )
    parser.add_argument("--token", required=True, help="FCM token destino")
    parser.add_argument("--title", default="Axioma test", help="Titulo de la notificacion")
    parser.add_argument("--body", default="Push de prueba desde el backend", help="Cuerpo de la notificacion")
    parser.add_argument(
        "--data",
        action="append",
        default=[],
        help="Par clave=valor para el payload data. Se puede repetir.",
    )
    parser.add_argument(
        "--credentials-file",
        help="Ruta al service account JSON. Sobrescribe FIREBASE_CREDENTIALS_FILE.",
    )
    parser.add_argument(
        "--credentials-json",
        help="Service account JSON en una sola linea. Sobrescribe FIREBASE_CREDENTIALS_JSON.",
    )
    return parser.parse_args()


def parse_data(entries: list[str]) -> dict[str, str]:
    payload: dict[str, str] = {}
    for entry in entries:
        if "=" not in entry:
            raise ValueError(f"Formato invalido para --data: {entry}. Usa clave=valor")
        key, value = entry.split("=", 1)
        payload[key] = value
    return payload


def main() -> int:
    args = parse_args()

    if args.credentials_file:
        os.environ["FIREBASE_CREDENTIALS_FILE"] = args.credentials_file
    if args.credentials_json:
        os.environ["FIREBASE_CREDENTIALS_JSON"] = args.credentials_json

    from app.core.firebase_client import FirebaseClient

    try:
        payload = parse_data(args.data)
    except ValueError as exc:
        print(str(exc), file=sys.stderr)
        return 2

    client = FirebaseClient()
    if not client.is_initialized:
        print(
            "Firebase no esta inicializado. Revisa FIREBASE_CREDENTIALS_FILE o FIREBASE_CREDENTIALS_JSON.",
            file=sys.stderr,
        )
        return 1

    result = client.send_push_notification(
        tokens=[args.token],
        title=args.title,
        body=args.body,
        data=payload,
    )
    print(json.dumps(result, indent=2))

    return 0 if result.get("success", 0) > 0 else 1


if __name__ == "__main__":
    raise SystemExit(main())