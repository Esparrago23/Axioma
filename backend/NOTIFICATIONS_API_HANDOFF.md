# Backend Handoff: Notificaciones + FCM

## Variables de entorno

El backend ya soporta dos formas de inicializar Firebase Admin SDK en [backend/app/core/firebase_client.py](backend/app/core/firebase_client.py):

1. `FIREBASE_CREDENTIALS_JSON`
   Usa el contenido completo del service account en una sola linea JSON.

2. `FIREBASE_CREDENTIALS_FILE`
   Usa una ruta a un archivo JSON accesible por el proceso de la API.

### Configuracion recomendada para este repo

Ya deje preparada la opcion por archivo para Docker:

1. Descarga el service account desde Firebase Console.
2. Guarda el archivo como `backend/secrets/firebase-service-account.json`.
3. El contenedor `api` monta `backend/secrets` en `/run/secrets`.
4. En [backend/.env](backend/.env) ya quedo apuntado:

```env
FIREBASE_CREDENTIALS_FILE=/run/secrets/firebase-service-account.json
```

Con eso no necesitas meter el JSON entero dentro del `.env`.

### Donde sacar el archivo correcto en Firebase

No uses `google-services.json` para el backend. Ese archivo es solo para Android.

Para el backend necesitas un **service account**:

1. Abre Firebase Console.
2. Entra al proyecto `Axioma`.
3. Ve a `Project settings`.
4. Abre la pestaña `Service accounts`.
5. Pulsa `Generate new private key`.
6. Descarga el JSON y renombralo a `firebase-service-account.json`.
7. Guardalo en `backend/secrets/firebase-service-account.json`.

Despues reinicia la API con Docker Compose para que lea el archivo montado.

Tambien se usa:

- `NOTIFICATIONS_RADIUS_KM`: radio de cercania para alertas push. Default actual: `5`.

Si ninguna credencial esta configurada, el backend sigue funcionando en modo degradado:

- Guarda historial en base de datos.
- Resuelve usuarios cercanos.
- No envia push real a Firebase.

## Contrato para Android

Endpoint listo para registrar token y ubicacion:

- `PATCH /users/me/fcm-token`
- Requiere bearer token.
- Responde `204 No Content`.

Payload recomendado al login o cuando Firebase refresque token:

```json
{
  "fcm_token": "DEVICE_FCM_TOKEN",
  "last_latitude": -34.6037,
  "last_longitude": -58.3816
}
```

Payload recomendado cuando solo cambie la ubicacion del dispositivo:

```json
{
  "last_latitude": -34.6040,
  "last_longitude": -58.3821
}
```

Payload para desregistrar push en logout o al invalidar permisos:

```json
{
  "fcm_token": null
}
```

Reglas del endpoint:

- Si llega solo ubicacion, conserva el `fcm_token` actual.
- Si llega `fcm_token: null`, limpia token y ultima ubicacion.
- Si llega ubicacion sin token previamente registrado, responde `400`.
- `last_latitude` y `last_longitude` siempre deben viajar juntas.

## Endpoints listos para la app

- `GET /notifications/?offset=0&limit=50`
- `PATCH /notifications/{id}/read`

Respuesta de `GET /notifications/`:

```json
[
  {
    "id": 12,
    "user_id": 7,
    "title": "Nuevo reporte cerca de ti",
    "message": "Bache grande en Av. Siempre Viva",
    "type": "NEW_NEARBY_REPORT",
    "is_read": false,
    "report_id": 44,
    "created_at": "2026-03-12T17:42:00.000000"
  }
]
```

## Flujo backend ya implementado

1. Android registra `fcm_token` y ultima ubicacion.
2. Cuando se crea un reporte, el backend busca usuarios con token y ubicacion dentro de `NOTIFICATIONS_RADIUS_KM`.
3. El backend guarda una fila por destinatario en la tabla `notifications`.
4. Si Firebase esta configurado, envia el push multicast.

## Checklist de validacion Firebase

### 1. Verificar configuracion del backend

Confirma que exista este archivo real:

- `backend/secrets/firebase-service-account.json`

Y que [backend/.env](backend/.env) tenga:

```env
FIREBASE_CREDENTIALS_FILE=/run/secrets/firebase-service-account.json
```

### 2. Reiniciar la API

Desde la raiz del repo:

```powershell
docker compose up -d --build api
```

### 3. Revisar logs de arranque

```powershell
docker compose logs api --tail=100
```

Resultado esperado:

- No debe aparecer `Firebase deshabilitado`.
- La API debe arrancar normal.

### 4. Probar envio manual a un token real

De momento necesitas un `FCM token` valido de algun dispositivo de prueba. Sin ese token no puedes validar el envio real end-to-end.

Ya deje un script de prueba en [backend/scripts/send_test_push.py](backend/scripts/send_test_push.py).

Opcion Docker:

```powershell
docker compose exec api env PYTHONPATH=. python scripts/send_test_push.py --token "TU_FCM_TOKEN" --title "Prueba Axioma" --body "Push enviada desde backend"
```

Opcion local con la venv:

```powershell
C:/Users/minis/OneDrive/Escritorio/Axioma/Axioma/.venv/Scripts/python.exe backend/scripts/send_test_push.py --token "TU_FCM_TOKEN" --credentials-file "c:/Users/minis/OneDrive/Escritorio/Axioma/Axioma/backend/secrets/firebase-service-account.json"
```

Resultado esperado:

```json
{
  "success": 1,
  "failure": 0
}
```

### 5. Probar el flujo de negocio completo cuando ya haya token registrado

1. Registrar token y ubicacion en `PATCH /users/me/fcm-token`.
2. Crear un reporte nuevo cerca de esa ubicacion con `POST /reports/`.
3. Verificar que el usuario receptor vea una fila en `GET /notifications/`.
4. Verificar que el dispositivo reciba la push.
5. Marcarla como leida con `PATCH /notifications/{id}/read`.

### 6. Si falla

Casos comunes:

- `Firebase deshabilitado`: falta el archivo o la ruta de `FIREBASE_CREDENTIALS_FILE` no coincide.
- `success: 0`: el token es invalido, expiro o pertenece a otra configuracion Firebase.
- La fila aparece en `GET /notifications/` pero no llega push: el backend guardo historial, pero Firebase rechazo el token o el dispositivo no tiene FCM operativo.

## Nota para Docker

Si se usa `FIREBASE_CREDENTIALS_FILE` dentro de contenedores, la ruta debe existir dentro del contenedor `api`.
En este repo ya quedo resuelto montando `./backend/secrets` como `/run/secrets`.