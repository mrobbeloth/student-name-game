@echo off
setlocal

REM Student Name Game Launcher for Windows
REM This script launches the portable version with bundled JRE

set SCRIPT_DIR=%~dp0
set JAVA_HOME=%SCRIPT_DIR%runtime

if not exist "%JAVA_HOME%\bin\java.exe" (
    echo Error: Java runtime not found at %JAVA_HOME%
    echo Please ensure the runtime folder contains a valid JDK 21+ installation.
    pause
    exit /b 1
)

cd /d "%SCRIPT_DIR%"

"%JAVA_HOME%\bin\java" ^
    --module-path "%SCRIPT_DIR%lib" ^
    --add-modules javafx.controls,javafx.fxml,javafx.media ^
    -jar "%SCRIPT_DIR%student-name-game.jar" %*

if errorlevel 1 (
    echo.
    echo Application exited with an error.
    pause
)

endlocal
