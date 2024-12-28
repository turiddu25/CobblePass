package com.cobblemon.mdks.cobblepass.data;

public enum RewardType {
    MINECRAFT_ITEM,    // Regular Minecraft items
    COBBLEMON_ITEM,    // Cobblemon-specific items
    POKEMON,           // Pokemon rewards
    COMMAND;           // Custom command execution

    public static RewardType fromString(String type) {
        try {
            return valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return MINECRAFT_ITEM; // Default to minecraft item for backwards compatibility
        }
    }
}
