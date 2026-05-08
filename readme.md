# LevelUpLife (Android)

Android client (Jetpack Compose + Kotlin) for the `LevelUpLife-Backend` (.NET) API.

> Status: login flow complete, dashboard placeholder. Built with MVVM, manual DI, Retrofit/OkHttp, kotlinx-serialization, EncryptedSharedPreferences and DataStore.

---

## 1. Tech stack

| Area | Tool / Version |
|------|----------------|
| Language | Kotlin 2.0.21 |
| UI | Jetpack Compose (BOM 2024.12.01), Material 3 |
| Build | Android Gradle Plugin 8.7.2, Gradle 8.9 |
| JVM | Java 17 (sourceCompatibility / kotlinOptions) |
| Min SDK | 24 (Android 7.0) |
| Compile / Target SDK | 35 |
| Networking | Retrofit 2.11.0, OkHttp 4.12.0, kotlinx-serialization-json 1.7.3 |
| Persistence | EncryptedSharedPreferences (security-crypto 1.1.0-alpha06), DataStore Preferences 1.1.1 |
| Navigation | Navigation Compose 2.8.5 |
| Tests | JUnit 4, kotlinx-coroutines-test, Turbine, MockWebServer |

---

## 2. Login feature

- Email **or** username + password input with field-level validation (required, format, min/max length).
- Loading indicator inside the submit button; button is disabled while authenticating.
- Friendly error banner mapped to backend status codes (400 / 401 / 423 / 5xx) and network failures (timeout / no connectivity).
- JWT token stored encrypted via `EncryptedSharedPreferences`.
- Auto-login on app start when a token is present.
- Light (blue) / dark (purple) theme toggle persisted with DataStore.
- Accessibility: `semantics` labels, password show/hide toggle, IME actions, `focusRequester` on first field, `imePadding`.
- Unit tests for validators, error mapper and `LoginViewModel`.

### Backend contract (current implementation)

Endpoint: `POST /api/auth/login`

Request body:

```json
{ "userNameOrEmail": "F3N1X", "password": "********" }
```

Successful response (HTTP 200):

```json
{
  "success": true,
  "data": {
    "token": "<JWT>",
    "userName": "F3N1X",
    "level": 1,
    "className": "Warrior"
  }
}
```

The user `id` is read from the JWT `sub` claim (no extra request needed).

Status code mapping (in `AuthErrorMapper`):

| HTTP / Throwable | UI message (es-ES) |
|------------------|--------------------|
| 400 | "Solicitud inválida. Revisa los datos ingresados." |
| 401 | "Usuario/email o contraseña incorrectos" |
| 423 | "Tu cuenta está bloqueada. Intenta más tarde." |
| 5xx | "Error del servidor. Intenta más tarde." |
| `IOException` / timeout | "Sin conexión. Revisa tu red e intenta de nuevo." |
| Anything else | "Ocurrió un error inesperado." |

---

## 3. Project setup

### 3.1 Requirements

