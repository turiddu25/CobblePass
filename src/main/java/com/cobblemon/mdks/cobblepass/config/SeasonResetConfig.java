package com.cobblemon.mdks.cobblepass.config;

import com.cobblemon.mdks.cobblepass.season.PremiumPreservationMode;
import com.google.gson.JsonObject;

/**
 * Configuration class for season reset functionality.
 * Contains settings that control how season resets are performed,
 * including backup behavior, premium preservation, and messaging options.
 */
public class SeasonResetConfig {
    private boolean autoBackupOnReset;
    private PremiumPreservationMode defaultPreservationMode;
    private boolean broadcastTransitionMessages;
    private int backupRetentionDays;
    private boolean requireConfirmation;
    private boolean validateBeforeReset;

    /**
     * Default constructor that initializes with safe default values.
     */
    public SeasonResetConfig() {
        setDefaults();
    }

    /**
     * Sets default values for all configuration options.
     * These defaults prioritize safety and user experience.
     */
    private void setDefaults() {
        this.autoBackupOnReset = true;
        this.defaultPreservationMode = PremiumPreservationMode.PRESERVE_ALL;
        this.broadcastTransitionMessages = true;
        this.backupRetentionDays = 30;
        this.requireConfirmation = true;
        this.validateBeforeReset = true;
    }

    /**
     * Loads configuration from JSON object.
     * Missing values will retain their default values.
     *
     * @param json JSON object containing configuration data
     */
    public void fromJson(JsonObject json) {
        if (json.has("autoBackupOnReset")) {
            this.autoBackupOnReset = json.get("autoBackupOnReset").getAsBoolean();
        }
        
        if (json.has("defaultPreservationMode")) {
            try {
                String modeStr = json.get("defaultPreservationMode").getAsString();
                this.defaultPreservationMode = PremiumPreservationMode.valueOf(modeStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Invalid mode, keep default
                this.defaultPreservationMode = PremiumPreservationMode.PRESERVE_ALL;
            }
        }
        
        if (json.has("broadcastTransitionMessages")) {
            this.broadcastTransitionMessages = json.get("broadcastTransitionMessages").getAsBoolean();
        }
        
        if (json.has("backupRetentionDays")) {
            int days = json.get("backupRetentionDays").getAsInt();
            this.backupRetentionDays = Math.max(1, days); // Ensure at least 1 day
        }
        
        if (json.has("requireConfirmation")) {
            this.requireConfirmation = json.get("requireConfirmation").getAsBoolean();
        }
        
        if (json.has("validateBeforeReset")) {
            this.validateBeforeReset = json.get("validateBeforeReset").getAsBoolean();
        }
    }

    /**
     * Converts configuration to JSON object for serialization.
     *
     * @return JsonObject containing all configuration values
     */
    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("autoBackupOnReset", autoBackupOnReset);
        json.addProperty("defaultPreservationMode", defaultPreservationMode.name());
        json.addProperty("broadcastTransitionMessages", broadcastTransitionMessages);
        json.addProperty("backupRetentionDays", backupRetentionDays);
        json.addProperty("requireConfirmation", requireConfirmation);
        json.addProperty("validateBeforeReset", validateBeforeReset);
        return json;
    }

    /**
     * Validates the current configuration settings.
     * Checks for invalid values and corrects them if necessary.
     *
     * @return true if configuration is valid, false if corrections were made
     */
    public boolean validate() {
        boolean isValid = true;
        
        // Ensure backup retention is at least 1 day
        if (backupRetentionDays < 1) {
            backupRetentionDays = 1;
            isValid = false;
        }
        
        // Ensure backup retention is not excessive (max 365 days)
        if (backupRetentionDays > 365) {
            backupRetentionDays = 365;
            isValid = false;
        }
        
        // Ensure preservation mode is not null
        if (defaultPreservationMode == null) {
            defaultPreservationMode = PremiumPreservationMode.PRESERVE_ALL;
            isValid = false;
        }
        
        return isValid;
    }

    // Getters
    public boolean isAutoBackupOnReset() {
        return autoBackupOnReset;
    }

    public PremiumPreservationMode getDefaultPreservationMode() {
        return defaultPreservationMode;
    }

    public boolean isBroadcastTransitionMessages() {
        return broadcastTransitionMessages;
    }

    public int getBackupRetentionDays() {
        return backupRetentionDays;
    }

    public boolean isRequireConfirmation() {
        return requireConfirmation;
    }

    public boolean isValidateBeforeReset() {
        return validateBeforeReset;
    }

    // Setters
    public void setAutoBackupOnReset(boolean autoBackupOnReset) {
        this.autoBackupOnReset = autoBackupOnReset;
    }

    public void setDefaultPreservationMode(PremiumPreservationMode defaultPreservationMode) {
        this.defaultPreservationMode = defaultPreservationMode;
    }

    public void setBroadcastTransitionMessages(boolean broadcastTransitionMessages) {
        this.broadcastTransitionMessages = broadcastTransitionMessages;
    }

    public void setBackupRetentionDays(int backupRetentionDays) {
        this.backupRetentionDays = Math.max(1, Math.min(365, backupRetentionDays));
    }

    public void setRequireConfirmation(boolean requireConfirmation) {
        this.requireConfirmation = requireConfirmation;
    }

    public void setValidateBeforeReset(boolean validateBeforeReset) {
        this.validateBeforeReset = validateBeforeReset;
    }
}