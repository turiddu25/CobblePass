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
    }

    public void init() {
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

    private void loadPlayerPass(String uuid) {
        String filename = uuid + ".json";
        String content = Utils.readFileSync(Constants.PLAYER_DATA_DIR, filename);
        
        if (content == null || content.isEmpty()) return;

        try {
            JsonObject json = JsonParser.parseString(content).getAsJsonObject();
            PlayerBattlePass pass = new PlayerBattlePass(UUID.fromString(uuid));
            pass.fromJson(json);
            playerPasses.put(UUID.fromString(uuid), pass);
        } catch (Exception e) {
            CobblePass.LOGGER.error("Failed to load battle pass for " + uuid, e);
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
        for (PlayerBattlePass pass : playerPasses.values()) {
            String filename = pass.getPlayerId() + ".json";
            Utils.writeFileAsync(Constants.PLAYER_DATA_DIR, filename,
                    Utils.newGson().toJson(pass.toJson()));
        }
        tierConfig.save();
    }

    public PlayerBattlePass getPlayerPass(ServerPlayer player) {
        return playerPasses.computeIfAbsent(player.getUUID(),
                uuid -> new PlayerBattlePass(uuid));
    }

    public void addXP(ServerPlayer player, int amount) {
        PlayerBattlePass pass = getPlayerPass(player);
        pass.addXP(amount);

        // Save after XP change - use sync to ensure level progression is saved immediately
        String filename = player.getUUID() + ".json";
        Utils.writeFileSync(Constants.PLAYER_DATA_DIR, filename,
                Utils.newGson().toJson(pass.toJson()));
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
        String filename = player.getUUID() + ".json";
        Utils.writeFileSync(Constants.PLAYER_DATA_DIR, filename,
                Utils.newGson().toJson(pass.toJson()));

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
