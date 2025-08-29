package com.cobblemon.mdks.cobblepass.config;

import com.cobblemon.mdks.cobblepass.CobblePass;
import com.cobblemon.mdks.cobblepass.premium.PremiumMode;
import com.cobblemon.mdks.cobblepass.season.SeasonResetConfig;
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
    private int fishXP;
    private int catchLegendaryXP;
    private int catchShinyXP;
    private int catchUltraBeastXP;
    private int catchMythicalXP;
    private int catchParadoxXP;
    private int releaseXP;
    private int seasonDurationDays;
    private int currentSeason;
    private long seasonStartTime;
    private long seasonEndTime;
    private boolean enablePermissionNodes;
    private XpProgression xpProgression;
    private GuiConfig guiConfig;
    private PremiumConfig premiumConfig;
    private SeasonResetConfig seasonResetConfig;

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
        this.fishXP = Constants.DEFAULT_FISH_XP;
        this.catchLegendaryXP = Constants.DEFAULT_CATCH_LEGENDARY_XP;
        this.catchShinyXP = Constants.DEFAULT_CATCH_SHINY_XP;
        this.catchUltraBeastXP = Constants.DEFAULT_CATCH_ULTRABEAST_XP;
        this.catchMythicalXP = Constants.DEFAULT_CATCH_MYTHICAL_XP;
        this.catchParadoxXP = Constants.DEFAULT_CATCH_PARADOX_XP;
        this.releaseXP = Constants.DEFAULT_RELEASE_XP;
        this.seasonDurationDays = 60;
        this.currentSeason = 0;
        this.seasonStartTime = 0;
        this.seasonEndTime = 0;
        this.enablePermissionNodes = Constants.DEFAULT_ENABLE_PERMISSION_NODES;
        this.xpProgression = new XpProgression();
        this.guiConfig = new GuiConfig();
        this.premiumConfig = new PremiumConfig();
        this.seasonResetConfig = new SeasonResetConfig();
    }

    public void load() {
        // Ensure config directory exists
        Utils.checkForDirectory("/" + Constants.CONFIG_PATH);
        
        // Always load GUI and language configurations first
        loadGuiConfig();
        loadLangConfig();
        
        String content = Utils.readFileSync(Constants.CONFIG_PATH, Constants.CONFIG_FILE);
        if (content == null || content.isEmpty()) {
            CobblePass.LOGGER.info("No main config file found, generating default configuration");
            save();
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
        fishXP = getOrDefault(json, "fishXP", Constants.DEFAULT_FISH_XP);
        catchLegendaryXP = getOrDefault(json, "catchLegendaryXP", Constants.DEFAULT_CATCH_LEGENDARY_XP);
        catchShinyXP = getOrDefault(json, "catchShinyXP", Constants.DEFAULT_CATCH_SHINY_XP);
        catchUltraBeastXP = getOrDefault(json, "catchUltraBeastXP", Constants.DEFAULT_CATCH_ULTRABEAST_XP);
        catchMythicalXP = getOrDefault(json, "catchMythicalXP", Constants.DEFAULT_CATCH_MYTHICAL_XP);
        catchParadoxXP = getOrDefault(json, "catchParadoxXP", Constants.DEFAULT_CATCH_PARADOX_XP);
        releaseXP = getOrDefault(json, "releaseXP", Constants.DEFAULT_RELEASE_XP);
        seasonDurationDays = getOrDefault(json, "seasonDurationDays", 60);
        currentSeason = getOrDefault(json, "currentSeason", 0);
        seasonStartTime = getOrDefault(json, "seasonStartTime", 0L);
        seasonEndTime = getOrDefault(json, "seasonEndTime", 0L);
        enablePermissionNodes = getOrDefault(json, "enablePermissionNodes", Constants.DEFAULT_ENABLE_PERMISSION_NODES);

        if (json.has("xpProgression") && json.get("xpProgression").isJsonObject()) {
            this.xpProgression = new XpProgression();
            this.xpProgression.fromJson(json.getAsJsonObject("xpProgression"));
        } else {
            this.xpProgression = new XpProgression();
        }

        // Load premium configuration with backward compatibility
        if (json.has("premiumConfig") && json.get("premiumConfig").isJsonObject()) {
            this.premiumConfig = new PremiumConfig();
            this.premiumConfig.fromJson(json.getAsJsonObject("premiumConfig"));
        } else {
            // Handle backward compatibility with old config format
            this.premiumConfig = new PremiumConfig();
            if (json.has("premiumCost")) {
                this.premiumConfig.setPremiumCost(json.get("premiumCost").getAsLong());
            }
            if (json.has("premiumMode")) {
                // Convert old boolean premiumMode to new enum system
                boolean oldPremiumMode = json.get("premiumMode").getAsBoolean();
                this.premiumConfig.setMode(oldPremiumMode ? PremiumMode.ECONOMY : PremiumMode.DISABLED);
            }
        }

        // Load season reset configuration
        if (json.has("seasonResetConfig") && json.get("seasonResetConfig").isJsonObject()) {
            this.seasonResetConfig = new SeasonResetConfig();
            this.seasonResetConfig.fromJson(json.getAsJsonObject("seasonResetConfig"));
        } else {
            this.seasonResetConfig = new SeasonResetConfig();
        }

        // Validate season reset configuration
        if (!this.seasonResetConfig.validate()) {
            CobblePass.LOGGER.warn("Season reset configuration had invalid values that were corrected");
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
        json.addProperty("fishXP", fishXP);
        json.addProperty("catchLegendaryXP", catchLegendaryXP);
        json.addProperty("catchShinyXP", catchShinyXP);
        json.addProperty("catchUltraBeastXP", catchUltraBeastXP);
        json.addProperty("catchMythicalXP", catchMythicalXP);
        json.addProperty("catchParadoxXP", catchParadoxXP);
        json.addProperty("releaseXP", releaseXP);
        json.addProperty("seasonDurationDays", seasonDurationDays);
        json.addProperty("currentSeason", currentSeason);
        json.addProperty("seasonStartTime", seasonStartTime);
        json.addProperty("seasonEndTime", seasonEndTime);
        json.addProperty("enablePermissionNodes", enablePermissionNodes);
        json.add("xpProgression", xpProgression.toJson());
        json.add("premiumConfig", premiumConfig.toJson());
        json.add("seasonResetConfig", seasonResetConfig.toJson());

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
    public int getFishXP() { return fishXP; }
    public int getCatchLegendaryXP() { return catchLegendaryXP; }
    public int getCatchShinyXP() { return catchShinyXP; }
    public int getCatchUltraBeastXP() { return catchUltraBeastXP; }
    public int getCatchMythicalXP() { return catchMythicalXP; }
    public int getCatchParadoxXP() { return catchParadoxXP; }
    public int getReleaseXP() { return releaseXP; }
    public int getCurrentSeason() { return currentSeason; }
    public boolean isEnablePermissionNodes() { return enablePermissionNodes; }
    public XpProgression getXpProgression() { return xpProgression; }
    public GuiConfig getGuiConfig() { return guiConfig; }
    public PremiumConfig getPremiumConfig() { return premiumConfig; }
    public SeasonResetConfig getSeasonResetConfig() { return seasonResetConfig; }
    
    // Backward compatibility getters
    @Deprecated
    public long getPremiumCost() { return premiumConfig.getPremiumCost(); }
    
    @Deprecated
    public boolean isPremiumMode() { return premiumConfig.getMode() != PremiumMode.DISABLED; }
    
    public void createNewSeason(int duration, int maxLevel, boolean premium) {
        currentSeason++;
        seasonDurationDays = duration;
        this.maxLevel = maxLevel;
        // Convert boolean premium to new premium mode system
        this.premiumConfig.setMode(premium ? PremiumMode.ECONOMY : PremiumMode.DISABLED);
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
    public java.util.Map<String, Integer> getXpSources() {
        java.util.Map<String, Integer> xpSources = new java.util.HashMap<>();
        xpSources.put("catch", catchXP);
        xpSources.put("defeat", defeatXP);
        xpSources.put("evolve", evolveXP);
        xpSources.put("hatch", hatchXP);
        xpSources.put("trade", tradeXP);
        xpSources.put("fish", fishXP);
        xpSources.put("catch_legendary", catchLegendaryXP);
        xpSources.put("catch_shiny", catchShinyXP);
        xpSources.put("catch_ultrabeast", catchUltraBeastXP);
        xpSources.put("catch_mythical", catchMythicalXP);
        xpSources.put("catch_paradox", catchParadoxXP);
        xpSources.put("release", releaseXP);
        return xpSources;
    }
}
