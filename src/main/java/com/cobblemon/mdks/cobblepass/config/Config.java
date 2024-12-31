package com.cobblemon.mdks.cobblepass.config;

import com.cobblemon.mdks.cobblepass.CobblePass;
import com.cobblemon.mdks.cobblepass.util.Constants;
import com.cobblemon.mdks.cobblepass.util.Utils;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Config {
    private int maxLevel;
    private int xpPerLevel;
    private int catchXP;
    private int defeatXP;
    private long premiumCost;
    private int seasonDurationDays;
    private int currentSeason;
    private long seasonStartTime;
    private long seasonEndTime;
    private boolean enablePermissionNodes;

    public Config() {
        setDefaults();
    }

    private void setDefaults() {
        generateDefaultConfig();
    }

    private void generateDefaultConfig() {
        this.maxLevel = Constants.DEFAULT_MAX_LEVEL;
        this.xpPerLevel = Constants.DEFAULT_XP_PER_LEVEL;
        this.catchXP = Constants.DEFAULT_CATCH_XP;
        this.defeatXP = Constants.DEFAULT_DEFEAT_XP;
        this.premiumCost = Constants.DEFAULT_PREMIUM_COST;
        this.seasonDurationDays = 60;
        this.currentSeason = 0;
        this.seasonStartTime = 0;
        this.seasonEndTime = 0;
        this.enablePermissionNodes = Constants.DEFAULT_ENABLE_PERMISSION_NODES;
    }

    public void load() {
        // Ensure config directory exists
        Utils.checkForDirectory("/" + Constants.CONFIG_PATH);
        
        String content = Utils.readFileSync(Constants.CONFIG_PATH, Constants.CONFIG_FILE);
        if (content == null || content.isEmpty()) {
            generateDefaultConfig();
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
    }

    private void loadFromJson(JsonObject json) {
        maxLevel = getOrDefault(json, "maxLevel", Constants.DEFAULT_MAX_LEVEL);
        xpPerLevel = getOrDefault(json, "xpPerLevel", Constants.DEFAULT_XP_PER_LEVEL);
        premiumCost = getOrDefault(json, "premiumCost", Constants.DEFAULT_PREMIUM_COST);
        seasonDurationDays = getOrDefault(json, "seasonDurationDays", 60);
        currentSeason = getOrDefault(json, "currentSeason", 0);
        seasonStartTime = getOrDefault(json, "seasonStartTime", 0L);
        seasonEndTime = getOrDefault(json, "seasonEndTime", 0L);
        enablePermissionNodes = getOrDefault(json, "enablePermissionNodes", Constants.DEFAULT_ENABLE_PERMISSION_NODES);
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
        // Ensure config directory exists
        Utils.checkForDirectory("/" + Constants.CONFIG_PATH);

        JsonObject json = new JsonObject();
        json.addProperty("maxLevel", maxLevel);
        json.addProperty("xpPerLevel", xpPerLevel);
        json.addProperty("catchXP", catchXP);
        json.addProperty("defeatXP", defeatXP);
        json.addProperty("premiumCost", premiumCost);
        json.addProperty("seasonDurationDays", seasonDurationDays);
        json.addProperty("currentSeason", currentSeason);
        json.addProperty("seasonStartTime", seasonStartTime);
        json.addProperty("seasonEndTime", seasonEndTime);
        json.addProperty("enablePermissionNodes", enablePermissionNodes);

        Utils.writeFileSync(Constants.CONFIG_PATH, Constants.CONFIG_FILE,
                Utils.newGson().toJson(json));
    }

    // Getters
    public int getMaxLevel() { return maxLevel; }
    public int getXpPerLevel() { return xpPerLevel; }
    public int getCatchXP() { return catchXP; }
    public int getDefeatXP() { return defeatXP; }
    public long getPremiumCost() { return premiumCost; }
    public int getCurrentSeason() { return currentSeason; }
    public boolean isEnablePermissionNodes() { return enablePermissionNodes; }
    
    public void startNewSeason() {
        currentSeason++;
        seasonStartTime = System.currentTimeMillis();
        seasonEndTime = seasonStartTime + (seasonDurationDays * Constants.MILLIS_PER_DAY);
        save();
    }

    public boolean isSeasonActive() {
        return seasonStartTime > 0 && System.currentTimeMillis() < seasonEndTime;
    }

    public long getSeasonEndTime() {
        return seasonEndTime;
    }

    public long getSeasonStartTime() {
        return seasonStartTime;
    }
}
