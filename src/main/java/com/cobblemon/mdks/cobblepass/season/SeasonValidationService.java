package com.cobblemon.mdks.cobblepass.season;

import com.cobblemon.mdks.cobblepass.CobblePass;
import com.cobblemon.mdks.cobblepass.util.Constants;
import com.cobblemon.mdks.cobblepass.util.LangManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Comprehensive validation service for season management operations.
 * Provides pre-operation validation, configuration checks, and system health verification.
 */
public class SeasonValidationService {
    private static SeasonValidationService instance;
    
    // Minimum disk space requirements (in bytes)
    private static final long MIN_BACKUP_SPACE = 100 * 1024 * 1024; // 100MB
    private static final long MIN_OPERATION_SPACE = 50 * 1024 * 1024; // 50MB
    
    private SeasonValidationService() {}
    
    public static SeasonValidationService getInstance() {
        if (instance == null) {
            instance = new SeasonValidationService();
        }
        return instance;
    }
    
    /**
     * Performs comprehensive pre-operation validation for season reset.
     * 
     * @param options The season reset options to validate
     * @return Validation result containing any errors or warnings
     */
    public ValidationResult validateSeasonReset(SeasonResetOptions options) {
        ValidationResult result = new ValidationResult();
        
        try {
            // File system validation
            validateFileSystemPermissions(result);
            validateDiskSpace(result, options.isCreateBackup());
            
            // Configuration validation
            validateSeasonConfiguration(result);
            validateResetOptions(result, options);
            
            // Player data validation
            validatePlayerDataIntegrity(result);
            
            // System state validation
            validateSystemState(result);
            
            // Premium system validation
            validatePremiumSystem(result, options);
            
        } catch (Exception e) {
            result.addError("Validation failed with exception: " + e.getMessage());
            CobblePass.LOGGER.error("Validation exception", e);
        }
        
        return result;
    }
    
    /**
     * Performs validation for season start operations.
     * 
     * @param options The season start options to validate
     * @return Validation result containing any errors or warnings
     */
    public ValidationResult validateSeasonStart(SeasonStartOptions options) {
        ValidationResult result = new ValidationResult();
        
        try {
            // Basic option validation
            if (options.getSeasonId() == null || options.getSeasonId().trim().isEmpty()) {
                result.addError("Season ID cannot be empty");
            }
            
            if (options.getSeasonName() == null || options.getSeasonName().trim().isEmpty()) {
                result.addError("Season name cannot be empty");
            }
            
            if (options.getMaxLevel() <= 0) {
                result.addError("Max level must be greater than 0");
            }
            
            if (options.getSeasonDurationDays() <= 0) {
                result.addError("Season duration must be greater than 0 days");
            }
            
            // File system validation
            validateFileSystemPermissions(result);
            
            // Configuration validation
            validateSeasonConfiguration(result);
            
            // Premium restoration validation
            if (options.isRestorePremiumStatus()) {
                validatePremiumRestoration(result, options);
            }
            
        } catch (Exception e) {
            result.addError("Season start validation failed: " + e.getMessage());
            CobblePass.LOGGER.error("Season start validation exception", e);
        }
        
        return result;
    }
    
    /**
     * Validates file system permissions and accessibility.
     */
    private void validateFileSystemPermissions(ValidationResult result) {
        try {
            // Check player data directory
            Path playerDataPath = Paths.get(Constants.PLAYER_DATA_DIR);
            if (Files.exists(playerDataPath)) {
                if (!Files.isReadable(playerDataPath)) {
                    result.addError("Player data directory is not readable");
                }
                if (!Files.isWritable(playerDataPath)) {
                    result.addError("Player data directory is not writable");
                }
            } else {
                // Try to create the directory
                try {
                    Files.createDirectories(playerDataPath);
                    result.addInfo("Created player data directory");
                } catch (IOException e) {
                    result.addError("Cannot create player data directory: " + e.getMessage());
                }
            }
            
            // Check config directory
            Path configPath = Paths.get(Constants.CONFIG_PATH);
            if (!Files.exists(configPath)) {
                try {
                    Files.createDirectories(configPath);
                    result.addInfo("Created config directory");
                } catch (IOException e) {
                    result.addError("Cannot create config directory: " + e.getMessage());
                }
            } else if (!Files.isWritable(configPath)) {
                result.addError("Config directory is not writable");
            }
            
            // Check backup directory accessibility
            Path backupPath = Paths.get(Constants.CONFIG_PATH + "/backups");
            if (!Files.exists(backupPath)) {
                try {
                    Files.createDirectories(backupPath);
                    result.addInfo("Created backup directory");
                } catch (IOException e) {
                    result.addError("Cannot create backup directory: " + e.getMessage());
                }
            } else if (!Files.isWritable(backupPath)) {
                result.addError("Backup directory is not writable");
            }
            
        } catch (Exception e) {
            result.addError("File system validation failed: " + e.getMessage());
        }
    }
    
