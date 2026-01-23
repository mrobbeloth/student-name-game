package com.example.namegame.service;

import com.example.namegame.model.ScoredMatch;
import com.example.namegame.model.Student;
import com.example.namegame.model.UnmatchedImage;
import javafx.application.Platform;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Manages loading and matching student images with roster entries.
 */
public class ImageService {
    private static final Set<String> IMAGE_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png");
    
    private static ImageService instance;
    
    private final List<Student> students;
    private final List<UnmatchedImage> unmatchedImages;
    private final FuzzyMatcher fuzzyMatcher;
    private WatchService watchService;
    private Thread watchThread;
    private final AtomicBoolean watching;
    private Consumer<Void> reloadCallback;
    
    private ImageService() {
        this.students = new ArrayList<>();
        this.unmatchedImages = new ArrayList<>();
        this.fuzzyMatcher = new FuzzyMatcher();
        this.watching = new AtomicBoolean(false);
    }
    
    public static synchronized ImageService getInstance() {
        if (instance == null) {
            instance = new ImageService();
        }
        return instance;
    }
    
    /**
     * Loads images from the configured directory and matches with roster.
     * @return true if at least one student was matched
     */
    public boolean loadImages() {
        students.clear();
        unmatchedImages.clear();
        
        Path directory = ConfigService.getInstance().getImagesDirectory();
        if (directory == null || !Files.isDirectory(directory)) {
            System.err.println("Invalid images directory");
            return false;
        }
        
        // Load roster first
        if (!RosterService.getInstance().loadRoster(directory)) {
            System.err.println("Failed to load roster");
            return false;
        }
        
        Map<String, String> squashedToOriginal = RosterService.getInstance().getSquashedToOriginal();
        Map<String, String> manualMappings = MappingService.getInstance().getAllMappings();
        
        try {
            Files.list(directory)
                .filter(this::isImageFile)
                .forEach(path -> processImageFile(path, squashedToOriginal, manualMappings));
        } catch (IOException e) {
            System.err.println("Failed to list directory: " + e.getMessage());
            return false;
        }
        
        System.out.println("Loaded " + students.size() + " students, " + 
                          unmatchedImages.size() + " unmatched images");
        
        return !students.isEmpty();
    }
    
    /**
     * Processes a single image file.
     */
    private void processImageFile(Path path, Map<String, String> squashedToOriginal, 
                                   Map<String, String> manualMappings) {
        String filename = path.getFileName().toString();
        
        // Check for manual mapping first
        String manualRosterName = manualMappings.get(filename);
        if (manualRosterName != null) {
            addStudent(path, manualRosterName);
            return;
        }
        
        // Try exact match
        String squashedFilename = FuzzyMatcher.extractNameFromFilename(filename);
        String exactMatch = squashedToOriginal.get(squashedFilename);
        if (exactMatch != null) {
            addStudent(path, exactMatch);
            return;
        }
        
        // Try fuzzy match with auto-accept for strong matches
        String fuzzyMatch = fuzzyMatcher.findBestMatch(squashedFilename, squashedToOriginal);
        if (fuzzyMatch != null) {
            // Auto-accept strong fuzzy matches
            addStudent(path, fuzzyMatch);
            return;
        }
        
        // Add to unmatched with suggestions
        List<ScoredMatch> suggestions = fuzzyMatcher.findMatches(squashedFilename, squashedToOriginal);
        unmatchedImages.add(new UnmatchedImage(path, suggestions));
    }
    
    /**
     * Adds a student from the given path and roster name.
     */
    private void addStudent(Path imagePath, String rosterName) {
        String[] names = RosterService.parseRosterName(rosterName);
        students.add(new Student(names[0], names[1], imagePath));
    }
    
    /**
     * Checks if a path is an image file.
     */
    private boolean isImageFile(Path path) {
        if (!Files.isRegularFile(path)) {
            return false;
        }
        String name = path.getFileName().toString().toLowerCase();
        return IMAGE_EXTENSIONS.stream().anyMatch(name::endsWith);
    }
    
    /**
     * Gets the list of matched students.
     */
    public List<Student> getStudents() {
        return Collections.unmodifiableList(students);
    }
    
    /**
     * Gets the list of unmatched images.
     */
    public List<UnmatchedImage> getUnmatchedImages() {
        return Collections.unmodifiableList(unmatchedImages);
    }
    
    /**
     * Assigns an unmatched image to a roster name.
     */
    public void assignUnmatched(UnmatchedImage image, String rosterName) {
        // Save mapping
        MappingService.getInstance().setMapping(image.filename(), rosterName);
        
        // Add to students
        addStudent(image.path(), rosterName);
        
        // Remove from unmatched
        unmatchedImages.remove(image);
    }
    
    /**
     * Assigns all unmatched images using their best suggestions.
     */
    public void assignAllSuggestions() {
        List<UnmatchedImage> toAssign = new ArrayList<>(unmatchedImages);
        for (UnmatchedImage image : toAssign) {
            ScoredMatch best = image.bestSuggestion();
            if (best != null) {
                assignUnmatched(image, best.rosterName());
            }
        }
    }
    
    /**
     * Starts watching the directory for changes.
     */
    public void startWatching(Consumer<Void> callback) {
        if (watching.get()) {
            return;
        }
        
        this.reloadCallback = callback;
        Path directory = ConfigService.getInstance().getImagesDirectory();
        if (directory == null) {
            return;
        }
        
        try {
            watchService = FileSystems.getDefault().newWatchService();
            directory.register(watchService,
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_DELETE,
                StandardWatchEventKinds.ENTRY_MODIFY);
            
            watching.set(true);
            
            // Use virtual thread for watching
            watchThread = Thread.startVirtualThread(this::watchLoop);
            
        } catch (IOException e) {
            System.err.println("Failed to start file watcher: " + e.getMessage());
        }
    }
    
    /**
     * The file watching loop.
     */
    private void watchLoop() {
        while (watching.get()) {
            try {
                WatchKey key = watchService.take();
                
                boolean hasChanges = false;
                for (WatchEvent<?> event : key.pollEvents()) {
                    Path changed = (Path) event.context();
                    if (isImageFile(changed) || changed.toString().toLowerCase().contains("roster")) {
                        hasChanges = true;
                    }
                }
                
                if (hasChanges && reloadCallback != null) {
                    Platform.runLater(() -> reloadCallback.accept(null));
                }
                
                key.reset();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (ClosedWatchServiceException e) {
                break;
            }
        }
    }
    
    /**
     * Stops watching the directory.
     */
    public void stopWatching() {
        watching.set(false);
        if (watchService != null) {
            try {
                watchService.close();
            } catch (IOException e) {
                // Ignore
            }
        }
        if (watchThread != null) {
            watchThread.interrupt();
        }
    }
    
    /**
     * Reloads images from the directory.
     */
    public void reload() {
        loadImages();
    }
}
