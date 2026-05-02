# Student Name Game - Copilot Instructions

## Project Overview
A JavaFX 24 cross-platform desktop application for learning student names through gamified exercises (matching, multiple choice, fill-in-the-blank).

## Tech Stack
- Java 25
- JavaFX 24 (controls, fxml, media)
- Apache POI 5.5.1 (Excel .xls/.xlsx parsing)
- Apache Commons Text 1.15.0 (Levenshtein distance)
- Maven (build system)

## Architecture
- **Model**: Java 25 records for data (Student, GameSession, etc.)
- **Service**: Business logic (ConfigService, RosterService, ImageService, etc.)
- **Controller**: FXML controllers for views
- **Util**: Animation factory, AppLogger, helpers

## Key Features
- Load student photos from configurable directory
- Parse roster from .xls/.xlsx files (Name column: "Last, First")
- Match squashed filenames to roster names with fuzzy matching
- Three game modes: Matching, Multiple Choice, Fill-in-the-blank
- Animations and sounds for feedback
- Statistics tracking and export/import
- Cross-platform: Windows, macOS, Linux (installers + portable ZIPs)

## Coding Guidelines
- Use Java 25 features: records, pattern matching, virtual threads
- Use JavaFX properties and bindings for reactive UI
- All UI updates on JavaFX Application Thread (Platform.runLater)
- Store user data in ~/.namegame/ (installed) or ./data/ (portable)

## Logging
- **Never** use `System.err.println()` or `System.out.println()` directly in service or controller code.
  On Windows jpackage (MSI/EXE) installs the app runs as a pure GUI process with no console
  window, so all console output is silently discarded.
- Use `AppLogger.log(String)` / `AppLogger.log(String, Throwable)` from
  `com.example.namegame.util.AppLogger` in every layer (services, controllers, utilities).
- `AppLogger.init(Path)` is called once at startup (`NameGameApplication.main()`) before any
  service singleton is created.  It directs output to both `stderr` **and** the persistent log
  file at `~/.namegame/namegame.log` (or `%TEMP%\namegame.log` as a fallback).
- Fatal startup exceptions are caught in `NameGameApplication.start()` and shown to the user
  via a JavaFX error dialog (with an expandable stack-trace area) rather than dying silently.

## Error Handling
- Wrap `Application.start()` in a broad try/catch and call `showFatalErrorDialog()` so the
  user always gets visible feedback when the app cannot start.
- Non-fatal errors in services (e.g. missing sound files, I/O on statistics) should be logged
  with `AppLogger.log()` and allow the app to continue running in a degraded state.

## Build Commands
```bash
mvn clean package                    # Full build
mvn clean package -DskipNativePackage # Portable only
mvn javafx:run                       # Run in development
```
