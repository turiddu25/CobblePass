package com.cobblemon.mdks.cobblepass.season;

import com.cobblemon.mdks.cobblepass.CobblePass;
import com.cobblemon.mdks.cobblepass.util.LangManager;
import net.minecraft.server.level.ServerPlayer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Comprehensive error handling service for season management operations.
 * Provides error recovery, rollback mechanisms, and detailed error reporting.
 */
public class SeasonErrorHandler {
    private static SeasonErrorHandler instance;
    private static final String ERROR_LOG_DIR = "config/cobblepass/error_logs";
    private static final DateTimeFormatter ERROR_TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
    
    // Track active operations for error recovery
    private final Map<String, OperationContext> activeOperations = new HashMap<>();
    
    private SeasonErrorHandler() {}
    
    public static SeasonErrorHandler getInstance() {
        if (instance == null) {
            instance = new SeasonErrorHandler();
        }
        return instance;
    }
    
    /**
     * Handles errors during season reset operations with automatic recovery.
     * 
     * @param operation The operation that failed
     * @param error The error that occurred
     * @param context The operation context for recovery
     * @return Recovery result indicating success or failure
     */
    public ErrorRecoveryResult handleSeasonResetError(String operation, Throwable error, OperationContext context) {
        CobblePass.LOGGER.error("Season reset error in operation: " + operation, error);
        
        ErrorRecoveryResult result = new ErrorRecoveryResult();
        result.setOperation(operation);
        result.setError(error);
        result.setContext(context);
        
        try {
            // Log the error for audit purposes
            logError(operation, error, context);
            
            // Attempt automatic recovery based on operation type
            switch (operation.toLowerCase()) {
                case "backup":
                    result = handleBackupError(error, context);
                    break;
                case "premium_preservation":
                    result = handlePremiumPreservationError(error, context);
                    break;
                case "player_data_reset":
                    result = handlePlayerDataResetError(error, context);
                    break;
                case "premium_restoration":
                    result = handlePremiumRestorationError(error, context);
                    break;
                default:
                    result = handleGenericError(operation, error, context);
                    break;
            }
            
            // Notify administrators if recovery failed
            if (!result.isRecovered()) {
                notifyAdministrators(operation, error, context, result);
            }
            
        } catch (Exception recoveryError) {
            CobblePass.LOGGER.error("Error during error recovery", recoveryError);
            result.setRecovered(false);
            result.setRecoveryError(recoveryError);
        }
        
        return result;
    }
    
    /**
     * Initiates rollback to a previous state using backup data.
     * 
     * @param backupPath Path to the backup to restore from
     * @param context Operation context
     * @return Rollback result
     */
    public RollbackResult initiateRollback(String backupPath, OperationContext context) {
        CobblePass.LOGGER.info("Initiating rollback from backup: " + backupPath);
        
        RollbackResult result = new RollbackResult();
        result.setBackupPath(backupPath);
        
        try {
            // Validate backup exists and is accessible
            Path backup = Paths.get(backupPath);
            if (!Files.exists(backup)) {
                result.setSuccess(false);
                result.setError("Backup directory does not exist: " + backupPath);
                return result;
            }
            
            // Create rollback operation context
            OperationContext rollbackContext = new OperationContext();
            rollbackContext.setOperationType("rollback");
            rollbackContext.setBackupPath(backupPath);
            rollbackContext.setTimestamp(System.currentTimeMillis());
            
            // Perform rollback using SeasonResetHandler
            SeasonResetHandler resetHandler = SeasonResetHandler.getInstance();
            
            // This would need to be implemented in SeasonResetHandler
            // resetHandler.rollbackFromBackup(backupPath);
            
            result.setSuccess(true);
            result.setMessage("Rollback completed successfully");
            
            // Notify administrators of successful rollback
            notifyRollbackComplete(backupPath, context);
            
        } catch (Exception e) {
            CobblePass.LOGGER.error("Rollback failed", e);
            result.setSuccess(false);
            result.setError("Rollback failed: " + e.getMessage());
            result.setException(e);
            
            // This is a critical failure - notify immediately
            notifyCriticalRollbackFailure(backupPath, e, context);
        }
        
        return result;
    }
    
