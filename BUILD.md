# Student Name Game - Build Instructions

This document provides comprehensive instructions for building the Student Name Game application.

> **Version Management**: The project version is centrally managed in [pom.xml](pom.xml). All references to `[VERSION]` in this document should be replaced with the current version from pom.xml (currently 1.0.1). The GitHub Actions workflow automatically extracts the version from pom.xml.

## Prerequisites

- **JDK 21** or later (with JavaFX support)
- **Maven 3.9+**
- **Git** (for version control)

### Installing Prerequisites

#### Windows
```powershell
# Using winget
winget install Microsoft.OpenJDK.21
winget install Apache.Maven

# Or download directly:
# JDK: https://adoptium.net/
# Maven: https://maven.apache.org/download.cgi
```

#### macOS
```bash
# Using Homebrew
brew install openjdk@21
brew install maven
```

#### Linux (Ubuntu/Debian)
```bash
sudo apt update
sudo apt install openjdk-21-jdk maven
```

## Project Structure

```
student-name-game/
├── pom.xml                          # Maven build configuration
├── src/
│   ├── main/
│   │   ├── java/                    # Java source files
│   │   │   └── com/example/namegame/
│   │   │       ├── model/           # Data models
│   │   │       ├── service/         # Business logic
│   │   │       ├── controller/      # FXML controllers
│   │   │       └── util/            # Utilities
│   │   ├── resources/               # FXML, CSS, sounds
│   │   ├── assembly/                # Assembly descriptors
│   │   ├── dist/                    # Distribution files
│   │   └── scripts/                 # Launcher scripts
│   └── test/                        # Test files
└── .github/workflows/               # CI/CD workflows
```

## Building the Application

### Quick Build (Development)

```bash
# Compile and run
mvn clean javafx:run

# Just compile
mvn clean compile
```

### Full Build (Production JAR)

```bash
# Create shaded (fat) JAR with all dependencies
mvn clean package -DskipTests

# Output: target/student-name-game-[VERSION]-SNAPSHOT-shaded.jar
```

### Portable Distribution

```bash
# Create portable ZIP distribution
mvn clean package assembly:single -DskipTests

# Output: target/student-name-game-[VERSION]-SNAPSHOT-portable.zip
```

## Platform-Specific Builds

### Creating a Custom JRE with jlink

For each platform, create a minimal JRE using jlink:

```bash
# Windows (run from Windows)
jlink ^
    --module-path "%JAVA_HOME%/jmods" ^
    --add-modules java.base,java.desktop,java.logging,java.naming,java.sql,java.xml,jdk.unsupported ^
    --output runtime ^
    --strip-debug ^
    --compress=2 ^
    --no-header-files ^
    --no-man-pages

# macOS/Linux
jlink \
    --module-path $JAVA_HOME/jmods \
    --add-modules java.base,java.desktop,java.logging,java.naming,java.sql,java.xml,jdk.unsupported \
    --output runtime \
    --strip-debug \
    --compress=2 \
    --no-header-files \
    --no-man-pages
```

### Native Installers with jpackage

#### Windows Installer (.msi)

```bash
# Requires WiX Toolset: https://wixtoolset.org/
mvn clean package -Pwindows -DskipTests

# Or manually:
jpackage ^
    --type msi ^
    --input target ^
    --main-jar student-name-game-[VERSION]-SNAPSHOT-shaded.jar ^
    --main-class com.example.namegame.Launcher ^
    --name "Student Name Game" ^
    --app-version [VERSION] ^
    --vendor "Your Organization" ^
    --win-menu ^
    --win-shortcut ^
    --dest target/installer
```

#### macOS Installer (.dmg)

```bash
mvn clean package -Pmacos -DskipTests

# Or manually:
jpackage \
    --type dmg \
    --input target \
    --main-jar student-name-game-[VERSION]-SNAPSHOT-shaded.jar \
    --main-class com.example.namegame.Launcher \
    --name "Student Name Game" \
    --app-version [VERSION] \
    --vendor "Your Organization" \
    --mac-package-name "StudentNameGame" \
    --dest target/installer
```

#### Linux Package (.deb)

```bash
mvn clean package -Plinux -DskipTests

# Or manually:
jpackage \
    --type deb \
    --input target \
    --main-jar student-name-game-[VERSION]-SNAPSHOT-shaded.jar \
    --main-class com.example.namegame.Launcher \
    --name "student-name-game" \
    --app-version [VERSION] \
    --vendor "Your Organization" \
    --linux-shortcut \
    --dest target/installer
```

## Assembling Portable Distribution

After building, assemble the portable distribution manually:

```bash
# 1. Build the shaded JAR
mvn clean package -DskipTests

# 2. Create distribution folder
mkdir -p dist/student-name-game

# 3. Copy files
cp target/student-name-game-*-shaded.jar dist/student-name-game/student-name-game.jar
cp src/main/scripts/run.bat dist/student-name-game/
cp src/main/scripts/run.sh dist/student-name-game/
chmod +x dist/student-name-game/run.sh
cp src/main/dist/portable.txt dist/student-name-game/
cp src/main/dist/README.txt dist/student-name-game/

# 4. Add platform-specific JRE (from jlink output)
cp -r runtime dist/student-name-game/

# 5. Create ZIP
cd dist
zip -r student-name-game-$(os)-portable.zip student-name-game/
```

## Running from Source

### Using Maven
```bash
mvn clean javafx:run
```

### Using IDE (IntelliJ IDEA, Eclipse)

1. Import as Maven project
2. Set JDK 21 as project SDK
3. Run `NameGameApplication.java` or `Launcher.java`

### Using Command Line
```bash
# After building
java --module-path /path/to/javafx/lib \
     --add-modules javafx.controls,javafx.fxml,javafx.media \
     -jar target/student-name-game-*-shaded.jar
```

## Testing

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=FuzzyMatcherTest

# Generate test coverage report
mvn test jacoco:report
```

## Troubleshooting

### Common Issues

1. **JavaFX not found**
   - Ensure JavaFX modules are on the module path
   - Use the javafx-maven-plugin for automatic module management

2. **Module resolution errors**
   - Check module-info.java for correct requires statements
   - Verify all dependencies export required packages

3. **shaded JAR fails to run**
   - Use `Launcher.java` as main class (not `NameGameApplication`)
   - Ensure maven-shade-plugin transformers are configured

4. **Native installer fails**
   - Windows: Install WiX Toolset 3.11+
   - macOS: May require signing certificates for distribution
   - Linux: Ensure dpkg-deb is available

### Getting Help

- Check the README.txt in the distribution
- Review application logs in `~/.namegame/` directory
- Open an issue on the project repository

## CI/CD

The project includes GitHub Actions workflows for automated builds:

- Push to `main` → Builds all platforms, creates draft release
- Pull request → Builds and tests on all platforms
- Tag `v*` → Creates release with all artifacts

See `.github/workflows/build.yml` for configuration details.
