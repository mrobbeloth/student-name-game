package com.example.namegame.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Lightweight application logger that writes to both stderr and a persistent
 * log file.  On Windows GUI installations (jpackage MSI) there is no console
 * window, so file logging ensures that diagnostics are always retrievable even
 * when {@code System.err} output is silently discarded.
 *
 * <p>Call {@link #init(Path)} once at application startup (before any service
 * is created) to enable file logging.  After that, every layer of the
 * application should use {@link #log(String)} / {@link #log(String, Throwable)}
 * instead of {@code System.err.println()}.
 */
public final class AppLogger {

    private static volatile Path logFile;

    private AppLogger() {}

    /**
     * Sets the log-file path.  Must be called exactly once at startup (from
     * {@code main()}) before any service singleton is created and before the
     * JavaFX toolkit is launched.  Subsequent calls are ignored.
     *
     * @param file writable path for the log file, or {@code null} to disable
     *             file logging (stderr only)
     */
    public static void init(Path file) {
        if (logFile == null) {
            logFile = file;
        }
    }

    /** Returns the log-file path, or {@code null} if file logging is disabled. */
    public static Path getLogFile() {
        return logFile;
    }

    /** Logs a plain message. */
    public static void log(String message) {
        log(message, null);
    }

    /**
     * Logs a message, optionally with a stack trace.
     *
     * @param message human-readable description
     * @param t       exception to append (may be {@code null})
     */
    public static void log(String message, Throwable t) {
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        StringBuilder sb = new StringBuilder();
        sb.append('[').append(timestamp).append("] ").append(message)
          .append(System.lineSeparator());
        if (t != null) {
            StringWriter sw = new StringWriter();
            t.printStackTrace(new PrintWriter(sw));
            sb.append(sw).append(System.lineSeparator());
        }
        String entry = sb.toString();
        System.err.print(entry);
        Path file = logFile;
        if (file != null) {
            try {
                Files.writeString(file, entry,
                        StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } catch (IOException ignored) {
                // Nothing left to do if we can't write the log file
            }
        }
    }
}