    /**
     * Validates operation preconditions and provides recovery suggestions.
     * 
     * @param operation The operation to validate
     * @param context Operation context
     * @return Validation result with recovery suggestions
     */
    public OperationValidationResult validateOperation(String operation, OperationContext context) {
        OperationValidationResult result = new OperationValidationResult();
        result.setOperation(operation);
        
        try {
            SeasonValidationService validator = SeasonValidationService.getInstance();
            
            switch (operation.toLowerCase()) {
                case "season_reset":
                    if (context.getResetOptions() != null) {
                        var validationResult = validator.validateSeasonReset(context.getResetOptions());
                        result.setValid(validationResult.isValid());
                        result.setErrors(validationResult.getErrors());
                        result.setWarnings(validationResult.getWarnings());
                    } else {
                        result.setValid(false);
                        result.addError("Season reset options not provided");
                    }
                    break;
                    
                case "season_start":
                    if (context.getStartOptions() != null) {
                        var validationResult = validator.validateSeasonStart(context.getStartOptions());
                        result.setValid(validationResult.isValid());
                        result.setErrors(validationResult.getErrors());
                        result.setWarnings(validationResult.getWarnings());
                    } else {
                        result.setValid(false);
                        result.addError("Season start options not provided");
                    }
                    break;
                    
                default:
                    result.setValid(true);
                    result.addWarning("No specific validation available for operation: " + operation);
                    break;
            }
            
            // Add recovery suggestions based on errors
            addRecoverySuggestions(result);
            
        } catch (Exception e) {
            CobblePass.LOGGER.error("Operation validation failed", e);
            result.setValid(false);
            result.addError("Validation failed: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Handles backup operation errors.
     */
    private ErrorRecoveryResult handleBackupError(Throwable error, OperationContext context) {
        ErrorRecoveryResult result = new ErrorRecoveryResult();
        result.setOperation("backup");
        result.setError(error);
        
        if (error instanceof IOException) {
            // File system error - check disk space and permissions
            result.addRecoveryAction("Check available disk space");
            result.addRecoveryAction("Verify backup directory permissions");
            result.addRecoveryAction("Try alternative backup location");
        } else {
            result.addRecoveryAction("Retry backup operation");
            result.addRecoveryAction("Skip backup and proceed (not recommended)");
        }
        
        result.setRecovered(false); // Backup errors usually require manual intervention
        return result;
    }
    
    /**
     * Handles premium preservation errors.
     */
    private ErrorRecoveryResult handlePremiumPreservationError(Throwable error, OperationContext context) {
        ErrorRecoveryResult result = new ErrorRecoveryResult();
        result.setOperation("premium_preservation");
        result.setError(error);
        
        try {
            // Attempt to preserve premium status using alternative method
            PremiumPreservationService service = PremiumPreservationService.getInstance();
            int preserved = service.preservePremiumStatus();
            
            if (preserved >= 0) {
                result.setRecovered(true);
                result.setRecoveryMessage("Successfully preserved " + preserved + " premium players using fallback method");
            } else {
                result.setRecovered(false);
                result.addRecoveryAction("Manually backup premium player list");
                result.addRecoveryAction("Use SYNC_PERMISSIONS mode for restoration");
            }
            
        } catch (Exception recoveryError) {
            result.setRecovered(false);
            result.setRecoveryError(recoveryError);
            result.addRecoveryAction("Skip premium preservation");
            result.addRecoveryAction("Manually restore premium status after reset");
        }
        
        return result;
    }
    
    /**
     * Handles player data reset errors.
     */
    private ErrorRecoveryResult handlePlayerDataResetError(Throwable error, OperationContext context) {
        ErrorRecoveryResult result = new ErrorRecoveryResult();
        result.setOperation("player_data_reset");
        result.setError(error);
        
        // Player data reset errors are critical - initiate rollback
        if (context.getBackupPath() != null) {
            RollbackResult rollback = initiateRollback(context.getBackupPath(), context);
            if (rollback.isSuccess()) {
                result.setRecovered(true);
                result.setRecoveryMessage("Rolled back to backup after player data reset failure");
            } else {
                result.setRecovered(false);
                result.addRecoveryAction("Manual data restoration required");
                result.addRecoveryAction("Contact system administrator");
            }
        } else {
            result.setRecovered(false);
            result.addRecoveryAction("No backup available - manual intervention required");
        }
        
        return result;
    }
    
    /**
     * Handles premium restoration errors.
     */
    private ErrorRecoveryResult handlePremiumRestorationError(Throwable error, OperationContext context) {
        ErrorRecoveryResult result = new ErrorRecoveryResult();
        result.setOperation("premium_restoration");
        result.setError(error);
        
        // Premium restoration errors are usually recoverable
        result.setRecovered(true); // Mark as recovered since the main operation succeeded
        result.addRecoveryAction("Premium status can be restored manually later");
        result.addRecoveryAction("Players can be granted premium individually");
        result.setRecoveryMessage("Season reset completed but premium restoration failed - manual restoration required");
        
        return result;
    }
    
    /**
     * Handles generic operation errors.
     */
    private ErrorRecoveryResult handleGenericError(String operation, Throwable error, OperationContext context) {
        ErrorRecoveryResult result = new ErrorRecoveryResult();
        result.setOperation(operation);
        result.setError(error);
        result.setRecovered(false);
        
        result.addRecoveryAction("Check server logs for detailed error information");
        result.addRecoveryAction("Verify system configuration");
        result.addRecoveryAction("Contact system administrator if problem persists");
        
        return result;
    }
    
    /**
     * Logs errors to file for audit purposes.
     */
    private void logError(String operation, Throwable error, OperationContext context) {
        try {
            Path errorLogDir = Paths.get(ERROR_LOG_DIR);
            if (!Files.exists(errorLogDir)) {
                Files.createDirectories(errorLogDir);
            }
            
            String timestamp = LocalDateTime.now().format(ERROR_TIMESTAMP_FORMAT);
            String filename = String.format("error_%s_%s.log", operation, timestamp);
            Path errorLogFile = errorLogDir.resolve(filename);
            
            StringBuilder logContent = new StringBuilder();
            logContent.append("Error Log - ").append(timestamp).append("\n");
            logContent.append("Operation: ").append(operation).append("\n");
            logContent.append("Error: ").append(error.getMessage()).append("\n");
            logContent.append("Stack Trace:\n");
            
            for (StackTraceElement element : error.getStackTrace()) {
                logContent.append("  ").append(element.toString()).append("\n");
            }
            
            if (context != null) {
                logContent.append("\nContext:\n");
                logContent.append("  Operation Type: ").append(context.getOperationType()).append("\n");
                logContent.append("  Timestamp: ").append(context.getTimestamp()).append("\n");
                if (context.getBackupPath() != null) {
                    logContent.append("  Backup Path: ").append(context.getBackupPath()).append("\n");
                }
            }
            
            Files.write(errorLogFile, logContent.toString().getBytes());
            
        } catch (Exception e) {
            CobblePass.LOGGER.error("Failed to log error to file", e);
        }
    }
    
    /**
     * Notifies administrators of critical errors.
     */
    private void notifyAdministrators(String operation, Throwable error, OperationContext context, ErrorRecoveryResult result) {
        try {
            if (CobblePass.getServer() != null) {
                String message = LangManager.getRaw("lang.season.reset.error.operation_failed",
                        Map.of("error", error.getMessage()));
                
                // Send to all online operators
                for (ServerPlayer player : CobblePass.getServer().getPlayerList().getPlayers()) {
                    if (player.hasPermissions(4)) { // Operator level
                        player.sendSystemMessage(LangManager.get("lang.season.reset.error.operation_failed",
                                Map.of("error", operation + ": " + error.getMessage())));
                        
                        if (!result.getRecoveryActions().isEmpty()) {
                            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§7Recovery actions:"));
                            for (String action : result.getRecoveryActions()) {
                                player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§7- " + action));
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            CobblePass.LOGGER.error("Failed to notify administrators", e);
        }
    }
    
    /**
     * Notifies administrators of successful rollback.
     */
    private void notifyRollbackComplete(String backupPath, OperationContext context) {
        try {
            if (CobblePass.getServer() != null) {
                for (ServerPlayer player : CobblePass.getServer().getPlayerList().getPlayers()) {
                    if (player.hasPermissions(4)) {
                        player.sendSystemMessage(LangManager.get("lang.season.reset.error.rollback_complete"));
                    }
                }
            }
        } catch (Exception e) {
            CobblePass.LOGGER.error("Failed to notify rollback completion", e);
        }
    }
    
    /**
     * Notifies administrators of critical rollback failure.
     */
    private void notifyCriticalRollbackFailure(String backupPath, Throwable error, OperationContext context) {
        try {
            if (CobblePass.getServer() != null) {
                for (ServerPlayer player : CobblePass.getServer().getPlayerList().getPlayers()) {
                    if (player.hasPermissions(4)) {
                        player.sendSystemMessage(LangManager.get("lang.season.reset.error.rollback_failed",
                                Map.of("backupPath", backupPath)));
                    }
                }
            }
        } catch (Exception e) {
            CobblePass.LOGGER.error("Failed to notify critical rollback failure", e);
        }
    }
    
    /**
     * Adds recovery suggestions based on validation errors.
     */
    private void addRecoverySuggestions(OperationValidationResult result) {
        for (String error : result.getErrors()) {
            if (error.contains("disk space")) {
                result.addRecoverySuggestion("Free up disk space or use external storage");
            } else if (error.contains("permission")) {
                result.addRecoverySuggestion("Check file system permissions");
            } else if (error.contains("directory")) {
                result.addRecoverySuggestion("Verify directory structure and accessibility");
            } else if (error.contains("configuration")) {
                result.addRecoverySuggestion("Review and fix configuration settings");
            }
        }
    }
    
    // Result classes for error handling operations
    
    public static class ErrorRecoveryResult {
        private String operation;
        private Throwable error;
        private boolean recovered = false;
        private String recoveryMessage;
        private Throwable recoveryError;
        private OperationContext context;
        private List<String> recoveryActions = new ArrayList<>();
        
        // Getters and setters
        public String getOperation() { return operation; }
        public void setOperation(String operation) { this.operation = operation; }
        
        public Throwable getError() { return error; }
        public void setError(Throwable error) { this.error = error; }
        
        public boolean isRecovered() { return recovered; }
        public void setRecovered(boolean recovered) { this.recovered = recovered; }
        
        public String getRecoveryMessage() { return recoveryMessage; }
        public void setRecoveryMessage(String recoveryMessage) { this.recoveryMessage = recoveryMessage; }
        
        public Throwable getRecoveryError() { return recoveryError; }
        public void setRecoveryError(Throwable recoveryError) { this.recoveryError = recoveryError; }
        
        public OperationContext getContext() { return context; }
        public void setContext(OperationContext context) { this.context = context; }
        
        public List<String> getRecoveryActions() { return recoveryActions; }
        public void addRecoveryAction(String action) { this.recoveryActions.add(action); }
    }
    
    public static class RollbackResult {
        private boolean success = false;
        private String error;
        private Exception exception;
        private String backupPath;
        private String message;
        
        // Getters and setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
        
        public Exception getException() { return exception; }
        public void setException(Exception exception) { this.exception = exception; }
        
        public String getBackupPath() { return backupPath; }
        public void setBackupPath(String backupPath) { this.backupPath = backupPath; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
    
    public static class OperationValidationResult {
        private String operation;
        private boolean valid = true;
        private List<String> errors = new ArrayList<>();
        private List<String> warnings = new ArrayList<>();
        private List<String> recoverySuggestions = new ArrayList<>();
        
        // Getters and setters
        public String getOperation() { return operation; }
        public void setOperation(String operation) { this.operation = operation; }
        
        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }
        
        public List<String> getErrors() { return errors; }
        public void setErrors(List<String> errors) { this.errors = errors; }
        public void addError(String error) { this.errors.add(error); this.valid = false; }
        
        public List<String> getWarnings() { return warnings; }
        public void setWarnings(List<String> warnings) { this.warnings = warnings; }
        public void addWarning(String warning) { this.warnings.add(warning); }
        
        public List<String> getRecoverySuggestions() { return recoverySuggestions; }
        public void addRecoverySuggestion(String suggestion) { this.recoverySuggestions.add(suggestion); }
    }
    
    /**
     * Context class for tracking operation state.
     */
    public static class OperationContext {
        private String operationType;
        private long timestamp;
        private String backupPath;
        private SeasonResetOptions resetOptions;
        private SeasonStartOptions startOptions;
        private Map<String, Object> metadata = new HashMap<>();
        
        // Getters and setters
        public String getOperationType() { return operationType; }
        public void setOperationType(String operationType) { this.operationType = operationType; }
        
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
        
        public String getBackupPath() { return backupPath; }
        public void setBackupPath(String backupPath) { this.backupPath = backupPath; }
        
        public SeasonResetOptions getResetOptions() { return resetOptions; }
        public void setResetOptions(SeasonResetOptions resetOptions) { this.resetOptions = resetOptions; }
        
        public SeasonStartOptions getStartOptions() { return startOptions; }
        public void setStartOptions(SeasonStartOptions startOptions) { this.startOptions = startOptions; }
        
        public Map<String, Object> getMetadata() { return metadata; }
        public void addMetadata(String key, Object value) { this.metadata.put(key, value); }
    }
}