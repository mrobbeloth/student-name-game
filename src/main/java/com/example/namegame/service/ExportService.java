package com.example.namegame.service;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.zip.*;

/**
 * Handles export and import of application data.
 */
public class ExportService {
    private static final int BUFFER_SIZE = 4096;
    
    private static ExportService instance;
    
    private ExportService() {}
    
    public static synchronized ExportService getInstance() {
        if (instance == null) {
            instance = new ExportService();
        }
        return instance;
    }
    
    /**
     * Exports all data to a ZIP file.
     * @param destination The destination ZIP file path
     * @throws IOException If export fails
     */
    public void exportData(Path destination) throws IOException {
        Path dataDir = ConfigService.getInstance().getDataDirectory();
        
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(destination.toFile()))) {
            Files.walk(dataDir)
                .filter(Files::isRegularFile)
                .forEach(file -> {
                    try {
                        String entryName = dataDir.relativize(file).toString();
                        ZipEntry entry = new ZipEntry(entryName);
                        zos.putNextEntry(entry);
                        Files.copy(file, zos);
                        zos.closeEntry();
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });
        } catch (UncheckedIOException e) {
            throw e.getCause();
        }
    }
    
    /**
     * Imports data from a ZIP file.
     * @param source The source ZIP file path
     * @throws IOException If import fails
     */
    public void importData(Path source) throws IOException {
        Path dataDir = ConfigService.getInstance().getDataDirectory();
        
        // Validate ZIP contents first
        if (!isValidBackup(source)) {
            throw new IOException("Invalid backup file: missing expected configuration files");
        }
        
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(source.toFile()))) {
            ZipEntry entry;
            byte[] buffer = new byte[BUFFER_SIZE];
            
            while ((entry = zis.getNextEntry()) != null) {
                Path targetPath = dataDir.resolve(entry.getName());
                
                // Security check: prevent zip slip attack
                if (!targetPath.normalize().startsWith(dataDir.normalize())) {
                    throw new IOException("Invalid ZIP entry: " + entry.getName());
                }
                
                if (entry.isDirectory()) {
                    Files.createDirectories(targetPath);
                } else {
                    Files.createDirectories(targetPath.getParent());
                    try (OutputStream os = Files.newOutputStream(targetPath)) {
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            os.write(buffer, 0, len);
                        }
                    }
                }
                zis.closeEntry();
            }
        }
        
        // Reload services after import
        MappingService.getInstance().reload();
        StatisticsService.getInstance().reload();
    }
    
    /**
     * Validates that the ZIP file is a valid backup.
     */
    private boolean isValidBackup(Path source) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(source.toFile()))) {
            ZipEntry entry;
            boolean hasConfig = false;
            
            while ((entry = zis.getNextEntry()) != null) {
                String name = entry.getName();
                if (name.equals("config.ini") || name.equals("statistics.json") || name.equals("mappings.json")) {
                    hasConfig = true;
                }
                zis.closeEntry();
            }
            
            return hasConfig;
        }
    }
    
    /**
     * Generates a default backup filename with timestamp.
     */
    public String generateBackupFilename() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HHmmss"));
        return "namegame-backup-" + timestamp + ".zip";
    }
}
