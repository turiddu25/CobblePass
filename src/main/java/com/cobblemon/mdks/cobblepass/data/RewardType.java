package com.cobblemon.mdks.cobblepass.data;

public enum RewardType {
    ITEM,             // Any mod's items (minecraft:, cobblemon:, etc)
    POKEMON,          // Pokemon rewards
    COMMAND;          // Custom command execution

    public static RewardType fromString(String type) {
        try {
            return valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            // Convert old types to new ITEM type for backwards compatibility
            if (type.equalsIgnoreCase("MINECRAFT_ITEM") || 
                type.equalsIgnoreCase("COBBLEMON_ITEM")) {
                return ITEM;
            }
            return ITEM; // Default to ITEM type
        }
    }
}
