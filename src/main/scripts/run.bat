@echo off
setlocal

REM Student Name Game Launcher for Windows
REM Uses bundled JRE (runtime\) if present, otherwise falls back to system Java

set SCRIPT_DIR=%~dp0

if exist "%SCRIPT_DIR%runtime\bin\java.exe" (
    set JAVA_EXE=%SCRIPT_DIR%runtime\bin\java.exe
) else (
    set JAVA_EXE=java
)

cd /d "%SCRIPT_DIR%"

"%JAVA_EXE%" ^
    --module-path "%SCRIPT_DIR%lib" ^
    --add-modules javafx.controls,javafx.fxml,javafx.media ^
    --enable-native-access=javafx.graphics,javafx.media ^
    -jar "%SCRIPT_DIR%student-name-game.jar" %*

if errorlevel 1 (
    echo.
    echo Application exited with an error.
    pause
)

endlocal
