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
        JsonArray tiersArray = json.getAsJsonArray("tiers");
        
        for (JsonElement element : tiersArray) {
            JsonObject tierObj = element.getAsJsonObject();
            int level = tierObj.get("level").getAsInt();
            
            // Load rewards
            Reward freeReward = null;
            Reward premiumReward = null;

            if (tierObj.has("freeReward")) {
                JsonObject freeRewardObj = tierObj.getAsJsonObject("freeReward");
                freeReward = Reward.fromJson(freeRewardObj);
            }

            if (tierObj.has("premiumReward")) {
                JsonObject premiumRewardObj = tierObj.getAsJsonObject("premiumReward");
                premiumReward = Reward.fromJson(premiumRewardObj);
            }

            // Backwards compatibility for old format
            if (freeReward == null && premiumReward == null) {
                String displayItem = tierObj.get("displayItem").getAsString();
                String redeemableItem = tierObj.get("redeemableItem").getAsString();
                boolean isPremium = tierObj.get("isPremium").getAsBoolean();
                
                String redeemNbt = String.format("{id:\"%s\",Count:1}", redeemableItem);
                
                if (isPremium) {
                    premiumReward = Reward.minecraftItem(redeemNbt);
                } else {
                    freeReward = Reward.minecraftItem(redeemNbt);
                }
            }
            
            tiers.put(level, new BattlePassTier(level, freeReward, premiumReward));
        }
    }

    private void generateDefaultTiers() {
        tiers.clear();
        // Generate 100 default tiers
        for (int i = 1; i <= 100; i++) {
            String displayItem = getDefaultDisplayItem(i);
            String redeemItem = getDefaultRedeemItem(i);
            boolean isPremium = i % 5 == 0; // Every 5th tier is premium by default
            
            String redeemNbt = String.format("{id:\"%s\",Count:1}", redeemItem);
            
            Reward freeReward = isPremium ? null : Reward.minecraftItem(redeemNbt);
            Reward premiumReward = isPremium ? Reward.minecraftItem(redeemNbt) : null;
            
            tiers.put(i, new BattlePassTier(i, freeReward, premiumReward));
        }
    }

    private String getDefaultDisplayItem(int level) {
        // Default display items based on tier level
        if (level % 10 == 0) return "minecraft:diamond";
        if (level % 5 == 0) return "minecraft:gold_ingot";
        return "minecraft:iron_ingot";
    }

    private String getDefaultRedeemItem(int level) {
        // Default redeem items based on tier level
        if (level % 10 == 0) return "minecraft:diamond_block";
        if (level % 5 == 0) return "minecraft:gold_block";
        return "minecraft:iron_block";
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
