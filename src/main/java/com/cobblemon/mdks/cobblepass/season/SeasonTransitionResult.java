package com.cobblemon.mdks.cobblepass.season;

public class SeasonTransitionResult {
    private final boolean success;
    private final String message;

    public SeasonTransitionResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}