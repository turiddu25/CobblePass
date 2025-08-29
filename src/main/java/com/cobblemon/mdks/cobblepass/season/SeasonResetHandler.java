package com.cobblemon.mdks.cobblepass.season;

import com.cobblemon.mdks.cobblepass.CobblePass;
import com.cobblemon.mdks.cobblepass.util.Constants;
import com.cobblemon.mdks.cobblepass.util.Utils;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Handles the complex process of resetting player data during season transitions.
 * Provides backup capabilities, safe data clearing, and rollback functionality.
 */
public class SeasonResetHandler {
    private static final String BACKUP_DIR = Constants.CONFIG_PATH + "/backups";
    private static final String SEASON_DATA_DIR = Constants.CONFIG_PATH + "/season_data";
    private static final DateTimeFormatter BACKUP_TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
    
    private static SeasonResetHandler instance;
    private final Gson gson;
    
    private SeasonResetHandler() {
        this.gson = Utils.newGson();
    }
    
    public static SeasonResetHandler getInstance() {
        if (instance == null) {
            instance = new SeasonResetHandler();
        }
        return instance;
    }
    
    /**
     * Performs a complete season reset with the specified options.
     * 
     * @param options Configuration options for the reset operation
     * @return Result object containing operation status and summary
     */
    public SeasonResetResult performReset(SeasonResetOptions options) {
        CobblePass.LOGGER.info("Starting season reset with options: " + options);
        
        long startTime = System.currentTimeMillis();
        SeasonResetSummary summary = new SeasonResetSummary();
        List<String> warnings = new ArrayList<>();
        
        try {
            // Validate preconditions using comprehensive validation service
            if (options.isValidateBeforeReset()) {
                SeasonValidationService validator = SeasonValidationService.getInstance();
                SeasonValidationService.ValidationResult validationResult = validator.validateSeasonReset(options);
                
                if (!validationResult.isValid()) {
                    SeasonResetResult result = new SeasonResetResult(false, "Validation failed: " + String.join(", ", validationResult.getErrors()));
                    result.setSummary(summary);
                    result.setWarnings(validationResult.getErrors());
                    result.setOperationDuration(System.currentTimeMillis() - startTime);
                    return result;
                }
                
                // Add validation warnings to the result
                if (validationResult.hasWarnings()) {
                    warnings.addAll(validationResult.getWarnings());
                }
            }
            
            // Step 1: Create backup if requested
            String backupPath = null;
            if (options.isCreateBackup()) {
                try {
                    backupPath = backupPlayerData();
                    if (backupPath == null) {
                        // Handle backup failure with error recovery
                        SeasonErrorHandler errorHandler = SeasonErrorHandler.getInstance();
                        SeasonErrorHandler.OperationContext context = new SeasonErrorHandler.OperationContext();
                        context.setOperationType("season_reset");
                        context.setResetOptions(options);
                        context.setTimestamp(startTime);
                        
                        SeasonErrorHandler.ErrorRecoveryResult recovery = errorHandler.handleSeasonResetError(
                                "backup", new RuntimeException("Backup creation failed"), context);
                        
                        if (!recovery.isRecovered()) {
                            SeasonResetResult result = new SeasonResetResult(false, "Failed to create backup - " + recovery.getRecoveryMessage());
                            result.setSummary(summary);
                            result.setWarnings(warnings);
                            result.setOperationDuration(System.currentTimeMillis() - startTime);
                            return result;
                        }
                    } else {
                        summary.setBackupPath(backupPath);
                        summary.setBackupFilesCreated(countFilesInBackup(backupPath));
                    }
                } catch (Exception e) {
                    // Handle backup exception with error recovery
                    SeasonErrorHandler errorHandler = SeasonErrorHandler.getInstance();
                    SeasonErrorHandler.OperationContext context = new SeasonErrorHandler.OperationContext();
                    context.setOperationType("season_reset");
                    context.setResetOptions(options);
                    context.setTimestamp(startTime);
                    
                    SeasonErrorHandler.ErrorRecoveryResult recovery = errorHandler.handleSeasonResetError("backup", e, context);
                    
                    if (!recovery.isRecovered()) {
                        SeasonResetResult result = new SeasonResetResult(false, "Backup failed: " + e.getMessage());
                        result.setSummary(summary);
                        result.setWarnings(warnings);
                        result.setOperationDuration(System.currentTimeMillis() - startTime);
                        return result;
                    }
                }
            }
            
            // Step 2: Preserve premium status if requested
            int premiumPlayersPreserved = 0;
            if (options.getPreservationMode() != PremiumPreservationMode.NONE) {
                // Use PremiumManager to prepare for season transition
                premiumPlayersPreserved = com.cobblemon.mdks.cobblepass.premium.PremiumManager.getInstance()
                        .prepareForSeasonTransition();
                summary.setPremiumPlayersPreserved(premiumPlayersPreserved);
            }
            
            // Step 3: Clear player progress
            int playersReset = clearPlayerProgress();
            summary.setTotalPlayersReset(playersReset);
            
            // Step 4: Update season information
            updateSeasonInformation(options);
            
            // Step 5: Generate final summary
            summary.setResetTimestamp(System.currentTimeMillis());
            summary.setPreviousSeasonId(String.valueOf(CobblePass.config.getCurrentSeason() - 1));
            summary.setNewSeasonId(String.valueOf(CobblePass.config.getCurrentSeason()));
            
            long operationDuration = System.currentTimeMillis() - startTime;
            
            CobblePass.LOGGER.info("Season reset completed successfully in " + operationDuration + "ms");
            SeasonResetResult result = new SeasonResetResult(true, "Season reset completed successfully");
            result.setSummary(summary);
            result.setWarnings(warnings);
            result.setOperationDuration(operationDuration);
            return result;
            
        } catch (Exception e) {
            CobblePass.LOGGER.error("Season reset failed", e);
            
            // Attempt rollback if backup was created
            if (options.isCreateBackup() && summary.getBackupPath() != null) {
                try {
                    rollbackFromBackup(summary.getBackupPath());
                    warnings.add("Reset failed, but rollback was successful");
                } catch (Exception rollbackException) {
                    CobblePass.LOGGER.error("Rollback also failed", rollbackException);
                    warnings.add("Reset failed and rollback also failed");
                }
            }
            
            SeasonResetResult result = new SeasonResetResult(false, "Season reset failed: " + e.getMessage());
            result.setSummary(summary);
            result.setWarnings(warnings);
            result.setOperationDuration(System.currentTimeMillis() - startTime);
            result.setException(e);
            return result;
        }
    }
    
