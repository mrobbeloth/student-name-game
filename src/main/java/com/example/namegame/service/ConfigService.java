package com.example.namegame.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Manages application configuration stored in a properties file.
 * Supports both installed mode (~/.namegame/) and portable mode (./data/).
 */
public class ConfigService {
    private static final String CONFIG_FILE = "config.properties";
    private static final String PORTABLE_MARKER = "portable.txt";
    private static final String KEY_IMAGES_DIRECTORY = "images.directory";
    private static final String KEY_FIRST_LAUNCH = "first.launch";
    
    private static ConfigService instance;
    
    private final Path dataDirectory;
    private final Path configPath;
    private Properties properties;
    
    private ConfigService() {
        this.dataDirectory = determineDataDirectory();
        this.configPath = dataDirectory.resolve(CONFIG_FILE);
        this.properties = new Properties();
        ensureDirectoryExists();
        loadOrCreateConfig();
    }
    
    public static synchronized ConfigService getInstance() {
        if (instance == null) {
            instance = new ConfigService();
        }
        return instance;
    }
    
    /**
     * Determines the data directory based on portable mode.
     */
    private Path determineDataDirectory() {
        // Check for portable marker in application directory
        Path appDir = getApplicationDirectory();
        Path portableMarker = appDir.resolve(PORTABLE_MARKER);
        
        if (Files.exists(portableMarker)) {
            // Portable mode: store data alongside app
            return appDir.resolve("data");
        } else {
            // Installed mode: store in user home
            return Path.of(System.getProperty("user.home"), ".namegame");
        }
    }
    
    /**
     * Gets the application directory.
     */
    private Path getApplicationDirectory() {
        try {
            // Try to get the JAR location
            String jarPath = ConfigService.class
                .getProtectionDomain()
                .getCodeSource()
                .getLocation()
                .toURI()
                .getPath();
            
            // Handle Windows paths that start with /
            if (System.getProperty("os.name").toLowerCase().contains("win") && jarPath.startsWith("/")) {
                jarPath = jarPath.substring(1);
            }
            
            Path path = Path.of(jarPath);
            if (Files.isRegularFile(path)) {
                return path.getParent();
            }
            return path;
        } catch (Exception e) {
            return Path.of(".");
        }
    }
    
    private void ensureDirectoryExists() {
        try {
            Files.createDirectories(dataDirectory);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create data directory: " + dataDirectory, e);
        }
    }
    
    private void loadOrCreateConfig() {
        try {
            if (Files.exists(configPath)) {
                try (InputStream input = Files.newInputStream(configPath)) {
                    properties.load(input);
                }
            } else {
                // Set default values
                properties.setProperty(KEY_FIRST_LAUNCH, "true");
                properties.setProperty(KEY_IMAGES_DIRECTORY, "");
                save();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load configuration", e);
        }
    }

    private void save() {
        try {
            try (OutputStream output = Files.newOutputStream(configPath)) {
                properties.store(output, "Student Name Game Configuration");
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to save configuration", e);
        }
    }
    
    public Path getDataDirectory() {
        return dataDirectory;
    }
    
    public boolean isFirstLaunch() {
        String value = properties.getProperty(KEY_FIRST_LAUNCH);
        return value == null || Boolean.parseBoolean(value);
    }
    
    public void markFirstLaunchComplete() {
        properties.setProperty(KEY_FIRST_LAUNCH, "false");
        save();
    }
    
    public Path getImagesDirectory() {
        String path = properties.getProperty(KEY_IMAGES_DIRECTORY);
        return (path == null || path.isEmpty()) ? null : Path.of(path);
    }
    
    public void setImagesDirectory(Path directory) {
        properties.setProperty(KEY_IMAGES_DIRECTORY, directory.toString());
        save();
    }
    
    public boolean isPortableMode() {
        Path appDir = getApplicationDirectory();
        return Files.exists(appDir.resolve(PORTABLE_MARKER));
    }
}
