package com.cobblemon.mdks.cobblepass.battlepass;

import com.cobblemon.mdks.cobblepass.CobblePass;
import com.cobblemon.mdks.cobblepass.util.Constants;
import com.cobblemon.mdks.cobblepass.util.Utils;
import com.cobblemon.mdks.cobblepass.config.TierConfig;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BattlePass {
    private final Map<UUID, PlayerBattlePass> playerPasses = new HashMap<>();
    private TierConfig tierConfig;

    public BattlePass() {
        this.tierConfig = new TierConfig();
        // Ensure player data directory exists
        File playerDir = new File(Constants.PLAYER_DATA_DIR);
        if (!playerDir.exists()) {
            playerDir.mkdirs();
        }
    }

    public void init() {
        // Load player data
        File playerDir = new File(Constants.PLAYER_DATA_DIR);
        if (playerDir.exists() && playerDir.isDirectory()) {
            File[] files = playerDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.getName().endsWith(".json")) {
                        loadPlayerPass(file.getName().replace(".json", ""));
                    }
                }
            }
        }
    }

    public void loadPlayerPass(String uuid) {
        String filename = uuid + ".json";
        String content = Utils.readFileSync(Constants.PLAYER_DATA_DIR, filename);
        
        if (content == null || content.isEmpty()) {
            // Create new player pass if file doesn't exist
            PlayerBattlePass newPass = new PlayerBattlePass(UUID.fromString(uuid));
            playerPasses.put(UUID.fromString(uuid), newPass);
            // Save the new pass immediately
            savePlayerPass(uuid);
            return;
        }

        try {
            JsonObject json = JsonParser.parseString(content).getAsJsonObject();
            PlayerBattlePass pass = new PlayerBattlePass(UUID.fromString(uuid));
            pass.fromJson(json);
            playerPasses.put(UUID.fromString(uuid), pass);
            CobblePass.LOGGER.debug("Loaded battle pass for " + uuid + " with level " + pass.getLevel() + " and XP " + pass.getXP());
        } catch (Exception e) {
            CobblePass.LOGGER.error("Failed to load battle pass for " + uuid, e);
        }
    }

    public void savePlayerPass(String uuid) {
        PlayerBattlePass pass = playerPasses.get(UUID.fromString(uuid));
        if (pass != null) {
            String filename = uuid + ".json";
            Utils.writeFileSync(Constants.PLAYER_DATA_DIR, filename,
                    Utils.newGson().toJson(pass.toJson()));
            CobblePass.LOGGER.debug("Saved battle pass for " + uuid + " with level " + pass.getLevel() + " and XP " + pass.getXP());
        }
    }

    public void reload() {
        this.tierConfig = new TierConfig();
        init();
    }

    public void reloadTiers() {
        // Only reload tier configuration without touching player data
        this.tierConfig = new TierConfig();
    }

    public void save() {
        for (Map.Entry<UUID, PlayerBattlePass> entry : playerPasses.entrySet()) {
            String filename = entry.getKey() + ".json";
            Utils.writeFileSync(Constants.PLAYER_DATA_DIR, filename,
                    Utils.newGson().toJson(entry.getValue().toJson()));
        }
        tierConfig.save();
    }

    public PlayerBattlePass getPlayerPass(ServerPlayer player) {
        UUID uuid = player.getUUID();
        if (!playerPasses.containsKey(uuid)) {
            // Try to load from file first
            loadPlayerPass(uuid.toString());
        }
        // If loading failed or no file exists, create new pass
        return playerPasses.computeIfAbsent(uuid,
                id -> {
                    PlayerBattlePass newPass = new PlayerBattlePass(id);
                    // Save the new pass immediately
                    savePlayerPass(id.toString());
                    return newPass;
                });
    }

    public void addXP(ServerPlayer player, int amount) {
        PlayerBattlePass pass = getPlayerPass(player);
        pass.addXP(amount);

        // Save after XP change - use sync to ensure level progression is saved immediately
        savePlayerPass(player.getUUID().toString());
    }

    public boolean claimReward(ServerPlayer player, int level, boolean premium) {
        PlayerBattlePass pass = getPlayerPass(player);
        BattlePassTier tier = getTier(level);

        if (tier == null) {
            return false;
        }

        // Check if player has reached required level
        if (level > pass.getLevel()) {
            player.sendSystemMessage(Component.literal(String.format(
                Constants.MSG_LEVEL_NOT_REACHED,
                level
            )));
            return false;
        }

        // Check if already claimed
        if (premium && pass.hasClaimedPremiumReward(level)) {
            player.sendSystemMessage(Component.literal(String.format(
                Constants.MSG_ALREADY_CLAIMED_LEVEL,
                level
            )));
            return false;
        } else if (!premium && pass.hasClaimedFreeReward(level)) {
            player.sendSystemMessage(Component.literal(String.format(
                Constants.MSG_ALREADY_CLAIMED_LEVEL,
                level
            )));
            return false;
        }

        // Mark as claimed first
        if (premium) {
            pass.claimPremiumReward(level);
        } else {
            pass.claimFreeReward(level);
        }

        // Save claim state immediately
        savePlayerPass(player.getUUID().toString());

        // Grant reward after saving claim state
        if (premium) {
            tier.grantPremiumReward(player);
        } else {
            tier.grantFreeReward(player);
        }
        return true;
    }

    public BattlePassTier getTier(int level) {
        return tierConfig.getTier(level);
    }

    public Map<Integer, BattlePassTier> getTiers() {
        return tierConfig.getAllTiers();
    }
}
