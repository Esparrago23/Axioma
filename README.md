# Axioma

Plataforma de reporte ciudadano para Android. Los ciudadanos pueden reportar problemas urbanos y de seguridad, visualizarlos en un mapa interactivo, votar por su credibilidad y comentar en tiempo real.

## Capturas de pantalla

> *(Agregar capturas de pantalla aquí)*

## Requisitos previos

- Android Studio Hedgehog o superior
- JDK 21
- Cuenta de Firebase (para FCM)
- Token de acceso de Mapbox
- Backend de Axioma corriendo (ver `/backend`)

## Configuración

1. Clona el repositorio:
   ```bash
   git clone https://github.com/Esparrago23/Axioma.git
   cd Axioma/android
   ```

2. Crea el archivo `local.properties` en `/android` con las siguientes variables:
   ```properties
   MAPBOX_ACCESS_TOKEN=pk.tu_token_de_mapbox
   DEV_API_URL=http://tu-servidor:8000/
   ```

3. Coloca tu archivo `google-services.json` de Firebase en `android/app/`.

4. Compila y ejecuta desde Android Studio seleccionando el flavor `dev`.

## Arquitectura

El proyecto sigue **MVVM + Clean Architecture** organizado por features:

```
com.patatus.axioma/
├── core/           # Infraestructura transversal (DB, red, navegación, hardware)
├── features/
│   ├── auth/       # Login, registro, sesión biométrica
│   ├── reports/    # Feed, mapa, creación, detalle y mis reportes
│   ├── comments/   # Comentarios en reportes
│   ├── notifications/ # Centro de notificaciones (FCM + WebSocket)
│   └── users/      # Perfil de usuario
└── ui/theme/       # Design system (colores, tipografía, componentes)
```

Cada feature sigue la separación en capas:
- **data** — datasources remotos (Retrofit), locales (Room) y mediators (Paging 3)
- **domain** — entidades, repositorios (interfaces) y use cases
- **presentation** — ViewModels (StateFlow), Screens (Compose) y UiState (sealed class)

## Stack tecnológico

| Categoría | Tecnología |
|---|---|
| Lenguaje | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Arquitectura | MVVM + Clean Architecture |
| DI | Hilt |
| Base de datos local | Room 2.x + Paging 3 RemoteMediator |
| Red | Retrofit 2 + OkHttp |
| Tiempo real | OkHttp WebSocket |
| Push Notifications | Firebase Cloud Messaging (FCM) |
| Background processing | WorkManager |
| GPS | FusedLocationProviderClient |
| Cámara | ActivityResultContracts (CameraCapture) |
| Autenticación rápida | BiometricPrompt |
| Mapas | Mapbox Maps SDK 11.x |
| Imágenes | Coil |

## Funcionalidades principales

- **Autenticación** — Registro, login con JWT (refresh automático), inicio rápido con biométrico
- **Feed de reportes** — Lista paginada con caché offline (Room + RemoteMediator)
- **Mapa interactivo** — Reportes geolocalizados con Mapbox
- **Creación de reportes** — Captura de foto, ubicación GPS automática, categoría y descripción
- **Votación** — Sistema de credibilidad por votos (+1 / -1)
- **Comentarios** — Hilo de comentarios por reporte (CRUD)
- **Notificaciones** — Push via FCM + tiempo real via WebSocket con reconexión automática
- **Perfil** — Edición de datos, foto de perfil y eliminación de cuenta

## Permisos requeridos

- `ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION` — GPS para geolocalizar reportes
- `CAMERA` — Captura de foto al crear un reporte
- `USE_BIOMETRIC` — Inicio rápido de sesión
- `POST_NOTIFICATIONS` — Notificaciones push (Android 13+)
- `INTERNET` — Comunicación con la API

## Contribuciones

Las contribuciones son bienvenidas. Abre un issue o un pull request en [github.com/Esparrago23/Axioma](https://github.com/Esparrago23/Axioma).
