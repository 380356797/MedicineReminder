# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Clean rebuild
./gradlew clean assembleDebug

# Output APK location
app/build/outputs/apk/debug/app-debug.apk

# Copy APK to Desktop for manual install (Windows)
cp app/build/outputs/apk/debug/app-debug.apk ~/Desktop/MedicineReminder-v1.1.apk
```

## Tech Stack & Constraints

- **AGP 9.0.1** — Hilt and kapt are incompatible with AGP 9. Do NOT add Hilt or kapt.
- **KSP 2.3.7** — Use `ksp` plugin (not kapt) for Room annotation processing.
- **Navigation3** (`androidx.navigation3`) — NOT Navigation Compose. Uses `NavDisplay`, `rememberNavBackStack`, `entryProvider`.
- **Manual DI** via `CompositionLocal` — No DI frameworks. ViewModels use a custom `appViewModel()` reflection-based factory.
- **Compose BOM 2026.03.01** with Material3. Use `Icons.AutoMirrored.Filled` (not `Icons.Default`) for list/arrow icons.
- **compileSdk/targetSdk 36**, minSdk 24, JVM 17.
- **Windows environment** — Use PowerShell syntax for shell commands. `adb` is at `C:/Users/Administrator/AppData/Local/Android/Sdk/platform-tools/adb.exe`.

## Architecture

### Dependency Injection

Three singletons live on `MedicineApp` (Application class): `database`, `medicineRepository`, `healthRepository`, `reminderScheduler`.

`AppDependencies` wraps the Compose tree with `CompositionLocalProvider`, exposing:
- `LocalMedicineRepository`, `LocalHealthRepository`, `LocalReminderScheduler`

Every screen creates its ViewModel via `appViewModel()`:
```kotlin
viewModel: HomeViewModel = appViewModel()
```
The `appViewModel()` function uses reflection to match ViewModel constructor parameter types to the CompositionLocal values. ViewModel constructors can take any combination of `MedicineRepository`, `HealthRepository`, `ReminderScheduler`.

### Navigation (Navigation3)

- `NavigationKeys.kt`: All routes are `@Serializable data object/data class` implementing `NavKey`
- `Navigation.kt`: `entryProvider` maps each `NavKey` to a composable. Navigation is via `backStack.add(key)` / `backStack.removeLastOrNull()`.
- Bottom nav bar shows on `HomeTab`, `HealthTab`, `ProfileTab` only.

### Database (Room)

5 entities: `Medicine`, `MedicineSchedule`, `MedicineLog`, `HealthIndicator`, `HealthRecord`.
Database is prepopulated on first creation via `PrepopulateCallback` — ~85 preset medicines (isPreset=true), ~37 health indicators.

DAOs return `Flow` for reactive queries, `suspend` for writes. Repositories are thin pass-through wrappers.

### MVVM Pattern

ViewModels expose `StateFlow` via `.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), default)`. Repositories are injected via constructor. No business logic in repositories.

### Reminder System

`AlarmManager` + `PendingIntent` broadcast → `ReminderReceiver` → launches full-screen `AlarmActivity` + posts notification fallback. `BootReceiver` re-schedules alarms on boot.

## Code Conventions

- **No Hilt imports** — never use `@Inject`, `@HiltViewModel`, `dagger.*`, `javax.inject.*`
- **New DAO query needed?** Add to existing `*Dao.kt`, add wrapper in `*Repository.kt`
- **New screen?** Add `NavKey` in `NavigationKeys.kt`, add `entry` in `Navigation.kt`, create `*Screen.kt` + `*ViewModel.kt`
- **ViewModel creation:** Use `appViewModel()`, do NOT use `viewModel()` directly
- **When exhaustiveness:** `RepeatType` enum has 4 values (DAILY, EVERY_OTHER_DAY, WEEKLY, CUSTOM) — all must be handled in `when` expressions
- **Locale:** UI text is in Chinese (简体中文)

## Agent skills

### Issue tracker

GitHub Issues in `github.com/380356797/MedicineReminder`. See `docs/agents/issue-tracker.md`.

### Triage labels

Default vocabulary: `needs-triage`, `needs-info`, `ready-for-agent`, `ready-for-human`, `wontfix`. See `docs/agents/triage-labels.md`.

### Domain docs

Single-context: `CONTEXT.md` + `docs/adr/` at repo root. See `docs/agents/domain.md`.