    /**
     * Validates available disk space for operations.
     */
    private void validateDiskSpace(ValidationResult result, boolean needsBackupSpace) {
        try {
            File playerDataDir = new File(Constants.PLAYER_DATA_DIR);
            if (!playerDataDir.exists()) {
                return; // No data to backup
            }
            
            long usableSpace = playerDataDir.getUsableSpace();
            long playerDataSize = calculateDirectorySize(playerDataDir);
            
            if (needsBackupSpace) {
                long requiredSpace = Math.max(playerDataSize * 2, MIN_BACKUP_SPACE);
                if (usableSpace < requiredSpace) {
                    result.addError(String.format(
                            "Insufficient disk space. Required: %d MB, Available: %d MB",
                            requiredSpace / (1024 * 1024),
                            usableSpace / (1024 * 1024)
                    ));
                } else if (usableSpace < requiredSpace * 1.5) {
                    result.addWarning(String.format(
                            "Low disk space. Required: %d MB, Available: %d MB",
                            requiredSpace / (1024 * 1024),
                            usableSpace / (1024 * 1024)
                    ));
                }
            } else {
                if (usableSpace < MIN_OPERATION_SPACE) {
                    result.addWarning("Very low disk space available");
                }
            }
            
        } catch (Exception e) {
            result.addWarning("Could not check disk space: " + e.getMessage());
        }
    }
    
    /**
     * Validates season configuration integrity.
     */
    private void validateSeasonConfiguration(ValidationResult result) {
        try {
            if (CobblePass.config == null) {
                result.addError("Configuration not initialized");
                return;
            }
            
            // Check season reset configuration
            if (CobblePass.config.getSeasonResetConfig() == null) {
                result.addWarning("Season reset configuration not found, using defaults");
            }
            
            // Check premium configuration
            if (CobblePass.config.getPremiumConfig() == null) {
                result.addError("Premium configuration not found");
            } else {
                var premiumConfig = CobblePass.config.getPremiumConfig();
                if (premiumConfig.getMode() == null) {
                    result.addError("Premium mode not configured");
                }
            }
            
            // Check XP progression configuration
            if (CobblePass.config.getXpProgression() == null) {
                result.addError("XP progression configuration not found");
            }
            
            // Check max level configuration
            if (CobblePass.config.getMaxLevel() <= 0) {
                result.addError("Invalid max level configuration");
            }
            
        } catch (Exception e) {
            result.addError("Configuration validation failed: " + e.getMessage());
        }
    }
    
    /**
     * Validates season reset options.
     */
    private void validateResetOptions(ValidationResult result, SeasonResetOptions options) {
        try {
            if (options == null) {
                result.addError("Season reset options cannot be null");
                return;
            }
            
            // Validate preservation mode
            if (options.getPreservationMode() == null) {
                result.addError("Premium preservation mode must be specified");
            }
            
            // Check for conflicting options
            if (options.getPreservationMode() == PremiumPreservationMode.SYNC_PERMISSIONS &&
                CobblePass.config.getPremiumConfig().getMode() != com.cobblemon.mdks.cobblepass.premium.PremiumMode.PERMISSION) {
                result.addWarning("Premium preservation mode is SYNC_PERMISSIONS but premium mode is not PERMISSION");
            }
            
            // Validate backup requirements
            if (options.isCreateBackup() && !options.isValidateBeforeReset()) {
                result.addWarning("Backup is enabled but validation is disabled - this may cause backup failures");
            }
            
        } catch (Exception e) {
            result.addError("Reset options validation failed: " + e.getMessage());
        }
    }
    
    /**
     * Validates player data integrity.
     */
    private void validatePlayerDataIntegrity(ValidationResult result) {
        try {
            File playerDataDir = new File(Constants.PLAYER_DATA_DIR);
            if (!playerDataDir.exists()) {
                result.addInfo("No player data directory found");
                return;
            }
            
            File[] playerFiles = playerDataDir.listFiles((dir, name) -> name.endsWith(".json"));
            if (playerFiles == null) {
                result.addWarning("Could not read player data directory");
                return;
            }
            
            int validFiles = 0;
            int corruptFiles = 0;
            
            for (File file : playerFiles) {
                try {
                    String content = Files.readString(file.toPath());
                    if (content.trim().isEmpty()) {
                        corruptFiles++;
                    } else {
                        // Basic JSON validation
                        com.google.gson.JsonParser.parseString(content);
                        validFiles++;
                    }
                } catch (Exception e) {
                    corruptFiles++;
                }
            }
            
            result.addInfo(String.format("Player data validation: %d valid files, %d corrupt files", validFiles, corruptFiles));
            
            if (corruptFiles > 0) {
                result.addWarning(String.format("%d player data files appear to be corrupt", corruptFiles));
            }
            
        } catch (Exception e) {
            result.addWarning("Player data validation failed: " + e.getMessage());
        }
    }
    
