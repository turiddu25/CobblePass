package com.cobblemon.mdks.cobblepass.battlepass;

import com.cobblemon.mdks.cobblepass.CobblePass;
import com.cobblemon.mdks.cobblepass.util.Constants;
import com.cobblemon.mdks.cobblepass.util.Utils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.server.level.ServerPlayer;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BattlePass {
    private final Map<UUID, PlayerBattlePass> playerPasses;
    private final List<BattlePassTier> tiers;

    public BattlePass() {
        this.playerPasses = new HashMap<>();
        this.tiers = new ArrayList<>();
    }

    public void init() {
        // Load tiers
        loadTiers();

        // Load player data
        File playerDir = new File(Constants.PLAYER_DATA_DIR);
        if (playerDir.exists() && playerDir.isDirectory()) {
            for (File file : playerDir.listFiles()) {
                if (file.isFile() && file.getName().endsWith(".json")) {
                    loadPlayerPass(file.getName().replace(".json", ""));
                }
            }
        }
    }

    public void loadTiers() {
        Utils.readFileAsync(Constants.CONFIG_DIR, "tiers.json", content -> {
            if (content == null || content.isEmpty()) {
                createDefaultTiers();
                saveTiers();
                return;
            }

            try {
                JsonArray tiersArray = JsonParser.parseString(content).getAsJsonArray();
                tiers.clear();
                for (JsonElement element : tiersArray) {
                    JsonObject tierObj = element.getAsJsonObject();
                    tiers.add(new BattlePassTier(
                            tierObj.get("level").getAsInt(),
                            tierObj.get("freeReward").getAsString(),
                            tierObj.get("premiumReward").getAsString()
                    ));
                }
            } catch (Exception e) {
                CobblePass.LOGGER.error("Failed to load tiers", e);
                createDefaultTiers();
                saveTiers();
            }
        });
    }

    private void createDefaultTiers() {
        tiers.clear();
        // Add some default tiers
        tiers.add(new BattlePassTier(1, "minecraft:diamond", "minecraft:diamond_block"));
        tiers.add(new BattlePassTier(2, "minecraft:iron_ingot{Count:5}", "minecraft:iron_block"));
        tiers.add(new BattlePassTier(3, "cobblemon:poke_ball{Count:3}", "cobblemon:ultra_ball{Count:3}"));
    }

    public void saveTiers() {
        JsonArray tiersArray = new JsonArray();
        for (BattlePassTier tier : tiers) {
            JsonObject tierObj = new JsonObject();
            tierObj.addProperty("level", tier.getLevel());
            tierObj.addProperty("freeReward", tier.getFreeReward());
            tierObj.addProperty("premiumReward", tier.getPremiumReward());
            tiersArray.add(tierObj);
        }
        Utils.writeFileAsync(Constants.CONFIG_DIR, "tiers.json", Utils.newGson().toJson(tiersArray));
    }

    private void loadPlayerPass(String uuid) {
        String filename = uuid + ".json";
        Utils.readFileAsync(Constants.PLAYER_DATA_DIR, filename, content -> {
            if (content == null || content.isEmpty()) return;

            try {
                JsonObject json = JsonParser.parseString(content).getAsJsonObject();
                PlayerBattlePass pass = new PlayerBattlePass(UUID.fromString(uuid));
                pass.fromJson(json);
                playerPasses.put(UUID.fromString(uuid), pass);
            } catch (Exception e) {
                CobblePass.LOGGER.error("Failed to load battle pass for " + uuid, e);
            }
        });
    }

    public void save() {
        for (PlayerBattlePass pass : playerPasses.values()) {
            String filename = pass.getPlayerId() + ".json";
            Utils.writeFileAsync(Constants.PLAYER_DATA_DIR, filename,
                    Utils.newGson().toJson(pass.toJson()));
        }
        saveTiers();
    }

    public PlayerBattlePass getPlayerPass(ServerPlayer player) {
        return playerPasses.computeIfAbsent(player.getUUID(),
                uuid -> new PlayerBattlePass(uuid));
    }

    public void addXP(ServerPlayer player, int amount) {
        PlayerBattlePass pass = getPlayerPass(player);
        pass.addXP(amount);

        // Save after XP change
        String filename = player.getUUID() + ".json";
        Utils.writeFileAsync(Constants.PLAYER_DATA_DIR, filename,
                Utils.newGson().toJson(pass.toJson()));
    }

    public boolean claimReward(ServerPlayer player, int level, boolean premium) {
        PlayerBattlePass pass = getPlayerPass(player);
        BattlePassTier tier = getTier(level);

        if (tier == null) {
            return false;
        }

        boolean claimed = pass.claimReward(level, premium, tier, player.level().registryAccess());
        if (claimed) {
            // Save after claiming reward
            String filename = player.getUUID() + ".json";
            Utils.writeFileAsync(Constants.PLAYER_DATA_DIR, filename,
                    Utils.newGson().toJson(pass.toJson()));
        }
        return claimed;
    }

    public BattlePassTier getTier(int level) {
        return tiers.stream()
                .filter(tier -> tier.getLevel() == level)
                .findFirst()
                .orElse(null);
    }

    public List<BattlePassTier> getTiers() {
        return new ArrayList<>(tiers);
    }
}
