# Contexto - Creación de Hábitos - LevelUpLife

## Proyecto
- **Frontend**: Android app Kotlin/Jetpack Compose con arquitectura MVVM
- **Backend**: .NET 9 con PostgreSQL
- **Ubicación**: `/home/AdamAG/Documents/Moviles/LevelUpLife-Frontend`

## Estado Actual
- Login funcional con JWT
- Dashboard placeholder implementado
- AuthRepository y HabitRepository implementados
- Theme toggle (light/dark) funcional
- **HABIT CREATION - COMPLETADO**:
  - Todos los DTOs creados (enums y data classes)
  - API interface con endpoint POST /api/habits
  - Repository implementado con manejo de errores
  - DTOs mapeados correctamente con `@SerialName` para backend

## Backend - Endpoint para Crear Hábito

### POST /api/habits

**Request Body (CreateHabitRequestDto)**:
- `title`: String (5-100 chars, requerido)
- `description`: String? (opcional, máx 500 chars)
- `disciplineId`: Int (requerido, ≥ 1)
- `userId`: Int (requerido, ≥ 1)
- `tasks`: List\<CreateHabitTaskRequestDto\> (requerido, mínimo 1 tarea)

**CreateHabitTaskRequestDto**:
- `title`: String (3-100 chars)
- `description`: String?
- `habitDisciplineId`: Int?
- `weekDays`: String?
- `difficulty`: enum (EASY, MEDIUM, HARD, EPIC)
- `frequency`: enum (DAILY, WEEKLY, MONTHLY)
- `periodLength`: Int (≥ 1)
- `periodUnit`: enum (DAYS, WEEKS, MONTHS)
- `startDate`: String (ISO 8601: "yyyy-MM-dd")
- `completionCriteria`: enum (REPETITIONS, TIMER, EVIDENCE)
- `evidence`: enum (PHOTO, VIDEO, HEALTH_CONNECT)? (solo si completionCriteria=EVIDENCE)
- `repetitionCriteria`: RepetitionCriteriaRequestDto? (solo si completionCriteria=REPETITIONS)
- `xpValue`: Int? (≥ 0)
- `isActive`: Boolean?

**RepetitionCriteriaRequestDto**:
- `repetitions`: Int (≥ 1)
- `measurementUnit`: enum (REPS, SERIES, KMS, CALS)
- `isPartialAllowed`: Boolean?
- `isActive`: Boolean?

**Validaciones Cruzadas (Backend)**:
- Si `completionCriteria = REPETITIONS` → `repetitionCriteria` es obligatorio
- Si `completionCriteria ≠ REPETITIONS` → `repetitionCriteria` no debe enviarse
- Si `completionCriteria ≠ EVIDENCE` → `evidence` no debe enviarse

**Response**:
- HTTP 201 Created con JSON: `{ success: true, message: "..." }`
- HTTP 400 si validaciones fallan
- HTTP 500 si error del servidor

## DTOs en Kotlin (COMPLETADOS)

### Enums (en `/app/src/main/java/com/example/leveluplife/data/network/dto/`):
- `TaskDifficulty`: EASY, MEDIUM, HARD, EPIC
- `TaskFrequency`: DAILY, WEEKLY, MONTHLY
- `TaskPeriodUnit`: DAYS, WEEKS, MONTHS
- `TaskCompletionCriteria`: REPETITIONS, TIMER, EVIDENCE
- `TaskEvidence`: PHOTO, VIDEO, HEALTH_CONNECT
- `MeasurementUnit`: REPS, SERIES, KMS, CALS

### Data Classes:
- `CreateHabitRequestDto`: title, description?, disciplineId, userId, tasks
- `CreateHabitTaskRequestDto`: todos los campos de tarea con `@SerialName`
- `RepetitionCriteriaRequestDto`: repetitions, measurementUnit, isPartialAllowed?, isActive?
- `CreateHabitResponseDto`: success, message

**Nota**: Todos usan `@Serializable` y `@SerialName` de kotlinx-serialization

## Arquitectura MVVM

### Capas:
- **Data**: Repositories, API interfaces, DTOs, interceptors
- **Domain**: Validadores (pure functions)
- **UI**: Screens, ViewModels, Components

### Directorio actual:
```
app/src/main/java/com/example/leveluplife/
├── data/
│   ├── auth/ (AuthRepository, TokenStore, etc.)
│   ├── habits/ (HabitRepository, DefaultHabitRepository)
│   ├── network/ (API interfaces, DTOs, interceptors)
│   └── preferences/ (ThemePreferences, DebugApiPreferences)
├── domain/
│   └── validation/ (Validators.kt)
└── ui/
    ├── auth/ (LoginScreen, LoginViewModel, LoginUiState)
    ├── home/ (HomeScreen, HomeViewModel, HomeUiState)
    ├── dashboard/ (placeholder)
    ├── navigation/ (AppNavigation)
    ├── theme/ (LevelUpLifeTheme, ThemeController)
    ├── habit/ (CreateHabitScreen, CreateHabitViewModel, CreateHabitUiState)
    └── components/ (LulPrimaryButton, LulLogo, etc.)
```

## Plan de Implementación - COMPLETADO

