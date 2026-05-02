package com.example.namegame.service;

import javafx.scene.image.Image;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Loads JavaFX images with bounded reuse so controllers do not repeatedly hit the filesystem.
 */
public class ImageCacheService {
    private static ImageCacheService instance;

    private final Map<CacheKey, Image> cache;

    private ImageCacheService() {
        this.cache = new ConcurrentHashMap<>();
    }

    public static synchronized ImageCacheService getInstance() {
        if (instance == null) {
            instance = new ImageCacheService();
        }
        return instance;
    }

    /**
     * Loads an image for the requested dimensions, reusing a cached instance when available.
     */
    public Image load(Path path, double requestedWidth, double requestedHeight) throws IOException {
        CacheKey key = CacheKey.of(path, requestedWidth, requestedHeight);
        Image cached = cache.get(key);
        if (cached != null) {
            return cached;
        }

        try (InputStream inputStream = Files.newInputStream(path)) {
            Image image = new Image(inputStream, requestedWidth, requestedHeight, true, true);
            cache.put(key, image);
            return image;
        }
    }

    /**
     * Clears all cached images so modified files can be reloaded from disk.
     */
    public void clear() {
        cache.clear();
    }

    private record CacheKey(String normalizedPath, int requestedWidth, int requestedHeight) {
        private static CacheKey of(Path path, double requestedWidth, double requestedHeight) {
            Path normalized = path.toAbsolutePath().normalize();
            return new CacheKey(
                normalized.toString(),
                Math.max(0, (int) Math.round(requestedWidth)),
                Math.max(0, (int) Math.round(requestedHeight))
            );
        }
    }
}
