#!/bin/bash

# Student Name Game Launcher for macOS/Linux
# Uses bundled JRE (runtime/) if present, otherwise falls back to system Java

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

if [ -x "$SCRIPT_DIR/runtime/bin/java" ]; then
    JAVA_EXE="$SCRIPT_DIR/runtime/bin/java"
else
    JAVA_EXE="java"
fi

cd "$SCRIPT_DIR"

"$JAVA_EXE" \
    --module-path "$SCRIPT_DIR/lib" \
    --add-modules javafx.controls,javafx.fxml,javafx.media \
    --enable-native-access=javafx.graphics,javafx.media \
    -jar "$SCRIPT_DIR/student-name-game.jar" "$@"