### Fase 1: DTOs en Kotlin ✅
- [x] Crear enums: TaskDifficulty, TaskFrequency, TaskPeriodUnit, TaskCompletionCriteria, TaskEvidence, MeasurementUnit
- [x] Crear DTOs:
  - RepetitionCriteriaRequestDto.kt
  - CreateHabitTaskRequestDto.kt
  - CreateHabitRequestDto.kt
  - CreateHabitResponseDto.kt

### Fase 2: API Interface ✅
- [x] Agregar endpoint `POST /api/habits` en HabitsApi.kt
- [x] Usar Retrofit con kotlinx-serialization

### Fase 3: Repository ✅
- [x] Agregar método `createHabit()` en HabitRepository
- [x] Implementar en DefaultHabitRepository con manejo de errores
- [x] Manejo de HTTP 400/500 con mensajes del backend

### Fase 4: UI (Pendiente)
- [ ] Crear CreateHabitScreen.kt
- [ ] Crear CreateHabitViewModel.kt
- [ ] Crear CreateHabitUiState.kt
- [ ] Implementar UI con Jetpack Compose (scroll, lista de tareas dinámica)

### Fase 5: Navigation (Pendiente)
- [ ] Agregar ruta Routes.CREATE_HABIT
- [ ] Agregar pantalla en AppNavigation.kt

## Notas Importantes
- El backend ya soporta múltiples tareas en un solo request
- El userId se obtiene del JWT (ya implementado en AuthSession)
- Mantener consistencia con los DTOs existentes (LoginRequest, HabitDto)
- Usar `@Serializable` de kotlinx-serialization
- Validar campos en el ViewModel antes de enviar al repository
- **DateOnly en backend → String ISO 8601 en Kotlin** ("yyyy-MM-dd")

## Próximos Pasos
1. Crear UI con Jetpack Compose (Fase 4)
2. Integrar en navigation (Fase 5)
3. Implementar validaciones en ViewModel
4. Agregar lógica de tareas dinámicas (agregar/eliminar)

## Comandos Útiles
```bash
./gradlew assembleDebug          # build debug APK
./gradlew installDebug           # install on device
./gradlew test                   # unit tests
./gradlew clean                  # clean build
```

## Archivos Relevantes del Backend
- `/home/AdamAG/Documents/Moviles/LevelUpLife-Backend/Controllers/HabitController.cs`
- `/home/AdamAG/Documents/Moviles/LevelUpLife-Backend/DTOs/Requests/CreateHabitRequestDto.cs`
- `/home/AdamAG/Documents/Moviles/LevelUpLife-Backend/DTOs/Requests/CreateHabitTaskRequestDto.cs`
- `/home/AdamAG/Documents/Moviles/LevelUpLife-Backend/DTOs/Requests/CreateRepetitionCriteriaRequestDto.cs`
- `/home/AdamAG/Documents/Moviles/LevelUpLife-Backend/Services/HabitService.cs`

## Archivos Relevantes del Frontend (Creados)
- `/home/AdamAG/Documents/Moviles/LevelUpLife-Frontend/app/src/main/java/com/example/leveluplife/data/habits/HabitRepository.kt`
- `/home/AdamAG/Documents/Moviles/LevelUpLife-Frontend/app/src/main/java/com/example/leveluplife/data/network/HabitsApi.kt`
- `/home/AdamAG/Documents/Moviles/LevelUpLife-Frontend/app/src/main/java/com/example/leveluplife/data/network/dto/CreateHabitRequestDto.kt`
- `/home/AdamAG/Documents/Moviles/LevelUpLife-Frontend/app/src/main/java/com/example/leveluplife/data/network/dto/CreateHabitTaskRequestDto.kt`
- `/home/AdamAG/Documents/Moviles/LevelUpLife-Frontend/app/src/main/java/com/example/leveluplife/data/network/dto/RepetitionCriteriaRequestDto.kt`
- `/home/AdamAG/Documents/Moviles/LevelUpLife-Frontend/app/src/main/java/com/example/leveluplife/data/network/dto/CreateHabitResponseDto.kt`
- `/home/AdamAG/Documents/Moviles/LevelUpLife-Frontend/app/src/main/java/com/example/leveluplife/data/network/dto/TaskDifficulty.kt`
- `/home/AdamAG/Documents/Moviles/LevelUpLife-Frontend/app/src/main/java/com/example/leveluplife/data/network/dto/TaskFrequency.kt`
- `/home/AdamAG/Documents/Moviles/LevelUpLife-Frontend/app/src/main/java/com/example/leveluplife/data/network/dto/TaskPeriodUnit.kt`
- `/home/AdamAG/Documents/Moviles/LevelUpLife-Frontend/app/src/main/java/com/example/leveluplife/data/network/dto/TaskCompletionCriteria.kt`
- `/home/AdamAG/Documents/Moviles/LevelUpLife-Frontend/app/src/main/java/com/example/leveluplife/data/network/dto/TaskEvidence.kt`
- `/home/AdamAG/Documents/Moviles/LevelUpLife-Frontend/app/src/main/java/com/example/leveluplife/data/network/dto/MeasurementUnit.kt`