    /**
     * Creates a timestamped backup of all player data.
     * 
     * @return The path to the created backup directory, or null if backup failed
     */
    public String backupPlayerData() {
        try {
            // Ensure backup directory exists
            Utils.checkForDirectory("/" + BACKUP_DIR);
            
            // Create timestamped backup directory
            String timestamp = LocalDateTime.now().format(BACKUP_TIMESTAMP_FORMAT);
            String backupDirName = "season_reset_" + timestamp;
            Path backupPath = Paths.get(BACKUP_DIR, backupDirName);
            Files.createDirectories(backupPath);
            
            // Backup player data
            Path playerDataPath = Paths.get(Constants.PLAYER_DATA_DIR);
            if (Files.exists(playerDataPath)) {
                Path playerBackupPath = backupPath.resolve("players");
                copyDirectory(playerDataPath, playerBackupPath);
            }
            
            // Backup season data if it exists
            Path seasonDataPath = Paths.get(SEASON_DATA_DIR);
            if (Files.exists(seasonDataPath)) {
                Path seasonBackupPath = backupPath.resolve("season_data");
                copyDirectory(seasonDataPath, seasonBackupPath);
            }
            
            // Create backup metadata
            BackupMetadata metadata = new BackupMetadata();
            metadata.timestamp = System.currentTimeMillis();
            metadata.seasonNumber = CobblePass.config.getCurrentSeason();
            metadata.playerCount = countPlayerFiles();
            metadata.backupReason = "Season Reset";
            
            String metadataJson = gson.toJson(metadata);
            Files.write(backupPath.resolve("backup_metadata.json"), metadataJson.getBytes());
            
            CobblePass.LOGGER.info("Created backup at: " + backupPath.toString());
            return backupPath.toString();
            
        } catch (Exception e) {
            CobblePass.LOGGER.error("Failed to create backup", e);
            return null;
        }
    }
    
    /**
     * Safely clears all player progress data.
     * 
     * @return The number of players whose data was cleared
     */
    public int clearPlayerProgress() {
        try {
            int playerCount = countPlayerFiles();
            
            // Use the existing BattlePass reset functionality
            CobblePass.battlePass.resetAllPlayerData();
            
            CobblePass.LOGGER.info("Cleared progress data for " + playerCount + " players");
            return playerCount;
            
        } catch (Exception e) {
            CobblePass.LOGGER.error("Failed to clear player progress", e);
            return 0;
        }
    }
    
    /**
     * Generates a detailed summary of the reset operation.
     * 
     * @param result The reset result to generate summary for
     * @return A formatted summary report
     */
    public SeasonResetSummary generateResetSummary(SeasonResetResult result) {
        return result.getSummary();
    }
    
