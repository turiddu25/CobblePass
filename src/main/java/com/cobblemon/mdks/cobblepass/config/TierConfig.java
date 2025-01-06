package com.cobblemon.mdks.cobblepass.config;

import com.cobblemon.mdks.cobblepass.CobblePass;
import com.cobblemon.mdks.cobblepass.battlepass.BattlePassTier;
import com.cobblemon.mdks.cobblepass.data.Reward;
import com.cobblemon.mdks.cobblepass.data.RewardType;
import com.cobblemon.mdks.cobblepass.util.Constants;
import com.cobblemon.mdks.cobblepass.util.Utils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.Map;

public class TierConfig {
    private static final String TIERS_FILE = Constants.TIERS_FILE;
    private static final String TIERS_PATH = Constants.CONFIG_DIR;
    
    private final Map<Integer, BattlePassTier> tiers = new HashMap<>();

    public TierConfig() {
        load();
    }

    public void load() {
        String content = Utils.readFileSync(TIERS_PATH, TIERS_FILE);
        if (content == null || content.isEmpty()) {
            generateDefaultTiers();
            save();
            return;
        }

        try {
            JsonObject json = JsonParser.parseString(content).getAsJsonObject();
            loadFromJson(json);
        } catch (Exception e) {
            CobblePass.LOGGER.error("Failed to load tier config", e);
            generateDefaultTiers();
            save();
        }
    }

    private void loadFromJson(JsonObject json) {
        tiers.clear();
        
        // Load templates if they exist
        Map<String, JsonObject> templates = new HashMap<>();
        if (json.has("templates")) {
            JsonObject templatesObj = json.getAsJsonObject("templates");
            for (Map.Entry<String, JsonElement> entry : templatesObj.entrySet()) {
                templates.put(entry.getKey(), entry.getValue().getAsJsonObject());
            }
        }

        JsonArray tiersArray = json.getAsJsonArray("tiers");
        
        for (JsonElement element : tiersArray) {
            JsonObject tierObj = element.getAsJsonObject();
            int level = tierObj.get("level").getAsInt();
            
            // Load rewards
            Reward freeReward = loadReward(tierObj, "freeReward", templates);
            Reward premiumReward = loadReward(tierObj, "premiumReward", templates);
            
            tiers.put(level, new BattlePassTier(level, freeReward, premiumReward));
        }
    }

    private Reward loadReward(JsonObject tierObj, String rewardKey, Map<String, JsonObject> templates) {
        if (!tierObj.has(rewardKey)) {
            return null;
        }

        JsonElement rewardElement = tierObj.get(rewardKey);
        
        // Handle template references
        if (rewardElement.isJsonPrimitive() && rewardElement.getAsString().startsWith("@")) {
            String templateName = rewardElement.getAsString().substring(1);
            if (templates.containsKey(templateName)) {
                return Reward.fromJson(templates.get(templateName));
            }
            return null;
        }

        // Handle direct reward definitions
        if (rewardElement.isJsonObject()) {
            return Reward.fromJson(rewardElement.getAsJsonObject());
        }

        return null;
    }

    private void generateDefaultTiers() {
        tiers.clear();
        
        // Tier 1 - Basic Item
        JsonObject tier1Data = new JsonObject();
        tier1Data.addProperty("id", "cobblemon:poke_ball");
        tier1Data.addProperty("Count", 5);
        tiers.put(1, new BattlePassTier(1, Reward.item(tier1Data), null));
        
        // Tier 2 - Minecraft Item
        JsonObject tier2Data = new JsonObject();
        tier2Data.addProperty("id", "minecraft:diamond");
        tier2Data.addProperty("Count", 3);
        tiers.put(2, new BattlePassTier(2, Reward.item(tier2Data), null));
        
        // Tier 3 - Command with Custom Display
        tiers.put(3, new BattlePassTier(3, 
            Reward.command(
                "effect give %player% minecraft:regeneration 30 2",
                "minecraft:potion",
                "Healing Boost"
            ), 
            null));
        
        // Tier 4 - Pokemon
        JsonObject tier4Data = new JsonObject();
        tier4Data.addProperty("species", "eevee");
        tier4Data.addProperty("level", 10);
        tier4Data.addProperty("shiny", false);
        tiers.put(4, new BattlePassTier(4, Reward.pokemon(tier4Data), null));
        
        // Tier 5 - Premium Command
        tiers.put(5, new BattlePassTier(5, null,
            Reward.command(
                "xp add %player% 1000",
                "minecraft:experience_bottle",
                "XP Boost"
            )));
    }

    public void save() {
        JsonObject json = new JsonObject();
        JsonArray tiersArray = new JsonArray();
        
        for (BattlePassTier tier : tiers.values()) {
            JsonObject tierObj = new JsonObject();
            tierObj.addProperty("level", tier.getLevel());
            
            if (tier.getFreeReward() != null) {
                tierObj.add("freeReward", tier.getFreeReward().toJson());
            }
            
            if (tier.getPremiumReward() != null) {
                tierObj.add("premiumReward", tier.getPremiumReward().toJson());
            }
            
            tiersArray.add(tierObj);
        }
        
        json.add("tiers", tiersArray);
        Utils.writeFileSync(TIERS_PATH, TIERS_FILE, Utils.newGson().toJson(json));
    }

    private String extractItemId(String nbtString) {
        // Extract item ID from NBT string format {id:"minecraft:item",Count:1}
        try {
            int startIndex = nbtString.indexOf("\"") + 1;
            int endIndex = nbtString.indexOf("\"", startIndex);
            return nbtString.substring(startIndex, endIndex);
        } catch (Exception e) {
            return "minecraft:stone"; // Fallback item
        }
    }

    public BattlePassTier getTier(int level) {
        return tiers.get(level);
    }

    public Map<Integer, BattlePassTier> getAllTiers() {
        return new HashMap<>(tiers);
    }
}
