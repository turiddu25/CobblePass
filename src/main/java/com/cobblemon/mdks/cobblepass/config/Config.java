package com.cobblemon.mdks.cobblepass.config;

import com.cobblemon.mdks.cobblepass.CobblePass;
import com.cobblemon.mdks.cobblepass.util.Constants;
import com.cobblemon.mdks.cobblepass.util.Utils;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Config {
    private String version;
    private int maxLevel;
    private int xpPerLevel;
    private long premiumCost;
    private long premiumDuration;
    private boolean enablePermissionNodes;

    public Config() {
        setDefaults();
    }

    private void setDefaults() {
        this.version = Constants.CONFIG_VERSION;
        this.maxLevel = Constants.DEFAULT_MAX_LEVEL;
        this.xpPerLevel = Constants.DEFAULT_XP_PER_LEVEL;
        this.premiumCost = Constants.DEFAULT_PREMIUM_COST;
        this.premiumDuration = Constants.DEFAULT_PREMIUM_DURATION;
        this.enablePermissionNodes = Constants.DEFAULT_ENABLE_PERMISSION_NODES;
    }

    public void load() {
        Utils.readFileAsync(Constants.CONFIG_PATH, Constants.CONFIG_FILE, content -> {
            if (content == null || content.isEmpty()) {
                save();
                return;
            }

            try {
                JsonObject json = JsonParser.parseString(content).getAsJsonObject();
                loadFromJson(json);
            } catch (Exception e) {
                CobblePass.LOGGER.error("Failed to load config", e);
                setDefaults();
                save();
            }
        });
    }

    private void loadFromJson(JsonObject json) {
        version = getOrDefault(json, "version", Constants.CONFIG_VERSION);
        maxLevel = getOrDefault(json, "maxLevel", Constants.DEFAULT_MAX_LEVEL);
        xpPerLevel = getOrDefault(json, "xpPerLevel", Constants.DEFAULT_XP_PER_LEVEL);
        premiumCost = getOrDefault(json, "premiumCost", Constants.DEFAULT_PREMIUM_COST);
        premiumDuration = getOrDefault(json, "premiumDuration", Constants.DEFAULT_PREMIUM_DURATION);
        enablePermissionNodes = getOrDefault(json, "enablePermissionNodes", Constants.DEFAULT_ENABLE_PERMISSION_NODES);

        // Version check and upgrade if needed
        if (!version.equals(Constants.CONFIG_VERSION)) {
            CobblePass.LOGGER.info("Updating config from version " + version + " to " + Constants.CONFIG_VERSION);
            version = Constants.CONFIG_VERSION;
            save();
        }
    }

    private <T> T getOrDefault(JsonObject json, String key, T defaultValue) {
        if (!json.has(key)) return defaultValue;
        
        JsonElement element = json.get(key);
        if (defaultValue instanceof String) {
            return (T) element.getAsString();
        } else if (defaultValue instanceof Integer) {
            return (T) Integer.valueOf(element.getAsInt());
        } else if (defaultValue instanceof Long) {
            return (T) Long.valueOf(element.getAsLong());
        } else if (defaultValue instanceof Boolean) {
            return (T) Boolean.valueOf(element.getAsBoolean());
        }
        return defaultValue;
    }

    public void save() {
        JsonObject json = new JsonObject();
        json.addProperty("version", version);
        json.addProperty("maxLevel", maxLevel);
        json.addProperty("xpPerLevel", xpPerLevel);
        json.addProperty("premiumCost", premiumCost);
        json.addProperty("premiumDuration", premiumDuration);
        json.addProperty("enablePermissionNodes", enablePermissionNodes);

        Utils.writeFileAsync(Constants.CONFIG_PATH, Constants.CONFIG_FILE,
                Utils.newGson().toJson(json));
    }

    // Getters
    public String getVersion() { return version; }
    public int getMaxLevel() { return maxLevel; }
    public int getXpPerLevel() { return xpPerLevel; }
    public long getPremiumCost() { return premiumCost; }
    public long getPremiumDuration() { return premiumDuration; }
    public boolean isEnablePermissionNodes() { return enablePermissionNodes; }
}
