# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Build
./gradlew app:assemble          # Build all variants
./gradlew app:assembleDebug     # Debug APK only

# Test
./gradlew app:testDebugUnitTest                  # Unit tests (debug)
./gradlew app:connectedDebugAndroidTest          # Instrumentation tests (requires device/emulator)

# Single test class
./gradlew app:testDebugUnitTest --tests "com.example.akashiconline.ExampleUnitTest"

# Lint
./gradlew app:lintDebug         # Lint checks
./gradlew app:lintFix           # Lint + auto-fix safe issues

# Install to connected device
./gradlew app:installDebug
```

## Architecture

Early-stage Android app using **Jetpack Compose + Material 3**, single-Activity architecture.

**Package:** `com.example.akashiconline`  
**Min SDK:** 24 | **Target/Compile SDK:** 36

### Navigation

`MainActivity.kt` owns the entire nav shell. `AppDestinations` enum defines the three bottom-nav destinations (HOME, FAVORITES, PROFILE) with their icon resources. Navigation state is held in `rememberSaveable` inside `AkashicOnlineApp()`, making destination selection survive recomposition and process death.

`NavigationSuiteScaffold` from `material3-adaptive-navigation-suite` auto-selects between bottom bar, rail, and drawer based on window size class — no manual breakpoint logic needed.

### Theme

`ui/theme/Theme.kt` wraps `MaterialTheme` in `AkashicOnlineTheme`. Dynamic color is enabled on Android 12+ (`Build.VERSION.SDK_INT >= Build.VERSION_CODES.S`). Dark/light is driven by `isSystemInDarkTheme()`.

### Dependency Management

All library versions are centralized in `gradle/libs.versions.toml` (version catalog). Reference them via `libs.*` aliases in `build.gradle.kts` — don't hardcode version strings in build files.
