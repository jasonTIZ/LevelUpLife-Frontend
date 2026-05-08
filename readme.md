# LevelUpLife (Android)

App Android (Jetpack Compose + Kotlin) cliente del backend `LevelUpLife-Backend`.

## Pantalla de Login

- Validación de email y contraseña (campos requeridos, formato, longitud).
- Indicador de carga durante la autenticación.
- Mensajes amigables para 400 / 401 / 423 / 500 y errores de red.
- Almacenamiento seguro de tokens con `EncryptedSharedPreferences`.
- Toggle claro (azul) / oscuro (morado) persistido con DataStore.
- Tests unitarios para los validadores, el mapeador de errores y el `LoginViewModel`.

## Backend esperado

Endpoint: `POST /api/auth/login`

Petición:
```json
{ "email": "...", "password": "..." }
```

Respuesta esperada:
```json
{
  "accessToken": "...",
  "refreshToken": "...",
  "user": {
    "id": "...",
    "name": "...",
    "email": "...",
    "avatarUrl": "...",
    "roles": ["..."]
  }
}
```

Códigos mapeados a mensajes:
- 400 → "Solicitud inválida. Revisa los datos ingresados."
- 401 → "Email o contraseña incorrectos"
- 423 → "Tu cuenta está bloqueada. Intenta más tarde."
- 5xx → "Error del servidor. Intenta más tarde."
- IOException / timeout → "Sin conexión. Revisa tu red e intenta de nuevo."

## Configuración del host (emulador vs teléfono físico)

El host del backend se configura en `local.properties` (no se commitea):

```properties
api.host=localhost:5223
api.scheme=http
```

Estos valores se exponen como `BuildConfig.API_HOST` y `BuildConfig.API_SCHEME`.

### Emulador

No hace falta cambiar nada: si `api.host` empieza por `localhost`, la app
detecta automáticamente que está en un emulador AVD y reemplaza `localhost`
por `10.0.2.2` (IP del host del emulador).

### Teléfono físico

1. Averigua la IP del PC en tu LAN, p. ej. `192.168.1.42`.
2. Ajusta `local.properties`:
   ```properties
   api.host=192.168.1.42:5223
   api.scheme=http
   ```
3. Arranca el backend escuchando en todas las interfaces:
   ```bash
   dotnet run --urls "http://0.0.0.0:5223"
   ```
4. Asegúrate de que el firewall del PC permita el puerto y de que el
   teléfono y el PC estén en la misma red Wi-Fi.

`network_security_config.xml` permite tráfico HTTP en claro hacia
`localhost`, `10.0.2.2`, `10.0.3.2` y a nivel general en builds de
desarrollo. Si tu IP LAN es rechazada, agrega un `<domain>` adicional.

### Override en runtime (debug)

`DebugApiPreferences` permite sobreescribir `api.host` / `api.scheme` en
tiempo de ejecución (DataStore `debug_api_prefs`). Útil para alternar
entre emulador y teléfono sin recompilar; expón un screen de Settings si
lo necesitas.

## Comandos útiles

```bash
./gradlew assembleDebug
./gradlew test            # Tests unitarios JVM
./gradlew connectedCheck  # Tests instrumentados
```

## Estructura

```
com.example.leveluplife
├── data
│   ├── auth        (TokenStore, AuthRepository, AuthSession)
│   ├── error       (AuthError + Mapper)
│   ├── network     (Retrofit, OkHttp, AuthApi, HostProvider)
│   └── preferences (Theme + DebugApi DataStore)
├── domain
│   └── validation  (Validators puros, testeables)
├── ui
│   ├── auth        (LoginScreen, LoginViewModel)
│   ├── components  (LulLogo, ThemeToggleButton)
│   ├── dashboard   (placeholder post-login)
│   ├── navigation  (AppNavigation)
│   └── theme       (LevelUpLifeTheme + ThemeController)
├── AppContainer.kt
├── LevelUpLifeApp.kt
└── MainActivity.kt
```
