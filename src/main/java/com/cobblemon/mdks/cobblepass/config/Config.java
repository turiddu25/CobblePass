package com.cobblemon.mdks.cobblepass.config;

import com.cobblemon.mdks.cobblepass.CobblePass;
import com.cobblemon.mdks.cobblepass.util.Constants;
import com.cobblemon.mdks.cobblepass.util.LangManager;
import com.cobblemon.mdks.cobblepass.util.Utils;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Config {
    private int maxLevel;
    private int catchXP;
    private int defeatXP;
    private int evolveXP;
    private int hatchXP;
    private int tradeXP;
    private long premiumCost;
    private int seasonDurationDays;
    private int currentSeason;
    private long seasonStartTime;
    private long seasonEndTime;
    private boolean enablePermissionNodes;
    private XpProgression xpProgression;
    private GuiConfig guiConfig;

    private boolean premiumMode;

    public Config() {
        setDefaults();
    }

    private void setDefaults() {
        generateDefaultConfig();
    }

    private void generateDefaultConfig() {
        this.maxLevel = Constants.DEFAULT_MAX_LEVEL;
        this.catchXP = Constants.DEFAULT_CATCH_XP;
        this.defeatXP = Constants.DEFAULT_DEFEAT_XP;
        this.evolveXP = Constants.DEFAULT_EVOLVE_XP;
        this.hatchXP = Constants.DEFAULT_HATCH_XP;
        this.tradeXP = Constants.DEFAULT_TRADE_XP;
        this.premiumCost = Constants.DEFAULT_PREMIUM_COST;
        this.seasonDurationDays = 60;
        this.currentSeason = 0;
        this.seasonStartTime = 0;
        this.seasonEndTime = 0;
        this.enablePermissionNodes = Constants.DEFAULT_ENABLE_PERMISSION_NODES;
        this.xpProgression = new XpProgression();
        this.guiConfig = new GuiConfig();
        this.premiumMode = false;
    }

    public void load() {
        // Ensure config directory exists
        Utils.checkForDirectory("/" + Constants.CONFIG_PATH);
        
        // Always load GUI and language configurations first
        loadGuiConfig();
        loadLangConfig();
        
        String content = Utils.readFileSync(Constants.CONFIG_PATH, Constants.CONFIG_FILE);
        if (content == null || content.isEmpty()) {
            CobblePass.LOGGER.info("No main config file found, using defaults");
            return;
        }

        try {
            JsonObject json = JsonParser.parseString(content).getAsJsonObject();
            loadFromJson(json);
        } catch (Exception e) {
            CobblePass.LOGGER.error("Failed to load config", e);
        }
    }

    private void loadGuiConfig() {
        try {
            if (guiConfig == null) {
                guiConfig = new GuiConfig();
            }
            guiConfig.load();
            CobblePass.LOGGER.info("GUI configuration loaded successfully");
        } catch (Exception e) {
            CobblePass.LOGGER.error("Failed to load GUI configuration", e);
            guiConfig = new GuiConfig(); // Fallback to default
        }
    }

    private void loadLangConfig() {
        try {
            LangManager.load();
            CobblePass.LOGGER.info("Language configuration loaded successfully");
        } catch (Exception e) {
            CobblePass.LOGGER.error("Failed to load language configuration", e);
        }
    }

    private void loadFromJson(JsonObject json) {
        maxLevel = getOrDefault(json, "maxLevel", Constants.DEFAULT_MAX_LEVEL);
        catchXP = getOrDefault(json, "catchXP", Constants.DEFAULT_CATCH_XP);
        defeatXP = getOrDefault(json, "defeatXP", Constants.DEFAULT_DEFEAT_XP);
        evolveXP = getOrDefault(json, "evolveXP", Constants.DEFAULT_EVOLVE_XP);
        hatchXP = getOrDefault(json, "hatchXP", Constants.DEFAULT_HATCH_XP);
        tradeXP = getOrDefault(json, "tradeXP", Constants.DEFAULT_TRADE_XP);
        premiumCost = getOrDefault(json, "premiumCost", Constants.DEFAULT_PREMIUM_COST);
        seasonDurationDays = getOrDefault(json, "seasonDurationDays", 60);
        currentSeason = getOrDefault(json, "currentSeason", 0);
        seasonStartTime = getOrDefault(json, "seasonStartTime", 0L);
        seasonEndTime = getOrDefault(json, "seasonEndTime", 0L);
        enablePermissionNodes = getOrDefault(json, "enablePermissionNodes", Constants.DEFAULT_ENABLE_PERMISSION_NODES);
        premiumMode = getOrDefault(json, "premiumMode", false);

        if (json.has("xpProgression") && json.get("xpProgression").isJsonObject()) {
            this.xpProgression = new XpProgression();
            this.xpProgression.fromJson(json.getAsJsonObject("xpProgression"));
        } else {
            this.xpProgression = new XpProgression();
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
        // Ensure config directory exists
        Utils.checkForDirectory("/" + Constants.CONFIG_PATH);

        JsonObject json = new JsonObject();
        json.addProperty("maxLevel", maxLevel);
        json.addProperty("catchXP", catchXP);
        json.addProperty("defeatXP", defeatXP);
        json.addProperty("evolveXP", evolveXP);
        json.addProperty("hatchXP", hatchXP);
        json.addProperty("tradeXP", tradeXP);
        json.addProperty("premiumCost", premiumCost);
        json.addProperty("seasonDurationDays", seasonDurationDays);
        json.addProperty("currentSeason", currentSeason);
        json.addProperty("seasonStartTime", seasonStartTime);
        json.addProperty("seasonEndTime", seasonEndTime);
        json.addProperty("enablePermissionNodes", enablePermissionNodes);
        json.add("xpProgression", xpProgression.toJson());
        json.addProperty("premiumMode", premiumMode);

        Utils.writeFileSync(Constants.CONFIG_PATH, Constants.CONFIG_FILE,
                Utils.newGson().toJson(json));
    }

    // Getters
    public int getMaxLevel() { return maxLevel; }
    public int getCatchXP() { return catchXP; }
    public int getDefeatXP() { return defeatXP; }
    public int getEvolveXP() { return evolveXP; }
    public int getHatchXP() { return hatchXP; }
    public int getTradeXP() { return tradeXP; }
    public long getPremiumCost() { return premiumCost; }
    public int getCurrentSeason() { return currentSeason; }
    public boolean isEnablePermissionNodes() { return enablePermissionNodes; }
    public XpProgression getXpProgression() { return xpProgression; }
    public GuiConfig getGuiConfig() { return guiConfig; }
    public boolean isPremiumMode() { return premiumMode; }
    
    public void createNewSeason(int duration, int maxLevel, boolean premium) {
        currentSeason++;
        seasonDurationDays = duration;
        this.maxLevel = maxLevel;
        this.premiumMode = premium;
        seasonStartTime = 0;
        seasonEndTime = 0;
        save();
    }

    public void startNewSeason() {
        seasonStartTime = System.currentTimeMillis();
        seasonEndTime = seasonStartTime + (seasonDurationDays * Constants.MILLIS_PER_DAY);
        save();
    }

    public void stopSeason() {
        seasonStartTime = 0;
        seasonEndTime = 0;
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
