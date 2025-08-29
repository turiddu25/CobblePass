package com.cobblemon.mdks.cobblepass.premium;

/**
 * Enum representing different premium access modes for the battle pass system.
 */
public enum PremiumMode {
    /**
     * Players purchase premium access with in-game currency through the economy system.
     */
    ECONOMY("economy", "Players purchase premium with in-game currency"),
    
    /**
     * Players get premium access through permission nodes assigned by administrators.
     */
    PERMISSION("permission", "Players get premium through permission nodes"),
    
    /**
     * All players have premium access without any restrictions or requirements.
     */
    DISABLED("disabled", "All players have premium access");

    private final String configValue;
    private final String description;

    PremiumMode(String configValue, String description) {
        this.configValue = configValue;
        this.description = description;
    }

    /**
     * Gets the configuration value for this premium mode.
     * @return The string value used in configuration files
     */
    public String getConfigValue() {
        return configValue;
    }

    /**
     * Gets a human-readable description of this premium mode.
     * @return The description of what this mode does
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets a PremiumMode from its configuration value.
     * @param configValue The configuration string value
     * @return The corresponding PremiumMode, or ECONOMY as default
     */
    public static PremiumMode fromConfigValue(String configValue) {
        if (configValue == null) {
            return ECONOMY;
        }
        
        for (PremiumMode mode : values()) {
            if (mode.configValue.equalsIgnoreCase(configValue)) {
                return mode;
            }
        }
        
        return ECONOMY; // Default fallback
    }

    @Override
    public String toString() {
        return configValue;
    }
}