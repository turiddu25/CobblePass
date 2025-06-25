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
        // Do not load here
    }

    public void load() {
        String content = Utils.readFileSync(TIERS_PATH, TIERS_FILE);
        if (content == null || content.isEmpty()) {
            return;
        }

        try {
            JsonObject json = JsonParser.parseString(content).getAsJsonObject();
            loadFromJson(json);
        } catch (Exception e) {
            CobblePass.LOGGER.error("Failed to load tier config", e);
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

        if (rewardElement.isJsonObject()) {
            JsonObject rewardObject = rewardElement.getAsJsonObject();
            if (rewardObject.has("$template")) {
                String templateName = rewardObject.get("$template").getAsString();
                if (templates.containsKey(templateName)) {
                    return Reward.fromJson(templates.get(templateName));
                }
                return null;
            }
            return Reward.fromJson(rewardObject);
        }

        return null;
    }

    public void generateAndSaveTiers(int maxLevel) {
        tiers.clear();

        // Create a placeholder reward (an Apple, as you suggested)
        JsonObject placeholderData = new JsonObject();
        placeholderData.addProperty("id", "minecraft:apple");
        placeholderData.addProperty("Count", 1);
        Reward placeholderReward = Reward.item(placeholderData);

        for (int i = 1; i <= maxLevel; i++) {
            // By default, every tier gets a free placeholder reward. Premium is null.
            // Server owners can then change the item or add a premium reward.
            tiers.put(i, new BattlePassTier(i, placeholderReward, null));
        }

        // Now, save this new structure to tiers.json
        save();
    }

    private void generateDefaultTiers() {
        generateAndSaveTiers(10); // Generates a default 10-tier pass on first run
    }

    public void save() {
        JsonObject json = new JsonObject();
        JsonArray tiersArray = new JsonArray();

        // --- NEW: ADD A DEFAULT TEMPLATES OBJECT ---
        JsonObject templatesObj = new JsonObject();
        if (json.has("templates")) { // Preserve existing templates if they exist
            templatesObj = json.getAsJsonObject("templates");
        } else { // Otherwise, create some helpful default ones
            // Template for 10 Poke Balls
            JsonObject pokeBallData = new JsonObject();
            pokeBallData.addProperty("type", "ITEM");
            pokeBallData.add("data", new com.google.gson.Gson().fromJson("{id:\"cobblemon:poke_ball\",Count:10}", JsonObject.class));
            templatesObj.add("pokeballs_10", pokeBallData);

            // Template for a Rare Candy
            JsonObject rareCandyData = new JsonObject();
            rareCandyData.addProperty("type", "ITEM");
            rareCandyData.add("data", new com.google.gson.Gson().fromJson("{id:\"cobblemon:rare_candy\",Count:1}", JsonObject.class));
            templatesObj.add("rare_candy", rareCandyData);

            // Template for a random shiny Eevee
            JsonObject eeveeData = new JsonObject();
            eeveeData.addProperty("type", "POKEMON");
            eeveeData.add("data", new com.google.gson.Gson().fromJson("{species:\"eevee\",shiny:true}", JsonObject.class));
            templatesObj.add("shiny_eevee", eeveeData);

            // Template for a command reward
            JsonObject commandData = new JsonObject();
            commandData.addProperty("type", "COMMAND");
            commandData.add("data", new com.google.gson.Gson().fromJson("{command:\"say Hello %player%!\",display_name:\"Greeting\",id:\"minecraft:paper\"}", JsonObject.class));
            templatesObj.add("greeting_command", commandData);
        }
        json.add("templates", templatesObj);
        // --- END NEW PART ---

        for (BattlePassTier tier : tiers.values()) {
            JsonObject tierObj = new JsonObject();
            tierObj.addProperty("level", tier.getLevel());

            if (tier.getFreeReward() != null) {
                // We can now even generate tiers that use these templates by default!
                // For example, every 10th level could get a rare candy.
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
