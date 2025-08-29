package com.cobblemon.mdks.cobblepass.season;

public enum PremiumPreservationMode {
    PRESERVE_ALL("preserve_all"),
    SYNC_PERMISSIONS("sync_permissions"),
    PRESERVE_AND_SYNC("preserve_and_sync"),
    NONE("none");

    private final String configValue;

    PremiumPreservationMode(String configValue) {
        this.configValue = configValue;
    }

    public String getConfigValue() {
        return configValue;
    }

    public static PremiumPreservationMode fromConfigValue(String value) {
        for (PremiumPreservationMode mode : values()) {
            if (mode.configValue.equalsIgnoreCase(value)) {
                return mode;
            }
        }
        return NONE;
    }
}