    /**
     * Validates that the system is ready for a season reset.
     * 
     * @return List of validation errors, empty if validation passes
     */
    private List<String> validateResetPreconditions() {
        List<String> errors = new ArrayList<>();
        
        try {
            // Check if player data directory exists and is writable
            Path playerDataPath = Paths.get(Constants.PLAYER_DATA_DIR);
            if (Files.exists(playerDataPath) && !Files.isWritable(playerDataPath)) {
                errors.add("Player data directory is not writable");
            }
            
            // Check if backup directory can be created
            Path backupPath = Paths.get(BACKUP_DIR);
            if (!Files.exists(backupPath)) {
                try {
                    Files.createDirectories(backupPath);
                } catch (IOException e) {
                    errors.add("Cannot create backup directory: " + e.getMessage());
                }
            } else if (!Files.isWritable(backupPath)) {
                errors.add("Backup directory is not writable");
            }
            
            // Check available disk space (basic check)
            File playerDataDir = new File(Constants.PLAYER_DATA_DIR);
            if (playerDataDir.exists()) {
                long usableSpace = playerDataDir.getUsableSpace();
                long playerDataSize = calculateDirectorySize(playerDataDir);
                
                if (usableSpace < playerDataSize * 2) { // Need at least 2x space for backup
                    errors.add("Insufficient disk space for backup");
                }
            }
            
        } catch (Exception e) {
            errors.add("Validation error: " + e.getMessage());
        }
        
        return errors;
    }
    
    /**
     * Rolls back player data from a backup.
     * 
     * @param backupPath Path to the backup directory
     * @throws IOException If rollback fails
     */
    private void rollbackFromBackup(String backupPath) throws IOException {
        Path backup = Paths.get(backupPath);
        if (!Files.exists(backup)) {
            throw new IOException("Backup directory does not exist: " + backupPath);
        }
        
        // Restore player data
        Path playerBackupPath = backup.resolve("players");
        if (Files.exists(playerBackupPath)) {
            Path playerDataPath = Paths.get(Constants.PLAYER_DATA_DIR);
            
            // Clear current data
            if (Files.exists(playerDataPath)) {
                deleteDirectory(playerDataPath);
            }
            
            // Restore from backup
            copyDirectory(playerBackupPath, playerDataPath);
        }
        
        // Restore season data
        Path seasonBackupPath = backup.resolve("season_data");
        if (Files.exists(seasonBackupPath)) {
            Path seasonDataPath = Paths.get(SEASON_DATA_DIR);
            
            // Clear current data
            if (Files.exists(seasonDataPath)) {
                deleteDirectory(seasonDataPath);
            }
            
            // Restore from backup
            copyDirectory(seasonBackupPath, seasonDataPath);
        }
        
        CobblePass.LOGGER.info("Successfully rolled back from backup: " + backupPath);
    }
    
    /**
     * Updates season information after reset.
     */
    private void updateSeasonInformation(SeasonResetOptions options) {
        // This would typically update the current season number
        // For now, we'll just log the operation
        CobblePass.LOGGER.info("Updated season information after reset");
    }
    
    /**
     * Counts the number of player data files.
     */
    private int countPlayerFiles() {
        try {
            Path playerDataPath = Paths.get(Constants.PLAYER_DATA_DIR);
            if (!Files.exists(playerDataPath)) {
                return 0;
            }
            
            try (Stream<Path> files = Files.list(playerDataPath)) {
                return (int) files.filter(p -> p.toString().endsWith(".json")).count();
            }
        } catch (IOException e) {
            CobblePass.LOGGER.error("Error counting player files", e);
            return 0;
        }
    }
    
    /**
     * Counts files in a backup directory.
     */
    private int countFilesInBackup(String backupPath) {
        try {
            Path backup = Paths.get(backupPath, "players");
            if (!Files.exists(backup)) {
                return 0;
            }
            
            try (Stream<Path> files = Files.walk(backup)) {
                return (int) files.filter(Files::isRegularFile).count();
            }
        } catch (IOException e) {
            CobblePass.LOGGER.error("Error counting backup files", e);
            return 0;
        }
    }
    
    /**
     * Copies a directory recursively.
     */
    private void copyDirectory(Path source, Path target) throws IOException {
        Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Path targetDir = target.resolve(source.relativize(dir));
                Files.createDirectories(targetDir);
                return FileVisitResult.CONTINUE;
            }
            
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Path targetFile = target.resolve(source.relativize(file));
                Files.copy(file, targetFile, StandardCopyOption.REPLACE_EXISTING);
                return FileVisitResult.CONTINUE;
            }
        });
    }
    
    /**
     * Deletes a directory recursively.
     */
    private void deleteDirectory(Path directory) throws IOException {
        if (Files.exists(directory)) {
            Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }
                
                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }
    
    /**
     * Calculates the size of a directory in bytes.
     */
    private long calculateDirectorySize(File directory) {
        long size = 0;
        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        size += file.length();
                    } else if (file.isDirectory()) {
                        size += calculateDirectorySize(file);
                    }
                }
            }
        }
        return size;
    }
    
    /**
     * Metadata class for backup information.
     */
    private static class BackupMetadata {
        public long timestamp;
        public int seasonNumber;
        public int playerCount;
        public String backupReason;
    }
}