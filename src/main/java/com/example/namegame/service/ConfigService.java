package com.example.namegame.service;

import org.ini4j.Ini;
import org.ini4j.Profile.Section;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Manages application configuration stored in an INI file.
 * Supports both installed mode (~/.namegame/) and portable mode (./data/).
 */
public class ConfigService {
    private static final String CONFIG_FILE = "config.ini";
    private static final String PORTABLE_MARKER = "portable.txt";
    private static final String SECTION_GENERAL = "General";
    private static final String KEY_IMAGES_DIRECTORY = "images.directory";
    private static final String KEY_FIRST_LAUNCH = "first.launch";
    
    private static ConfigService instance;
    
    private final Path dataDirectory;
    private final Path configPath;
    private Ini ini;
    
    private ConfigService() {
        this.dataDirectory = determineDataDirectory();
        this.configPath = dataDirectory.resolve(CONFIG_FILE);
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
                ini = new Ini(configPath.toFile());
            } else {
                ini = new Ini();
                Section section = ini.add(SECTION_GENERAL);
                section.put(KEY_FIRST_LAUNCH, "true");
                section.put(KEY_IMAGES_DIRECTORY, "");
                save();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load configuration", e);
        }
    }
    
    private void save() {
        try {
            ini.store(configPath.toFile());
        } catch (IOException e) {
            throw new RuntimeException("Failed to save configuration", e);
        }
    }
    
    public Path getDataDirectory() {
        return dataDirectory;
    }
    
    public boolean isFirstLaunch() {
        Section section = ini.get(SECTION_GENERAL);
        if (section == null) return true;
        String value = section.get(KEY_FIRST_LAUNCH);
        return value == null || Boolean.parseBoolean(value);
    }
    
    public void markFirstLaunchComplete() {
        Section section = ini.get(SECTION_GENERAL);
        if (section == null) {
            section = ini.add(SECTION_GENERAL);
        }
        section.put(KEY_FIRST_LAUNCH, "false");
        save();
    }
    
    public Path getImagesDirectory() {
        Section section = ini.get(SECTION_GENERAL);
        if (section == null) return null;
        String path = section.get(KEY_IMAGES_DIRECTORY);
        return (path == null || path.isEmpty()) ? null : Path.of(path);
    }
    
    public void setImagesDirectory(Path directory) {
        Section section = ini.get(SECTION_GENERAL);
        if (section == null) {
            section = ini.add(SECTION_GENERAL);
        }
        section.put(KEY_IMAGES_DIRECTORY, directory.toString());
        save();
    }
    
    public boolean isPortableMode() {
        Path appDir = getApplicationDirectory();
        return Files.exists(appDir.resolve(PORTABLE_MARKER));
    }
}
