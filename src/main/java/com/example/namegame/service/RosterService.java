package com.example.namegame.service;

import org.apache.poi.ss.usermodel.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Reads student roster from Excel files (.xls or .xlsx).
 */
public class RosterService {
    private static RosterService instance;
    
    private final Map<String, String> squashedToOriginal;
    private final List<String> rosterNames;
    
    private RosterService() {
        this.squashedToOriginal = new HashMap<>();
        this.rosterNames = new ArrayList<>();
    }
    
    public static synchronized RosterService getInstance() {
        if (instance == null) {
            instance = new RosterService();
        }
        return instance;
    }
    
    /**
     * Loads the roster from an Excel file.
     * @param directory The directory containing the roster file
     * @return true if loaded successfully, false otherwise
     */
    public boolean loadRoster(Path directory) {
        squashedToOriginal.clear();
        rosterNames.clear();
        
        // Find roster file
        Path rosterFile = findRosterFile(directory);
        if (rosterFile == null) {
            System.err.println("No roster file (roster.xls or roster.xlsx) found in: " + directory);
            return false;
        }
        
        try (InputStream is = new FileInputStream(rosterFile.toFile());
             Workbook workbook = WorkbookFactory.create(is)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            
            // Find the "Name" column
            Row headerRow = sheet.getRow(0);
            int nameColumnIndex = findNameColumn(headerRow);
            
            if (nameColumnIndex < 0) {
                System.err.println("'Name' column not found in roster");
                return false;
            }
            
            // Read names
            DataFormatter formatter = new DataFormatter();
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    Cell cell = row.getCell(nameColumnIndex);
                    if (cell != null) {
                        String name = formatter.formatCellValue(cell).trim();
                        if (!name.isEmpty()) {
                            rosterNames.add(name);
                            String squashed = FuzzyMatcher.squashRosterName(name);
                            squashedToOriginal.put(squashed, name);
                        }
                    }
                }
            }
            
            System.out.println("Loaded " + rosterNames.size() + " names from roster");
            return !rosterNames.isEmpty();
            
        } catch (IOException e) {
            System.err.println("Failed to read roster: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Finds the roster file in the directory.
     */
    private Path findRosterFile(Path directory) {
        String[] possibleNames = {"roster.xlsx", "roster.xls", "Roster.xlsx", "Roster.xls"};
        
        for (String name : possibleNames) {
            Path file = directory.resolve(name);
            if (Files.exists(file)) {
                return file;
            }
        }
        
        // Try to find any xls/xlsx file with "roster" in the name
        try {
            return Files.list(directory)
                .filter(p -> {
                    String fileName = p.getFileName().toString().toLowerCase();
                    return fileName.contains("roster") && 
                           (fileName.endsWith(".xls") || fileName.endsWith(".xlsx"));
                })
                .findFirst()
                .orElse(null);
        } catch (IOException e) {
            return null;
        }
    }
    
    /**
     * Finds the index of the "Name" column.
     */
    private int findNameColumn(Row headerRow) {
        if (headerRow == null) return -1;
        
        DataFormatter formatter = new DataFormatter();
        for (Cell cell : headerRow) {
            String value = formatter.formatCellValue(cell).trim();
            if ("Name".equalsIgnoreCase(value)) {
                return cell.getColumnIndex();
            }
        }
        return -1;
    }
    
    /**
     * Gets the map of squashed names to original roster names.
     */
    public Map<String, String> getSquashedToOriginal() {
        return Collections.unmodifiableMap(squashedToOriginal);
    }
    
    /**
     * Gets all roster names in original format.
     */
    public List<String> getRosterNames() {
        return Collections.unmodifiableList(rosterNames);
    }
    
    /**
     * Finds a roster entry by squashed name (exact match).
     */
    public String findBySquashedName(String squashed) {
        return squashedToOriginal.get(squashed.toLowerCase());
    }
    
    /**
     * Parses a roster name into first and last name.
     * @param rosterName Name in "Last, First" format
     * @return Array of [firstName, lastName]
     */
    public static String[] parseRosterName(String rosterName) {
        String[] parts = rosterName.split(",", 2);
        String lastName = parts[0].trim();
        String firstName = parts.length > 1 ? parts[1].trim() : "";
        return new String[] { firstName, lastName };
    }
    
    /**
     * Checks if the roster is loaded.
     */
    public boolean isLoaded() {
        return !rosterNames.isEmpty();
    }
    
    /**
     * Clears the roster.
     */
    public void clear() {
        squashedToOriginal.clear();
        rosterNames.clear();
    }
}
