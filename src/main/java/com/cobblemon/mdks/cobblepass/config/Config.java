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
    private int catchXP;
    private int defeatXP;
    private long premiumCost;
    private int seasonDurationDays;
    private long seasonStartTime;
    private int currentSeason;
    private boolean enablePermissionNodes;

    public Config() {
        setDefaults();
    }

    private void setDefaults() {
        this.version = Constants.CONFIG_VERSION;
        this.maxLevel = Constants.DEFAULT_MAX_LEVEL;
        this.xpPerLevel = Constants.DEFAULT_XP_PER_LEVEL;
        this.catchXP = Constants.DEFAULT_CATCH_XP;
        this.defeatXP = Constants.DEFAULT_DEFEAT_XP;
        this.premiumCost = Constants.DEFAULT_PREMIUM_COST;
        this.seasonDurationDays = 60; // Default season duration
        this.seasonStartTime = -1L; // -1 means no active season
        this.currentSeason = 0; // 0 means no season has started
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
        seasonDurationDays = getOrDefault(json, "seasonDurationDays", 60);
        seasonStartTime = getOrDefault(json, "seasonStartTime", -1L);
        currentSeason = getOrDefault(json, "currentSeason", 0);
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
        json.addProperty("catchXP", catchXP);
        json.addProperty("defeatXP", defeatXP);
        json.addProperty("premiumCost", premiumCost);
        json.addProperty("seasonDurationDays", seasonDurationDays);
        json.addProperty("seasonStartTime", seasonStartTime);
        json.addProperty("currentSeason", currentSeason);
        json.addProperty("enablePermissionNodes", enablePermissionNodes);

        Utils.writeFileAsync(Constants.CONFIG_PATH, Constants.CONFIG_FILE,
                Utils.newGson().toJson(json));
    }

    // Getters
    public String getVersion() { return version; }
    public int getMaxLevel() { return maxLevel; }
    public int getXpPerLevel() { return xpPerLevel; }
    public int getCatchXP() { return catchXP; }
    public int getDefeatXP() { return defeatXP; }
    public long getPremiumCost() { return premiumCost; }
    public long getSeasonStartTime() { return seasonStartTime; }
    public int getCurrentSeason() { return currentSeason; }
    public long getSeasonEndTime() { 
        return seasonStartTime + (Constants.MILLIS_PER_DAY * seasonDurationDays); 
    }
    public boolean isSeasonActive() { return seasonStartTime > 0 && System.currentTimeMillis() < getSeasonEndTime(); }
    
    public void startNewSeason() {
        currentSeason++;
        seasonStartTime = System.currentTimeMillis();
        save();
    }
    public boolean isEnablePermissionNodes() { return enablePermissionNodes; }
}
