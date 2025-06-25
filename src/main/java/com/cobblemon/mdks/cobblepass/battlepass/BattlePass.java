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
import java.util.concurrent.ConcurrentHashMap;

public class BattlePass {
    private final Map<UUID, PlayerBattlePass> playerPasses = new ConcurrentHashMap<>();
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
        this.tierConfig.load(); // Load the tier configuration from tiers.json

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
            CobblePass.LOGGER.error("Failed to load battle pass for " + uuid + ". The file may be corrupt.", e);
            
            // Backup the corrupted file instead of allowing it to be overwritten.
            File corruptedFile = new File(Constants.PLAYER_DATA_DIR, uuid + ".json");
            File backupFile = new File(Constants.PLAYER_DATA_DIR, uuid + ".json.corrupted");
            if (corruptedFile.exists()) {
                corruptedFile.renameTo(backupFile);
                CobblePass.LOGGER.error("The corrupted file has been renamed to " + backupFile.getName() + " for manual review.");
            }
            // By not creating a new pass here, the player won't be able to interact with the pass
            // until the issue is fixed, but it prevents data loss.
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


    public void reloadTiers() {
        // Only reload tier configuration without touching player data
        this.tierConfig.load();
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
        // This check is now very important. If the map doesn't contain the UUID,
        // it could be because their file was corrupt.
        if (!playerPasses.containsKey(uuid)) {
            loadPlayerPass(uuid.toString());
        }
        
        // This will now only create a new pass if no file existed in the first place.
        // If loading failed, it will return null, preventing interaction and data wipe.
        return playerPasses.computeIfAbsent(uuid,
                id -> {
                    // This block now only runs for a genuinely new player.
                    PlayerBattlePass newPass = new PlayerBattlePass(id);
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

    public void generateNewTiers(int maxLevel) {
        this.tierConfig.generateAndSaveTiers(maxLevel);
        this.tierConfig.load(); // Reload the newly created tiers into memory
    }

    public void resetData() {
        this.tierConfig = new TierConfig();
        this.playerPasses.clear();
    }

    public void resetAllPlayerData() {
        this.playerPasses.clear(); // Clear the in-memory map

        // Delete all player data files on disk
        File playersDir = new File(Constants.PLAYER_DATA_DIR);
        if (playersDir.exists()) {
            try {
                // Walk the directory and delete all .json files within it
                java.nio.file.Files.walk(playersDir.toPath())
                    .filter(p -> p.toString().endsWith(".json"))
                    .map(java.nio.file.Path::toFile)
                    .forEach(File::delete);
                CobblePass.LOGGER.info("All player battle pass data has been reset.");
            } catch (java.io.IOException e) {
                CobblePass.LOGGER.error("Failed to reset player battle pass data files.", e);
            }
        }
    }
}