- **Android Studio** Ladybug (2024.2.x) or newer with AGP 8.7.x support.
- **JDK 17** (Android Studio's bundled JDK works; or `sudo apt install openjdk-17-jdk` on Linux).
- **Android SDK Platform 35** + Build-Tools 35, plus an emulator image (API 30+) or a physical device with USB debugging.
- The companion backend **`LevelUpLife-Backend`** running locally (.NET 8/9). Default ports observed during development: `5147` (HTTP) and `7139` (HTTPS).

### 3.2 Clone and open

```bash
git clone <repo-url> LevelUpLife-Frontend
cd LevelUpLife-Frontend
```

Open the folder in Android Studio. Wait for Gradle sync to finish (first sync may download ~400 MB of dependencies; allow network access and wait).

### 3.3 `local.properties`

This file is **not committed** (it's in `.gitignore`). Create it in the project root with at least:

```properties
# Android SDK (Android Studio fills this automatically)
sdk.dir=/home/<user>/Android/Sdk

# Backend host (consumed by BuildConfig.API_HOST / API_SCHEME)
api.host=localhost:5147
api.scheme=http
```

Both `api.host` and `api.scheme` are exposed as `BuildConfig.API_HOST` and `BuildConfig.API_SCHEME` and consumed by `HostProvider`.

> If `local.properties` is missing or these keys are absent, the build falls back to `localhost:5223` and `http`. Adjust to match the port your backend actually uses.

### 3.4 `gradle.properties` (project-wide build flags)

This file is **committed** and shared across the team — every contributor builds with the same Gradle/Kotlin/AGP behavior. It lives at the repo root and is loaded automatically by Gradle on every invocation. Do **not** delete it; if you do, the next sync will fail with `android.useAndroidX property is not enabled` or similar.

Current contents and rationale:

```properties
# JVM heap and encoding for the Gradle daemon.
# 2 GB is enough for this module size; bump to -Xmx4096m if you add lots of
# annotation processors (KSP, Hilt, Room, etc.) and start seeing OOMs.
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8

# Enforces the official Kotlin style guide (4-space indent, trailing commas, etc.).
# Android Studio's Kotlin formatter respects this value.
kotlin.code.style=official

# AndroidX is REQUIRED — every modern dependency in this project (Compose,
# Lifecycle, Navigation, DataStore, security-crypto, Material 3, etc.) is
# published under androidx.* coordinates. Without this flag the build fails
# at dependency resolution time with:
#   "Configuration ':app:debugRuntimeClasspath' contains AndroidX dependencies,
#    but the 'android.useAndroidX' property is not enabled."
android.useAndroidX=true

# Each module gets its own R class containing only its own resources.
# Faster incremental builds and prevents accidental cross-module resource
# leaks. Safe to keep on for app + library projects created with AGP 7.0+.
android.nonTransitiveRClass=true

# Reuse outputs of tasks across builds. Gives a noticeable speed-up on clean
# rebuilds and CI; cache lives under ~/.gradle/caches/build-cache-1.
org.gradle.caching=true

# Configuration cache deliberately OFF. AGP 8.7 + the Kotlin Compose plugin +
# some of the older AndroidX artifacts in the dep set are not yet 100%
# configuration-cache-safe and produce serialization errors on the first sync.
# Re-enable once you upgrade AGP to 8.9+ and verify with:
#     ./gradlew --configuration-cache assembleDebug
org.gradle.configuration-cache=false
```

Optional flags you may want to add later (left commented in the file):

| Flag | What it does | When to enable |
|------|--------------|----------------|
| `org.gradle.parallel=true` | Builds independent modules concurrently | When you split the app into multiple modules |
| `org.gradle.daemon=true` | Keeps the Gradle daemon alive between builds | Already on by default in Gradle 3.0+; set explicitly if your CI disables it |
| `android.enableJetifier=true` | Translates legacy `android.support.*` dependencies on the fly | Only if you must consume an old library that hasn't migrated to AndroidX (avoid — it slows builds) |
| `kotlin.incremental=true` | Incremental Kotlin compilation | On by default; disable only when debugging compiler issues |

> **Do not** put secrets or per-developer paths here (use `local.properties` instead — it's gitignored). And do **not** put `api.host` or `api.scheme` here either; those belong in `local.properties` so each contributor / CI runner can override them.

---

## 4. Running the app

The trick is that **"localhost" on the Android device is the device itself**, not your development PC. The two scenarios below cover the only two ways to talk to the backend.

### 4.1 Android Emulator (AVD)

The emulator's special loopback alias is `10.0.2.2`, which points to the host machine. The app does this rewrite **automatically**: if `api.host` starts with `localhost` *and* the device is detected as an emulator, `HostProvider` swaps it for `10.0.2.2` at runtime.

**Steps:**

1. Start the backend on the **host machine**, listening only on loopback is fine:

   ```bash
   # in LevelUpLife-Backend
   dotnet run --urls "http://localhost:5147"
   ```

2. Make sure `local.properties` points to the same port:

   ```properties
   api.host=localhost:5147
   api.scheme=http
   ```

3. Boot the emulator from Android Studio's Device Manager, then click **Run ▶️**.

4. Verify in Logcat (filter `okhttp.OkHttpClient`):

   ```
   --> POST http://10.0.2.2:5147/api/auth/login
   <-- 200 http://10.0.2.2:5147/api/auth/login
   ```

   Note that `localhost` was rewritten to `10.0.2.2` for you.

> **Tip:** if you run the AVD on a Mac/Windows host with WSL and the backend lives inside WSL, you may need to expose the Linux IP instead. In that case use the physical phone instructions (4.2) with the WSL IP.

### 4.2 Physical phone (USB or Wi-Fi)

The phone must reach your PC's backend over the LAN. Loopback won't work; you have to use the PC's LAN IP and have the backend bind to all interfaces.

**Steps:**

1. **Find your PC's LAN IP** (same Wi-Fi as the phone):

   ```bash
   # Linux
   hostname -I | awk '{print $1}'
   # or
   ip -4 addr show | grep -oP '(?<=inet\s)\d+(\.\d+){3}'

   # macOS
   ipconfig getifaddr en0   # Wi-Fi
   ipconfig getifaddr en1   # Ethernet

   # Windows (PowerShell)
   (Get-NetIPAddress -AddressFamily IPv4 ^
     | Where-Object { $_.InterfaceAlias -match 'Wi-Fi|Ethernet' -and $_.IPAddress -notmatch '^169\.' }
   ).IPAddress
   ```

   You should see something like `192.168.x.x` or `10.x.x.x`. Avoid `127.0.0.1` and `169.254.x.x`.

2. **Update `local.properties`** with that IP and the backend port:

   ```properties
   api.host=192.168.100.54:5147
   api.scheme=http
   ```

3. **Run the backend bound to all interfaces** (this is the part most people miss):

   ```bash
   # in LevelUpLife-Backend
   dotnet run --urls "http://0.0.0.0:5147"
   ```

   `0.0.0.0` makes Kestrel listen on every network interface, including the one the phone reaches via Wi-Fi.

4. **Allow the port through the host firewall.**

   - Linux (UFW):
     ```bash
     sudo ufw allow 5147/tcp
     ```
   - Linux (firewalld):
     ```bash
     sudo firewall-cmd --add-port=5147/tcp --permanent
     sudo firewall-cmd --reload
     ```
   - macOS: System Settings → Network → Firewall → allow `dotnet` for incoming connections.
   - Windows: Windows Defender Firewall → "Allow an app" → add `dotnet.exe` (Private network).

5. **Same Wi-Fi network.** Phone and PC must be on the same SSID and the network must allow client-to-client traffic (most home routers do; many corporate / guest networks isolate clients — if so, use a phone hotspot or USB tethering).

6. **Quick connectivity check** from the phone *before* even running the app: open Chrome on the phone and visit `http://192.168.100.54:5147/swagger` (or any GET endpoint your backend exposes). If that doesn't load, the app won't connect either — fix the network/firewall first.

7. **Connect the phone**:
   - **USB**: enable Developer Options + USB debugging, plug the cable, accept the debug prompt. The phone shows up as a deployment target in Android Studio.
   - **Wi-Fi (Android 11+)**: in Device Manager → "Pair using Wi-Fi"; follow the wizard.

8. **Run ▶️** and verify in Logcat:

   ```
   --> POST http://192.168.100.54:5147/api/auth/login
   <-- 200 http://192.168.100.54:5147/api/auth/login
   ```

### 4.3 Cleartext HTTP and `network_security_config`

Android 9+ blocks cleartext HTTP by default. The app whitelists development hosts via `app/src/main/res/xml/network_security_config.xml`:

```xml
<base-config cleartextTrafficPermitted="true">
    <trust-anchors><certificates src="system"/></trust-anchors>
</base-config>
<domain-config cleartextTrafficPermitted="true">
    <domain includeSubdomains="true">localhost</domain>
    <domain includeSubdomains="true">10.0.2.2</domain>
    <domain includeSubdomains="true">10.0.3.2</domain>
</domain-config>
```

The `<base-config>` allows cleartext globally, which is why arbitrary LAN IPs (e.g. `192.168.x.x`) work without an explicit entry. If you ever harden this for production, drop `<base-config cleartextTrafficPermitted="true">` and add the exact dev IPs as `<domain>` children, or build-flavor this file so production uses HTTPS only.

### 4.4 Runtime debug override

`DebugApiPreferences` lets you change `api.host` / `api.scheme` at runtime via DataStore (`debug_api_prefs`) without rebuilding. Useful for switching between emulator and phone setups in the same APK; expose a Settings screen if you want a UI for it. Production builds should ignore these values (gate by `BuildConfig.DEBUG`).

---

## 5. Useful commands

```bash
./gradlew assembleDebug          # build debug APK
./gradlew installDebug           # build and install on the connected device
./gradlew test                   # JVM unit tests (validators, mapper, ViewModel)
./gradlew connectedDebugAndroidTest   # instrumented tests (needs a device)
./gradlew clean                  # nuke build/ if you hit cache weirdness
./gradlew --refresh-dependencies # force re-download of Gradle deps
```

To run a single test class:

```bash
./gradlew test --tests com.example.leveluplife.domain.validation.ValidatorsTest
```

---

## 6. Project structure

```
com.example.leveluplife
├── data
│   ├── auth          (TokenStore, AuthRepository, AuthSession, JwtUtils)
│   ├── error         (AuthError + Mapper)
│   ├── network       (Retrofit, OkHttp, AuthApi, HostProvider, DTOs)
│   └── preferences   (Theme + DebugApi DataStore)
├── domain
│   └── validation    (pure Validators, JVM-testable)
├── ui
│   ├── auth          (LoginScreen, LoginViewModel, LoginUiState, mappers)
│   ├── components    (LulLogo, ThemeToggleButton)
│   ├── dashboard     (placeholder post-login screen)
│   ├── navigation    (AppNavigation NavHost)
│   └── theme         (LevelUpLifeTheme, color palettes, ThemeController)
├── AppContainer.kt   (manual DI graph)
├── LevelUpLifeApp.kt (Application class, lazy AppContainer)
└── MainActivity.kt   (entry, hosts the NavHost and theme)
```

---

## 7. Troubleshooting

| Symptom | Likely cause | Fix |
|---------|-------------|-----|
| `java.net.ConnectException: Failed to connect to localhost/127.0.0.1:<port>` on a physical phone | App is targeting the phone itself, not your PC | Set `api.host=<PC_LAN_IP>:<port>` in `local.properties`, run backend with `--urls "http://0.0.0.0:<port>"`. |
| Connection works on emulator but not on phone | Backend bound only to `localhost`, or firewall, or networks isolation | Bind to `0.0.0.0`, open the port in firewall, ensure same Wi-Fi without client isolation. |
| Backend returns HTTP 400 with `{"errors":{"UserNameOrEmail":...}}` | Frontend sending a different field name | This is already fixed: `LoginRequest` uses `@SerialName("userNameOrEmail")`. If you change DTOs, keep the name aligned. |
| App shows "Ocurrió un error inesperado" right after login but Logcat shows HTTP 200 | Response body shape doesn't match `LoginResponse` | Check `data/network/dto/LoginResponse.kt` matches what the backend actually returns. We currently expect `{ success, data: { token, userName, level, className } }`. |
| `Inconsistent JVM-target compatibility` build error | Java and Kotlin compile targets differ | Already pinned to JDK 17 in `app/build.gradle.kts`; if you changed it, restore `sourceCompatibility`, `targetCompatibility` and `kotlinOptions.jvmTarget` to `17`. |
| Gradle sync fails to download dependencies | Restricted network / proxy | Configure `gradle.properties` proxy keys or `--offline` after a successful first sync. |
| `android.useAndroidX property is not enabled` | `gradle.properties` was reset | Re-add `android.useAndroidX=true` and `android.nonTransitiveRClass=true`. |
| Login succeeds but token expires quickly | Backend doesn't issue a refresh token | Currently `refreshToken = null`; the user has to log in again after the JWT `exp`. Add a refresh endpoint server-side and reflect it in `LoginData` to enable silent renewal. |

If you hit something not covered here, capture the relevant Logcat lines (filter by `okhttp.OkHttpClient` and your package name) and the backend console output, then iterate.
