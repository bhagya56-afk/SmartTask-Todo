package utils;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

/**
 * FileHandler - Utility class for file operations
 * Demonstrates OOP principles: Single Responsibility, Encapsulation
 */
public class FileHandler {

    // Constructor
    public FileHandler() {
        // Create data directory if it doesn't exist
        createDataDirectory();
    }

    /**
     * Read all lines from a file
     * @param filename Path to the file
     * @return List of lines from the file
     * @throws IOException if file operations fail
     */
    public List<String> readFile(String filename) throws IOException {
        Path path = Paths.get(filename);

        // Check if file exists
        if (!Files.exists(path)) {
            System.out.println("File does not exist: " + filename);
            return new ArrayList<>(); // Return empty list instead of throwing exception
        }

        // Read all lines from file
        try {
            return Files.readAllLines(path);
        } catch (IOException e) {
            System.err.println("Error reading file " + filename + ": " + e.getMessage());
            throw e;
        }
    }

    /**
     * Write lines to a file
     * @param filename Path to the file
     * @param lines List of lines to write
     * @throws IOException if file operations fail
     */
    public void writeFile(String filename, List<String> lines) throws IOException {
        Path path = Paths.get(filename);

        // Create parent directories if they don't exist
        Path parent = path.getParent();
        if (parent != null && !Files.exists(parent)) {
            try {
                Files.createDirectories(parent);
            } catch (IOException e) {
                System.err.println("Error creating directories: " + e.getMessage());
                throw e;
            }
        }

        try {
            Files.write(path, lines, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            System.err.println("Error writing file " + filename + ": " + e.getMessage());
            throw e;
        }
    }

    /**
     * Append a line to a file
     * @param filename Path to the file
     * @param line Line to append
     * @throws IOException if file operations fail
     */
    public void appendToFile(String filename, String line) throws IOException {
        Path path = Paths.get(filename);

        // Create parent directories if they don't exist
        Path parent = path.getParent();
        if (parent != null && !Files.exists(parent)) {
            try {
                Files.createDirectories(parent);
            } catch (IOException e) {
                System.err.println("Error creating directories: " + e.getMessage());
                throw e;
            }
        }

        try {
            Files.write(path, Arrays.asList(line), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("Error appending to file " + filename + ": " + e.getMessage());
            throw e;
        }
    }

    /**
     * Check if file exists
     * @param filename Path to the file
     * @return true if file exists
     */
    public boolean fileExists(String filename) {
        return Files.exists(Paths.get(filename));
    }

    /**
     * Delete a file
     * @param filename Path to the file
     * @return true if file was deleted successfully
     */
    public boolean deleteFile(String filename) {
        try {
            Path path = Paths.get(filename);
            return Files.deleteIfExists(path);
        } catch (IOException e) {
            System.err.println("Error deleting file " + filename + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Create a backup of a file
     * @param filename Path to the original file
     * @param backupSuffix Suffix to add to backup file (e.g., ".backup")
     * @return true if backup was created successfully
     */
    public boolean createBackup(String filename, String backupSuffix) {
        try {
            Path originalPath = Paths.get(filename);
            if (!Files.exists(originalPath)) {
                return false;
            }

            String backupFilename = filename + backupSuffix;
            Path backupPath = Paths.get(backupFilename);

            Files.copy(originalPath, backupPath, StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            System.err.println("Error creating backup: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get file size in bytes
     * @param filename Path to the file
     * @return file size in bytes, -1 if file doesn't exist
     */
    public long getFileSize(String filename) {
        try {
            Path path = Paths.get(filename);
            if (Files.exists(path)) {
                return Files.size(path);
            }
            return -1;
        } catch (IOException e) {
            System.err.println("Error getting file size: " + e.getMessage());
            return -1;
        }
    }

    /**
     * Get last modified time of file
     * @param filename Path to the file
     * @return last modified time as timestamp, -1 if file doesn't exist
     */
    public long getLastModified(String filename) {
        try {
            Path path = Paths.get(filename);
            if (Files.exists(path)) {
                return Files.getLastModifiedTime(path).toMillis();
            }
            return -1;
        } catch (IOException e) {
            System.err.println("Error getting last modified time: " + e.getMessage());
            return -1;
        }
    }

    /**
     * Create data directory if it doesn't exist
     */
    private void createDataDirectory() {
        try {
            Path dataDir = Paths.get("data");
            if (!Files.exists(dataDir)) {
                Files.createDirectory(dataDir);
                System.out.println("Created data directory");
            }
        } catch (IOException e) {
            System.err.println("Error creating data directory: " + e.getMessage());
        }
    }

    /**
     * Export data to CSV format
     * @param filename Output filename
     * @param headers CSV headers
     * @param data List of data rows
     * @throws IOException if file operations fail
     */
    public void exportToCSV(String filename, String[] headers, List<String[]> data) throws IOException {
        List<String> csvLines = new ArrayList<>();

        // Add headers
        csvLines.add(String.join(",", headers));

        // Add data rows
        for (String[] row : data) {
            csvLines.add(String.join(",", row));
        }

        writeFile(filename, csvLines);
    }

    /**
     * Read CSV file and return as list of string arrays
     * @param filename CSV filename
     * @return List of string arrays representing CSV rows
     * @throws IOException if file operations fail
     */
    public List<String[]> readCSV(String filename) throws IOException {
        List<String> lines = readFile(filename);
        List<String[]> csvData = new ArrayList<>();

        for (String line : lines) {
            String[] fields = line.split(",");
            csvData.add(fields);
        }

        return csvData;
    }

    /**
     * Clean up old backup files (keep only last N backups)
     * @param baseFilename Base filename pattern
     * @param backupSuffix Backup suffix pattern
     * @param keepCount Number of backups to keep
     */
    public void cleanupBackups(String baseFilename, String backupSuffix, int keepCount) {
        try {
            Path directory = Paths.get("data");
            if (!Files.exists(directory)) {
                return;
            }

            String pattern = new File(baseFilename).getName() + backupSuffix;

            // Get all backup files
            File[] backupFiles = directory.toFile().listFiles((dir, name) -> name.contains(pattern));

            if (backupFiles == null || backupFiles.length <= keepCount) {
                return; // No files to clean up
            }

            // Sort by last modified time (newest first)
            Arrays.sort(backupFiles, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));

            // Delete old backups (keep only the newest 'keepCount' files)
            for (int i = keepCount; i < backupFiles.length; i++) {
                try {
                    if (backupFiles[i].delete()) {
                        System.out.println("Deleted old backup: " + backupFiles[i].getName());
                    }
                } catch (Exception e) {
                    System.err.println("Error deleting backup: " + e.getMessage());
                }
            }

        } catch (Exception e) {
            System.err.println("Error cleaning up backups: " + e.getMessage());
        }
    }

    /**
     * Validate file path for security (prevent directory traversal)
     * @param filename File path to validate
     * @return true if path is safe
     */
    public boolean isValidPath(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            return false;
        }

        // Check for directory traversal attempts
        if (filename.contains("..") || filename.contains("//") || filename.startsWith("/")) {
            return false;
        }

        // Only allow files in data directory or current directory
        return filename.startsWith("data/") || !filename.contains("/");
    }

    /**
     * Get file extension
     * @param filename File name
     * @return file extension without dot, empty string if no extension
     */
    public String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }

        int lastDot = filename.lastIndexOf('.');
        if (lastDot > 0 && lastDot < filename.length() - 1) {
            return filename.substring(lastDot + 1).toLowerCase();
        }

        return "";
    }

    /**
     * Create a timestamped backup
     * @param filename Original file path
     * @return true if backup was created successfully
     */
    public boolean createTimestampedBackup(String filename) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        return createBackup(filename, ".backup." + timestamp);
    }
}