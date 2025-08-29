package com.cobblemon.mdks.cobblepass.season;

import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates the outcome of a season start operation.
 * Contains success/failure status, season information, and operation details.
 */
public class SeasonStartResult {
    private boolean success;
    private String errorMessage;
    private String seasonId;
    private String seasonName;
    private int premiumPlayersRestored;
    private long operationDuration;
    private List<String> warnings;
    private Exception exception;
    
    public SeasonStartResult() {
        this.success = false;
        this.errorMessage = "";
        this.seasonId = "";
        this.seasonName = "";
        this.premiumPlayersRestored = 0;
        this.operationDuration = 0;
        this.warnings = new ArrayList<>();
        this.exception = null;
    }
    
    public SeasonStartResult(boolean success) {
        this();
        this.success = success;
    }
    
    public SeasonStartResult(boolean success, String errorMessage) {
        this(success);
        this.errorMessage = errorMessage;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public String getSeasonId() {
        return seasonId;
    }
    
    public void setSeasonId(String seasonId) {
        this.seasonId = seasonId;
    }
    
    public String getSeasonName() {
        return seasonName;
    }
    
    public void setSeasonName(String seasonName) {
        this.seasonName = seasonName;
    }
    
    public int getPremiumPlayersRestored() {
        return premiumPlayersRestored;
    }
    
    public void setPremiumPlayersRestored(int premiumPlayersRestored) {
        this.premiumPlayersRestored = premiumPlayersRestored;
    }
    
    public long getOperationDuration() {
        return operationDuration;
    }
    
    public void setOperationDuration(long operationDuration) {
        this.operationDuration = operationDuration;
    }
    
    public List<String> getWarnings() {
        return warnings;
    }
    
    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }
    
    public void addWarning(String warning) {
        this.warnings.add(warning);
    }
    
    public Exception getException() {
        return exception;
    }
    
    public void setException(Exception exception) {
        this.exception = exception;
    }
    
    /**
     * Create a successful result.
     */
    public static SeasonStartResult success() {
        return new SeasonStartResult(true);
    }
    
    /**
     * Create a successful result with season information.
     */
    public static SeasonStartResult success(String seasonId, String seasonName) {
        SeasonStartResult result = new SeasonStartResult(true);
        result.setSeasonId(seasonId);
        result.setSeasonName(seasonName);
        return result;
    }
    
    /**
     * Create a failed result with error message.
     */
    public static SeasonStartResult failure(String errorMessage) {
        return new SeasonStartResult(false, errorMessage);
    }
    
    /**
     * Create a failed result with exception.
     */
    public static SeasonStartResult failure(String errorMessage, Exception exception) {
        SeasonStartResult result = new SeasonStartResult(false, errorMessage);
        result.setException(exception);
        return result;
    }
    
    /**
     * Check if the operation has any warnings.
     */
    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }
    
    /**
     * Get a formatted string representation of the result.
     */
    public String getFormattedResult() {
        StringBuilder result = new StringBuilder();
        
        if (success) {
            result.append("Season start completed successfully");
            if (operationDuration > 0) {
                result.append(" in ").append(operationDuration).append("ms");
            }
            result.append("\n");
            
            if (seasonName != null && !seasonName.isEmpty()) {
                result.append("Season Name: ").append(seasonName).append("\n");
            }
            if (seasonId != null && !seasonId.isEmpty()) {
                result.append("Season ID: ").append(seasonId).append("\n");
            }
            if (premiumPlayersRestored > 0) {
                result.append("Premium Players Restored: ").append(premiumPlayersRestored).append("\n");
            }
        } else {
            result.append("Season start failed: ").append(errorMessage);
            if (exception != null) {
                result.append("\nException: ").append(exception.getMessage());
            }
        }
        
        if (hasWarnings()) {
            result.append("\n\nWarnings:\n");
            for (String warning : warnings) {
                result.append("- ").append(warning).append("\n");
            }
        }
        
        return result.toString();
    }
    
    @Override
    public String toString() {
        return "SeasonStartResult{" +
                "success=" + success +
                ", errorMessage='" + errorMessage + '\'' +
                ", seasonId='" + seasonId + '\'' +
                ", seasonName='" + seasonName + '\'' +
                ", premiumPlayersRestored=" + premiumPlayersRestored +
                ", operationDuration=" + operationDuration +
                ", warnings=" + warnings.size() +
                ", hasException=" + (exception != null) +
                '}';
    }
}