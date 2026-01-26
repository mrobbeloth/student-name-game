# Student Name Game

A JavaFX 21 cross-platform desktop application for learning student names through gamified exercises. Features matching, multiple choice, and fill-in-the-blank games with statistics tracking and progress monitoring.

![Java 21](https://img.shields.io/badge/Java-21%20LTS-orange)
![JavaFX](https://img.shields.io/badge/JavaFX-21-blue)
![Platform](https://img.shields.io/badge/Platform-Windows%20%7C%20macOS%20%7C%20Linux-lightgrey)

## Features

- **Three Game Modes**: Matching, Multiple Choice, and Fill-in-the-blank exercises
- **Smart Photo Matching**: Automatically matches student photos to roster names using fuzzy matching
- **Excel Integration**: Load student rosters from `.xls` or `.xlsx` files
- **Statistics Tracking**: Monitor learning progress with detailed statistics
- **Cross-Platform**: Runs on Windows, macOS, and Linux
- **Flexible Setup**: Configurable photo directories and roster files
- **Animation & Sound**: Engaging feedback with animations and sound effects
- **Export/Import**: Save and share statistics and progress data

## User Installation

### Requirements
- **Operating System**: Windows 10+, macOS 10.14+, or Linux (Ubuntu 18.04+)
- **No additional software required** - Java runtime is bundled with installers

### Option 1: Platform Installers (Recommended)

1. Go to the [Releases](../../releases) page
2. Download the appropriate installer for your platform:
   - **Windows**: `student-name-game-windows-installer.exe`
   - **macOS**: `student-name-game-macos-installer.pkg` 
   - **Linux**: `student-name-game-linux-installer.deb` or `student-name-game-linux-installer.rpm`
3. Run the installer and follow the setup wizard
4. Launch from your applications menu or desktop shortcut

### Option 2: Portable Version

1. Download `student-name-game-portable.zip` from [Releases](../../releases)
2. Extract to any folder on your computer
3. Run the appropriate launcher:
   - **Windows**: Double-click `run.bat`
   - **macOS/Linux**: Run `./run.sh` in terminal

### First-Time Setup

1. **Create a folder** for student photos (e.g., `C:\Students\Photos\`)
2. **Add student photos** to this folder:
   - Supported formats: `.jpg`, `.jpeg`, `.png`
   - Naming convention: `LastnameFirstname.jpg` (e.g., `SmithJohn.jpg`)
3. **Create a roster file** (Excel `.xls` or `.xlsx`):
   - Must have a "Name" column with entries in "Last, First" format
   - Save as `roster.xlsx` in the same folder as photos
4. **Launch the application** and configure the photo directory path

### Usage Tips

- **Photo Naming**: Name files to match student names for automatic matching
- **Manual Mapping**: Use the unmatched dialog to manually assign photos to names
- **Game Selection**: Choose from Matching, Multiple Choice, or Fill-in-the-blank modes
- **Statistics**: Track progress and identify students needing more practice
- **Keyboard Shortcuts**: Use `Ctrl+S` for settings, `Ctrl+?` for help

## Developer Setup

### Prerequisites

- **Java 21 LTS** (OpenJDK or Oracle JDK)
- **Maven 3.8+** (or use included Maven Wrapper)
- **Git** for version control
- **IDE** (IntelliJ IDEA, Eclipse, VS Code recommended)

### Environment Setup

#### 1. Install Java 21 LTS

**Windows (Chocolatey):**
```bash
choco install openjdk21
```

**Windows (Manual):**
- Download from [Microsoft OpenJDK](https://docs.microsoft.com/en-us/java/openjdk/download#openjdk-21)
- Set `JAVA_HOME` environment variable

**macOS (Homebrew):**
```bash
brew install openjdk@21
```

**Linux (Ubuntu/Debian):**
```bash
sudo apt update
sudo apt install openjdk-21-jdk
```

#### 2. Verify Installation
```bash
java -version
# Should show: openjdk version "21.x.x"
```

#### 3. Clone Repository
```bash
git clone https://github.com/mrobbeloth/student-name-game.git
cd student-name-game
```

### Development Commands

#### Build & Run
```bash
# Run in development mode
./mvnw javafx:run

# Build portable version
./mvnw clean package -DskipNativePackage

# Build with platform installers (requires additional setup)
./mvnw clean package

# Clean build artifacts
./mvnw clean
```

#### Testing
```bash
# Run tests (when implemented)
./mvnw test

# Run with specific test profile
./mvnw test -Dtest=SpecificTestClass
```

### Project Structure

```
src/main/
├── java/com/example/namegame/
│   ├── controller/          # FXML controllers
│   ├── model/              # Data models (records)
│   ├── service/            # Business logic
│   └── util/               # Utilities and helpers
├── resources/
│   ├── com/example/namegame/
│   │   ├── css/           # Stylesheets
│   │   └── views/         # FXML files
│   └── sounds/            # Audio files
└── assembly/              # Build configuration
```

### Architecture

- **Model**: Java 21 records for immutable data (`Student`, `GameSession`, etc.)
- **Service**: Business logic layer (`ConfigService`, `RosterService`, `ImageService`)
- **Controller**: JavaFX FXML controllers for UI interaction
- **Util**: Shared utilities (`AnimationFactory`, helpers)

### Technology Stack

- **Java 21 LTS** - Latest Java features (records, pattern matching, virtual threads)
- **JavaFX 21** - Modern desktop UI framework
- **Apache POI 5.5.1** - Excel file processing (`.xls`/`.xlsx`)
- **Apache Commons Text 1.15.0** - String utilities (Levenshtein distance)
- **Gson 2.13.2** - JSON serialization
- **Maven** - Build system and dependency management

### IDE Configuration

#### IntelliJ IDEA
1. Import as Maven project
2. Set Project SDK to Java 21
3. Enable preview features: `--enable-preview`
4. Install JavaFX plugin (if needed)

#### Eclipse
1. Import as "Existing Maven Projects"
2. Configure Java Build Path to use Java 21
3. Add VM arguments: `--enable-preview`

#### VS Code
1. Install "Extension Pack for Java"
2. Set `java.configuration.runtimes` to Java 21
3. Configure launch.json with `--enable-preview`

### Data Storage

- **Installed**: `~/.namegame/` (user home directory)
- **Portable**: `./data/` (relative to application)

Contains:
- `config.ini` - Application settings
- `mappings.json` - Manual image-to-name mappings  
- `statistics.json` - Game statistics and progress
- `exports/` - Exported statistics files

### Contributing

1. **Fork** the repository
2. **Create** a feature branch: `git checkout -b feature/amazing-feature`
3. **Commit** changes: `git commit -m 'Add amazing feature'`
4. **Push** to branch: `git push origin feature/amazing-feature`
5. **Create** a Pull Request

### Coding Guidelines

- Use **Java 21 features**: records, pattern matching, virtual threads
- Follow **JavaFX best practices**: properties, bindings, Platform.runLater()
- **All UI updates** must be on JavaFX Application Thread
- Use **meaningful variable names** and add JavaDoc for public methods
- **Format code** consistently (use IDE auto-formatting)

### Build System

The project uses Maven with the following key plugins:
- **maven-compiler-plugin** - Java 21 compilation with preview features
- **javafx-maven-plugin** - JavaFX application execution
- **maven-shade-plugin** - Creates fat JAR with all dependencies
- **maven-assembly-plugin** - Builds portable ZIP distributions

### Troubleshooting

#### Common Issues

**"Module not found" errors:**
- Ensure Java 21 is installed and `JAVA_HOME` is set correctly
- Verify JavaFX modules are on the module path

**Build failures:**
- Run `./mvnw clean` to clear old build artifacts
- Check Java version: `java -version`
- Ensure Maven Wrapper has execute permissions (Linux/macOS): `chmod +x mvnw`

**Runtime issues:**
- Verify photo directory exists and contains valid image files
- Check roster Excel file has a "Name" column
- Review application logs in data directory

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- **Apache POI** - Excel file processing
- **OpenJFX** - JavaFX framework
- **Apache Commons** - String utilities
- **Microsoft OpenJDK** - Java 21 LTS runtime