    /**
     * Validates current system state.
     */
    private void validateSystemState(ValidationResult result) {
        try {
            // Check if season is active
            if (!CobblePass.config.isSeasonActive()) {
                result.addError("No active season to reset");
            }
            
            // Check if another operation is in progress
            SeasonManager seasonManager = SeasonManager.getInstance();
            if (seasonManager.isTransitionInProgress()) {
                result.addError("Another season transition is already in progress");
            }
            
            // Check server state
            if (CobblePass.getServer() == null) {
                result.addWarning("Server instance not available - some validations skipped");
            } else {
                int onlinePlayerCount = CobblePass.getServer().getPlayerList().getPlayerCount();
                if (onlinePlayerCount > 0) {
                    result.addWarning(String.format("%d players are currently online during season reset", onlinePlayerCount));
                }
            }
            
        } catch (Exception e) {
            result.addWarning("System state validation failed: " + e.getMessage());
        }
    }
    
    /**
     * Validates premium system configuration and state.
     */
    private void validatePremiumSystem(ValidationResult result, SeasonResetOptions options) {
        try {
            var premiumManager = com.cobblemon.mdks.cobblepass.premium.PremiumManager.getInstance();
            if (premiumManager == null) {
                result.addError("Premium manager not initialized");
                return;
            }
            
            // Validate current premium mode
            var currentMode = premiumManager.getCurrentMode();
            if (currentMode == null) {
                result.addError("Premium mode not set");
                return;
            }
            
            // Check preservation mode compatibility
            if (options.getPreservationMode() == PremiumPreservationMode.SYNC_PERMISSIONS &&
                currentMode != com.cobblemon.mdks.cobblepass.premium.PremiumMode.PERMISSION) {
                result.addWarning("Premium preservation mode SYNC_PERMISSIONS requires PERMISSION premium mode");
            }
            
            // Validate premium provider
            var provider = premiumManager.getCurrentProvider();
            if (provider == null) {
                result.addError("Premium provider not initialized");
            }
            
        } catch (Exception e) {
            result.addWarning("Premium system validation failed: " + e.getMessage());
        }
    }
    
    /**
     * Validates premium restoration configuration.
     */
    private void validatePremiumRestoration(ValidationResult result, SeasonStartOptions options) {
        try {
            if (options.getPremiumRestorationMode() == null) {
                result.addError("Premium restoration mode must be specified");
                return;
            }
            
            // Check if preservation data exists
            var preservationService = PremiumPreservationService.getInstance();
            var preservedPlayers = preservationService.getPreservedPremiumPlayers();
            
            if (options.getPremiumRestorationMode() != PremiumPreservationMode.NONE && preservedPlayers.isEmpty()) {
                result.addWarning("Premium restoration is enabled but no preserved premium players found");
            }
            
        } catch (Exception e) {
            result.addWarning("Premium restoration validation failed: " + e.getMessage());
        }
    }
    
    /**
     * Calculates the total size of a directory.
     */
    private long calculateDirectorySize(File directory) {
        long size = 0;
        try {
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
        } catch (Exception e) {
            CobblePass.LOGGER.warn("Could not calculate directory size for: " + directory.getPath(), e);
        }
        return size;
    }
    
    /**
     * Result class for validation operations.
     */
    public static class ValidationResult {
        private final List<String> errors = new ArrayList<>();
        private final List<String> warnings = new ArrayList<>();
        private final List<String> info = new ArrayList<>();
        private final Map<String, Object> metadata = new HashMap<>();
        
        public void addError(String error) {
            errors.add(error);
            CobblePass.LOGGER.error("Validation error: " + error);
        }
        
        public void addWarning(String warning) {
            warnings.add(warning);
            CobblePass.LOGGER.warn("Validation warning: " + warning);
        }
        
        public void addInfo(String info) {
            this.info.add(info);
            CobblePass.LOGGER.info("Validation info: " + info);
        }
        
        public void addMetadata(String key, Object value) {
            metadata.put(key, value);
        }
        
        public boolean isValid() {
            return errors.isEmpty();
        }
        
        public boolean hasWarnings() {
            return !warnings.isEmpty();
        }
        
        public List<String> getErrors() {
            return new ArrayList<>(errors);
        }
        
        public List<String> getWarnings() {
            return new ArrayList<>(warnings);
        }
        
        public List<String> getInfo() {
            return new ArrayList<>(info);
        }
        
        public Map<String, Object> getMetadata() {
            return new HashMap<>(metadata);
        }
        
        public String getFormattedReport() {
            StringBuilder report = new StringBuilder();
            
            if (!errors.isEmpty()) {
                report.append("ERRORS:\n");
                for (String error : errors) {
                    report.append("  - ").append(error).append("\n");
                }
            }
            
            if (!warnings.isEmpty()) {
                report.append("WARNINGS:\n");
                for (String warning : warnings) {
                    report.append("  - ").append(warning).append("\n");
                }
            }
            
            if (!info.isEmpty()) {
                report.append("INFO:\n");
                for (String infoItem : info) {
                    report.append("  - ").append(infoItem).append("\n");
                }
            }
            
            return report.toString();
        }
    }
}