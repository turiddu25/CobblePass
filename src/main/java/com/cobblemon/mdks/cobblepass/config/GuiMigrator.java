package com.cobblemon.mdks.cobblepass.config;

import com.cobblemon.mdks.cobblepass.CobblePass;
import com.cobblemon.mdks.cobblepass.util.Constants;
import com.cobblemon.mdks.cobblepass.util.Utils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles automatic migration of GUI configuration from old spaced format to new compact format.
 * Creates backups and logs all migration actions for audit trail.
 */
public class GuiMigrator {
    private static final DateTimeFormatter BACKUP_TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    
    /**
     * Detects if the current GUI configuration uses old spaced format and migrates it if needed.
     * 
     * @return MigrationResult containing the outcome of the migration attempt
     */
    public static MigrationResult migrateIfNeeded() {
        MigrationResult result = new MigrationResult();
        
        // Check if gui.json exists
        String configPath = Constants.CONFIG_PATH + "/" + Constants.GUI_FILE;
        String content = Utils.readFileSync(Constants.CONFIG_PATH, Constants.GUI_FILE);
        
        if (content == null || content.isEmpty()) {
            result.setStatus(MigrationStatus.NO_CONFIG_FOUND);
            result.addLogEntry("No gui.json found - no migration needed");
            return result;
        }
        
        try {
            JsonObject json = JsonParser.parseString(content).getAsJsonObject();
            
            // Check if structure exists and needs migration
            if (!json.has("structure") || !json.get("structure").isJsonArray()) {
                result.setStatus(MigrationStatus.NO_MIGRATION_NEEDED);
                result.addLogEntry("No structure array found in configuration - no migration needed");
                return result;
            }
            
            JsonArray structureArray = json.getAsJsonArray("structure");
            List<String> originalStructure = new ArrayList<>();
            
            // Convert JsonArray to List<String>
            for (int i = 0; i < structureArray.size(); i++) {
                originalStructure.add(structureArray.get(i).getAsString());
            }
            
            // Detect if migration is needed
            if (!needsMigration(originalStructure)) {
                result.setStatus(MigrationStatus.NO_MIGRATION_NEEDED);
                result.addLogEntry("Configuration already uses compact format - no migration needed");
                return result;
            }
            
            // Perform migration
            result.setStatus(MigrationStatus.MIGRATION_STARTED);
            result.addLogEntry("Detected old spaced format configuration - starting migration");
            
            // Create backup
            if (!createBackup(content, result)) {
                result.setStatus(MigrationStatus.BACKUP_FAILED);
                return result;
            }
            
            // Convert structure to compact format
            List<String> migratedStructure = GuiStructureParser.parse(originalStructure);
            
            // Update the JSON with migrated structure
            JsonArray newStructureArray = new JsonArray();
            for (String row : migratedStructure) {
                newStructureArray.add(row);
            }
            json.add("structure", newStructureArray);
            
            // Save the migrated configuration
            Utils.writeFileSync(Constants.CONFIG_PATH, Constants.GUI_FILE, Utils.newGson().toJson(json));
            
            result.setStatus(MigrationStatus.MIGRATION_COMPLETED);
            result.setOriginalStructure(originalStructure);
            result.setMigratedStructure(migratedStructure);
            result.addLogEntry("Successfully migrated GUI configuration to compact format");
            result.addLogEntry("Original structure rows: " + originalStructure.size());
            result.addLogEntry("Migrated structure rows: " + migratedStructure.size());
            
            // Log the changes for audit trail
            logMigrationDetails(originalStructure, migratedStructure, result);
            
        } catch (Exception e) {
            result.setStatus(MigrationStatus.MIGRATION_FAILED);
            result.addLogEntry("Migration failed due to error: " + e.getMessage());
            CobblePass.LOGGER.error("GUI configuration migration failed", e);
        }
        
        return result;
    }
    
