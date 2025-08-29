package com.cobblemon.mdks.cobblepass.config;

import java.util.HashMap;
import java.util.Map;

import com.cobblemon.mdks.cobblepass.CobblePass;
import com.cobblemon.mdks.cobblepass.battlepass.BattlePassTier;
import com.cobblemon.mdks.cobblepass.data.Reward;
import com.cobblemon.mdks.cobblepass.util.Constants;
import com.cobblemon.mdks.cobblepass.util.Utils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class TierConfig {
    private static final String TIERS_FILE = Constants.TIERS_FILE;
    private static final String TIERS_PATH = Constants.CONFIG_DIR;
    
    private final Map<Integer, BattlePassTier> tiers = new HashMap<>();
    private final Map<String, JsonObject> templates = new HashMap<>();
    private boolean loaded = false;

    public TierConfig() {
        // Do not load here
    }

    public void load() {
        CobblePass.LOGGER.info("Loading tier configuration from tiers.json...");
        String content = Utils.readFileSync(TIERS_PATH, TIERS_FILE);
        if (content == null || content.isEmpty()) {
            CobblePass.LOGGER.warn("tiers.json not found or is empty. Generating default tiers.");
            generateDefaultTiers();
            loaded = true;
            return;
        }

        try {
            JsonObject json = JsonParser.parseString(content).getAsJsonObject();
            loadFromJson(json);
            CobblePass.LOGGER.info("Successfully loaded " + tiers.size() + " tiers from tiers.json.");
            loaded = true;
        } catch (Exception e) {
            CobblePass.LOGGER.error("Failed to parse tiers.json. The file may be corrupted or contain invalid data. The battle pass will not be loaded.", e);
            tiers.clear(); // Ensure tiers are empty on failure
            templates.clear(); // Ensure templates are empty on failure
            loaded = false;
        }
    }

    private void loadFromJson(JsonObject json) {
        tiers.clear();
        templates.clear();

        if (json.has("templates") && json.get("templates").isJsonObject()) {
            JsonObject templatesObj = json.getAsJsonObject("templates");
            for (Map.Entry<String, JsonElement> entry : templatesObj.entrySet()) {
                templates.put(entry.getKey(), entry.getValue().getAsJsonObject());
            }
        }

        if (json.has("tiers") && json.get("tiers").isJsonArray()) {
            JsonArray tiersArray = json.getAsJsonArray("tiers");
            for (JsonElement element : tiersArray) {
                JsonObject tierObj = element.getAsJsonObject();
                int level = tierObj.get("level").getAsInt();
                
                Reward freeReward = loadReward(tierObj, "freeReward", this.templates);
                Reward premiumReward = loadReward(tierObj, "premiumReward", this.templates);
                
                tiers.put(level, new BattlePassTier(level, freeReward, premiumReward));
            }
        }
    }

    private Reward loadReward(JsonObject tierObj, String rewardKey, Map<String, JsonObject> currentTemplates) {
        if (!tierObj.has(rewardKey)) {
            return null;
        }

        JsonElement rewardElement = tierObj.get(rewardKey);

        if (rewardElement.isJsonObject()) {
            JsonObject rewardObject = rewardElement.getAsJsonObject();
            if (rewardObject.has("$template")) {
                String templateName = rewardObject.get("$template").getAsString();
                if (currentTemplates.containsKey(templateName)) {
                    return Reward.fromJson(currentTemplates.get(templateName));
                }
                return null;
            }
            return Reward.fromJson(rewardObject);
        }

        return null;
    }

    private void generateDefaultTiers() {
        JsonObject defaultTiersJson = new JsonObject();
        JsonObject templatesJson = new JsonObject();
        
        JsonObject pokeBallTemplate = new JsonObject();
        pokeBallTemplate.addProperty("type", "ITEM");
        pokeBallTemplate.add("data", new com.google.gson.Gson().fromJson("{\"id\":\"cobblemon:poke_ball\",\"Count\":10}", JsonObject.class));
        templatesJson.add("pokeballs_10", pokeBallTemplate);

        defaultTiersJson.add("templates", templatesJson);

        JsonArray tiersArray = new JsonArray();
        for (int i = 1; i <= 10; i++) {
            JsonObject tier = new JsonObject();
            tier.addProperty("level", i);
            JsonObject reward = new JsonObject();
            reward.addProperty("$template", "pokeballs_10");
            
            if (i % 2 == 0) {
                tier.add("freeReward", reward);
            } else {
                tier.add("premiumReward", reward);
            }
            tiersArray.add(tier);
        }
        defaultTiersJson.add("tiers", tiersArray);

        Utils.writeFileSync(TIERS_PATH, TIERS_FILE, Utils.newGson().toJson(defaultTiersJson));
        
        loadFromJson(defaultTiersJson);
    }

    public void save() {
        // ADDED: Safety check to prevent wiping the file
        if (!loaded && !tiers.isEmpty()) {
             // This case can happen if a reload fails after a successful initial load.
             // We still want to save the in-memory data to prevent data loss from playtime.
        } else if (!loaded || tiers.isEmpty()) {
            CobblePass.LOGGER.warn("Tier configuration was not loaded correctly or is empty. Skipping save to prevent wiping tiers.json.");
            return;
        }
        
        JsonObject json = new JsonObject();
        
        JsonObject templatesObj = new JsonObject();
        for (Map.Entry<String, JsonObject> entry : this.templates.entrySet()) {
            templatesObj.add(entry.getKey(), entry.getValue());
        }
        json.add("templates", templatesObj);

        JsonArray tiersArray = new JsonArray();
        // Sort by level to ensure consistent file output
        tiers.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(entry -> {
                BattlePassTier tier = entry.getValue();
                JsonObject tierObj = new JsonObject();
                tierObj.addProperty("level", tier.getLevel());

                if (tier.getFreeReward() != null) {
                    tierObj.add("freeReward", tier.getFreeReward().toJson());
                }

                if (tier.getPremiumReward() != null) {
                    tierObj.add("premiumReward", tier.getPremiumReward().toJson());
                }

                tiersArray.add(tierObj);
            });

        json.add("tiers", tiersArray);
        Utils.writeFileSync(TIERS_PATH, TIERS_FILE, Utils.newGson().toJson(json));
    }

    public BattlePassTier getTier(int level) {
        return tiers.get(level);
    }

    public Map<Integer, BattlePassTier> getAllTiers() {
        return new HashMap<>(tiers);
    }

    public boolean isLoaded() {
        return loaded;
    }
}
