package com.example.namegame.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages manual name-to-image mappings stored in JSON.
 */
public class MappingService {
    private static final String MAPPINGS_FILE = "mappings.json";
    
    private static MappingService instance;
    
    private final Path mappingsPath;
    private final Gson gson;
    private Map<String, String> mappings;
    
    private MappingService() {
        this.mappingsPath = ConfigService.getInstance().getDataDirectory().resolve(MAPPINGS_FILE);
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        load();
    }
    
    public static synchronized MappingService getInstance() {
        if (instance == null) {
            instance = new MappingService();
        }
        return instance;
    }
    
    private void load() {
        if (Files.exists(mappingsPath)) {
            try {
                String json = Files.readString(mappingsPath);
                Type type = new TypeToken<Map<String, String>>() {}.getType();
                mappings = gson.fromJson(json, type);
                if (mappings == null) {
                    mappings = new HashMap<>();
                }
            } catch (IOException e) {
                System.err.println("Failed to load mappings: " + e.getMessage());
                mappings = new HashMap<>();
            }
        } else {
            mappings = new HashMap<>();
        }
    }
    
    private void save() {
        try {
            String json = gson.toJson(mappings);
            Files.writeString(mappingsPath, json);
        } catch (IOException e) {
            System.err.println("Failed to save mappings: " + e.getMessage());
        }
    }
    
    /**
     * Gets the manual mapping for a filename.
     * @param filename The image filename
     * @return The mapped roster name, or null if not mapped
     */
    public String getMapping(String filename) {
        return mappings.get(filename);
    }
    
    /**
     * Sets a manual mapping for a filename.
     * @param filename The image filename
     * @param rosterName The roster name in "Last, First" format
     */
    public void setMapping(String filename, String rosterName) {
        mappings.put(filename, rosterName);
        save();
    }
    
    /**
     * Removes a mapping.
     * @param filename The image filename
     */
    public void removeMapping(String filename) {
        mappings.remove(filename);
        save();
    }
    
    /**
     * Gets all mappings.
     */
    public Map<String, String> getAllMappings() {
        return new HashMap<>(mappings);
    }
    
    /**
     * Clears all mappings.
     */
    public void clearAll() {
        mappings.clear();
        save();
    }
    
    /**
     * Reloads mappings from disk.
     */
    public void reload() {
        load();
    }
}