    /**
     * Detects if a structure array needs migration from spaced to compact format.
     * 
     * @param structure The structure to check
     * @return true if migration is needed
     */
    private static boolean needsMigration(List<String> structure) {
        if (structure == null || structure.isEmpty()) {
            return false;
        }
        
        // Check if any row appears to be in spaced format
        for (String row : structure) {
            if (row != null && isSpacedFormat(row)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Detects if a row is in spaced format.
     * Spaced format typically has alternating characters and spaces.
     * 
     * @param row The row to check
     * @return true if the row appears to be in spaced format
     */
    private static boolean isSpacedFormat(String row) {
        // If the row is longer than 9 characters, it might be spaced format
        if (row.length() <= 9) {
            return false;
        }
        
        // Check for alternating pattern of non-space and space characters
        // This would indicate spaced format like "# i # # B #"
        int spaceCount = 0;
        int nonSpaceCount = 0;
        boolean hasAlternatingPattern = true;
        
        for (int i = 0; i < Math.min(row.length(), 17); i++) {
            char c = row.charAt(i);
            if (c == ' ') {
                spaceCount++;
                // In spaced format, spaces should be at odd positions (1, 3, 5, etc.)
                if (i % 2 == 0 && i > 0) {
                    hasAlternatingPattern = false;
                }
            } else {
                nonSpaceCount++;
                // In spaced format, non-spaces should be at even positions (0, 2, 4, etc.)
                if (i % 2 == 1) {
                    hasAlternatingPattern = false;
                }
            }
        }
        
        // Consider it spaced format if:
        // 1. It has alternating pattern
        // 2. It has a reasonable number of spaces (indicating formatting spaces)
        // 3. The length suggests it's expanded format
        return hasAlternatingPattern && spaceCount >= 4 && row.length() > 13;
    }
    
    /**
     * Creates a timestamped backup of the original configuration.
     * 
     * @param originalContent The original configuration content
     * @param result The migration result to update with backup information
     * @return true if backup was successful
     */
    private static boolean createBackup(String originalContent, MigrationResult result) {
        try {
            String timestamp = LocalDateTime.now().format(BACKUP_TIMESTAMP_FORMAT);
            String backupFileName = "gui_backup_" + timestamp + ".json";
            String backupPath = Constants.CONFIG_PATH + "/backups";
            
            // Ensure backup directory exists
            Utils.checkForDirectory("/" + backupPath);
            
            // Write backup file
            Utils.writeFileSync(backupPath, backupFileName, originalContent);
            
            result.setBackupPath(backupPath + "/" + backupFileName);
            result.addLogEntry("Created backup at: " + backupPath + "/" + backupFileName);
            
            CobblePass.LOGGER.info("Created GUI configuration backup: " + backupPath + "/" + backupFileName);
            return true;
            
        } catch (Exception e) {
            result.addLogEntry("Failed to create backup: " + e.getMessage());
            CobblePass.LOGGER.error("Failed to create GUI configuration backup", e);
            return false;
        }
    }
    
    /**
     * Logs detailed migration information for audit trail.
     * 
     * @param originalStructure The original structure before migration
     * @param migratedStructure The structure after migration
     * @param result The migration result to update with details
     */
    private static void logMigrationDetails(List<String> originalStructure, List<String> migratedStructure, MigrationResult result) {
        result.addLogEntry("=== Migration Details ===");
        
        for (int i = 0; i < Math.max(originalStructure.size(), migratedStructure.size()); i++) {
            String original = i < originalStructure.size() ? originalStructure.get(i) : "N/A";
            String migrated = i < migratedStructure.size() ? migratedStructure.get(i) : "N/A";
            
            if (!original.equals(migrated)) {
                result.addLogEntry("Row " + i + ": '" + original + "' -> '" + migrated + "'");
            } else {
                result.addLogEntry("Row " + i + ": No change needed");
            }
        }
        
        result.addLogEntry("=== End Migration Details ===");
        
        // Also log to main logger for permanent record
        CobblePass.LOGGER.info("GUI Configuration Migration Details:");
        for (String logEntry : result.getLogEntries()) {
            CobblePass.LOGGER.info("  " + logEntry);
        }
    }
    
    /**
     * Manually triggers migration of a specific configuration file.
     * Used for administrative purposes or when automatic migration fails.
     * 
     * @param configContent The configuration content to migrate
     * @return MigrationResult containing the outcome
     */
    public static MigrationResult migrateConfiguration(String configContent) {
        MigrationResult result = new MigrationResult();
        
        if (configContent == null || configContent.isEmpty()) {
            result.setStatus(MigrationStatus.NO_CONFIG_FOUND);
            result.addLogEntry("No configuration content provided");
            return result;
        }
        
        try {
            JsonObject json = JsonParser.parseString(configContent).getAsJsonObject();
            
            if (!json.has("structure") || !json.get("structure").isJsonArray()) {
                result.setStatus(MigrationStatus.NO_MIGRATION_NEEDED);
                result.addLogEntry("No structure array found in configuration");
                return result;
            }
            
            JsonArray structureArray = json.getAsJsonArray("structure");
            List<String> originalStructure = new ArrayList<>();
            
            for (int i = 0; i < structureArray.size(); i++) {
                originalStructure.add(structureArray.get(i).getAsString());
            }
            
            if (!needsMigration(originalStructure)) {
                result.setStatus(MigrationStatus.NO_MIGRATION_NEEDED);
                result.addLogEntry("Configuration already uses compact format");
                return result;
            }
            
            // Perform migration
            List<String> migratedStructure = GuiStructureParser.parse(originalStructure);
            
            JsonArray newStructureArray = new JsonArray();
            for (String row : migratedStructure) {
                newStructureArray.add(row);
            }
            json.add("structure", newStructureArray);
            
            result.setStatus(MigrationStatus.MIGRATION_COMPLETED);
            result.setOriginalStructure(originalStructure);
            result.setMigratedStructure(migratedStructure);
            result.setMigratedContent(Utils.newGson().toJson(json));
            result.addLogEntry("Successfully migrated configuration to compact format");
            
        } catch (Exception e) {
            result.setStatus(MigrationStatus.MIGRATION_FAILED);
            result.addLogEntry("Migration failed: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Status of a migration operation.
     */
    public enum MigrationStatus {
        NO_CONFIG_FOUND,
        NO_MIGRATION_NEEDED,
        MIGRATION_STARTED,
        BACKUP_FAILED,
        MIGRATION_COMPLETED,
        MIGRATION_FAILED
    }
    
    /**
     * Result of a migration operation containing status, logs, and migrated data.
     */
    public static class MigrationResult {
        private MigrationStatus status;
        private List<String> logEntries;
        private String backupPath;
        private List<String> originalStructure;
        private List<String> migratedStructure;
        private String migratedContent;
        
        public MigrationResult() {
            this.logEntries = new ArrayList<>();
        }
        
        public void addLogEntry(String entry) {
            logEntries.add(entry);
        }
        
        public boolean isSuccessful() {
            return status == MigrationStatus.MIGRATION_COMPLETED || status == MigrationStatus.NO_MIGRATION_NEEDED;
        }
        
        public boolean wasMigrationPerformed() {
            return status == MigrationStatus.MIGRATION_COMPLETED;
        }
        
        // Getters and setters
        public MigrationStatus getStatus() { return status; }
        public void setStatus(MigrationStatus status) { this.status = status; }
        
        public List<String> getLogEntries() { return new ArrayList<>(logEntries); }
        
        public String getBackupPath() { return backupPath; }
        public void setBackupPath(String backupPath) { this.backupPath = backupPath; }
        
        public List<String> getOriginalStructure() { return originalStructure; }
        public void setOriginalStructure(List<String> originalStructure) { this.originalStructure = originalStructure; }
        
        public List<String> getMigratedStructure() { return migratedStructure; }
        public void setMigratedStructure(List<String> migratedStructure) { this.migratedStructure = migratedStructure; }
        
        public String getMigratedContent() { return migratedContent; }
        public void setMigratedContent(String migratedContent) { this.migratedContent = migratedContent; }
    }
}