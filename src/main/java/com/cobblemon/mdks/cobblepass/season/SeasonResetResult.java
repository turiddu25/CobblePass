package com.cobblemon.mdks.cobblepass.season;

import java.util.ArrayList;
import java.util.List;

public class SeasonResetResult {
    private final boolean success;
    private final String message;
    private SeasonResetSummary summary;
    private List<String> warnings;
    private long operationDuration;
    private Exception exception;

    public SeasonResetResult(boolean success, String message) {
        this.success = success;
        this.message = message;
        this.warnings = new ArrayList<>();
        this.operationDuration = 0;
    }

    public static SeasonResetResult success(SeasonResetSummary summary) {
        SeasonResetResult result = new SeasonResetResult(true, "Season reset completed successfully");
        result.setSummary(summary);
        return result;
    }

    public static SeasonResetResult failure(String errorMessage) {
        return new SeasonResetResult(false, errorMessage);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getErrorMessage() {
        return message;
    }

    public SeasonResetSummary getSummary() {
        return summary != null ? summary : new SeasonResetSummary(0, 0, "");
    }

    public void setSummary(SeasonResetSummary summary) {
        this.summary = summary;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings != null ? warnings : new ArrayList<>();
    }

    public void addWarning(String warning) {
        if (warnings == null) {
            warnings = new ArrayList<>();
        }
        warnings.add(warning);
    }

    public boolean hasWarnings() {
        return warnings != null && !warnings.isEmpty();
    }

    public long getOperationDuration() {
        return operationDuration;
    }

    public void setOperationDuration(long operationDuration) {
        this.operationDuration = operationDuration;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }
}