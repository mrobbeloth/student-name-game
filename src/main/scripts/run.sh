#!/bin/bash

# Student Name Game Launcher for macOS/Linux
# This script launches the portable version with bundled JRE

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
JAVA_HOME="$SCRIPT_DIR/runtime"

if [ ! -x "$JAVA_HOME/bin/java" ]; then
    echo "Error: Java runtime not found at $JAVA_HOME"
    echo "Please ensure the runtime folder contains a valid JDK 21+ installation."
    exit 1
fi

cd "$SCRIPT_DIR"

"$JAVA_HOME/bin/java" \
    --module-path "$SCRIPT_DIR/lib" \
    --add-modules javafx.controls,javafx.fxml,javafx.media \
    -jar "$SCRIPT_DIR/student-name-game.jar" "$@"
