# Student Name Game - Copilot Instructions

## Project Overview
A JavaFX 21 cross-platform desktop application for learning student names through gamified exercises (matching, multiple choice, fill-in-the-blank).

## Tech Stack
- Java 21
- JavaFX 21 (controls, fxml, media)
- Apache POI 5.5.1 (Excel .xls/.xlsx parsing)
- ini4j 0.5.4 (configuration)
- Apache Commons Text 1.11.0 (Levenshtein distance)
- Maven (build system)

## Architecture
- **Model**: Java 21 records for data (Student, GameSession, etc.)
- **Service**: Business logic (ConfigService, RosterService, ImageService, etc.)
- **Controller**: FXML controllers for views
- **Util**: Animation factory, helpers

## Key Features
- Load student photos from configurable directory
- Parse roster from .xls/.xlsx files (Name column: "Last, First")
- Match squashed filenames to roster names with fuzzy matching
- Three game modes: Matching, Multiple Choice, Fill-in-the-blank
- Animations and sounds for feedback
- Statistics tracking and export/import
- Cross-platform: Windows, macOS, Linux (installers + portable ZIPs)

## Coding Guidelines
- Use Java 21 features: records, pattern matching, virtual threads
- Use JavaFX properties and bindings for reactive UI
- All UI updates on JavaFX Application Thread (Platform.runLater)
- Store user data in ~/.namegame/ (installed) or ./data/ (portable)

## Build Commands
```bash
mvn clean package                    # Full build
mvn clean package -DskipNativePackage # Portable only
mvn javafx:run                       # Run in development
```
