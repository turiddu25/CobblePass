package com.cobblemon.mdks.cobblepass.battlepass;

import com.cobblemon.mdks.cobblepass.CobblePass;
import com.cobblemon.mdks.cobblepass.premium.PremiumManager;
import com.cobblemon.mdks.cobblepass.premium.PremiumMode;
import com.cobblemon.mdks.cobblepass.util.Constants;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PlayerBattlePass {
    private final UUID playerId;
    private String version;
    private int level;
    private int xp;
    private boolean isPremium;
    private final Set<Integer> claimedFreeRewards;
    private final Set<Integer> claimedPremiumRewards;

    public PlayerBattlePass(UUID playerId) {
        this.playerId = playerId;
        this.version = Constants.PLAYER_DATA_VERSION;
        this.level = 1;
        this.xp = 0;
        this.isPremium = false;
        this.claimedFreeRewards = new HashSet<>();
        this.claimedPremiumRewards = new HashSet<>();
    }

    public void addXP(int amount) {
        this.xp += amount;
        checkLevelUp();
    }

    private void checkLevelUp() {
        while (xp >= getXPForNextLevel()) {
            if (level >= CobblePass.config.getMaxLevel()) {
                level = CobblePass.config.getMaxLevel();
                xp = 0;
                return;
            }
            xp -= getXPForNextLevel();
            level++;
        }
    }

    private int getXPForNextLevel() {
        if (CobblePass.config.getXpProgression().getMode().equalsIgnoreCase("MANUAL")) {
            return CobblePass.config.getXpProgression().getManualXpForLevel(level + 1);
        } else {
            return (int)(CobblePass.config.getXpProgression().getXpPerLevel() * Math.pow(CobblePass.config.getXpProgression().getXpMultiplier(), level - 1));
        }
    }

    public void claimFreeReward(int level) {
        claimedFreeRewards.add(level);
    }

    public void claimPremiumReward(int level) {
        claimedPremiumRewards.add(level);
    }

    public void setPremium(boolean premium) {
        this.isPremium = premium;
    }
    
    /**
     * Migrates existing premium status data when switching premium modes.
     * This method handles the transition from stored premium status to provider-based status.
     * @param player The server player to migrate data for
     */
    public void migratePremiumStatus(ServerPlayer player) {
        PremiumMode currentMode = CobblePass.config.getPremiumConfig().getMode();
        
        // If we have stored premium status and we're not in DISABLED mode, 
        // try to grant premium through the current provider
        if (this.isPremium && currentMode != PremiumMode.DISABLED) {
            PremiumManager.getInstance().grantPremium(player);
            CobblePass.LOGGER.info("Migrated premium status for player " + player.getName().getString() + " to mode " + currentMode.getConfigValue());
        }
        
        // For ECONOMY mode, we preserve the stored status as it represents a purchase
        // For PERMISSION mode, the provider will check permissions directly
        // For DISABLED mode, everyone gets premium access regardless of stored status
    }

    public boolean hasClaimedFreeReward(int level) {
        return claimedFreeRewards.contains(level);
    }

    public boolean hasClaimedPremiumReward(int level) {
        return claimedPremiumRewards.contains(level);
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("version", version);
        json.addProperty("level", level);
        json.addProperty("xp", xp);
        json.addProperty("isPremium", isPremium);

        JsonArray freeRewards = new JsonArray();
        claimedFreeRewards.forEach(freeRewards::add);
        json.add("claimedFreeRewards", freeRewards);

        JsonArray premiumRewards = new JsonArray();
        claimedPremiumRewards.forEach(premiumRewards::add);
        json.add("claimedPremiumRewards", premiumRewards);

        return json;
    }

    public void fromJson(JsonObject json) {
        if (json.has("version")) version = json.get("version").getAsString();
        if (json.has("level")) level = json.get("level").getAsInt();
        if (json.has("xp")) xp = json.get("xp").getAsInt();
        if (json.has("isPremium")) isPremium = json.get("isPremium").getAsBoolean();

        claimedFreeRewards.clear();
        if (json.has("claimedFreeRewards")) {
            json.get("claimedFreeRewards").getAsJsonArray()
                    .forEach(e -> claimedFreeRewards.add(e.getAsInt()));
        }

        claimedPremiumRewards.clear();
        if (json.has("claimedPremiumRewards")) {
            json.get("claimedPremiumRewards").getAsJsonArray()
                    .forEach(e -> claimedPremiumRewards.add(e.getAsInt()));
        }
    }

    // Getters
    public UUID getPlayerId() { return playerId; }
    public String getVersion() { return version; }
    public int getLevel() { return level; }
    public int getXP() { return xp; }
    
    /**
     * Checks if the player has premium access using the current premium provider.
     * This method delegates to the PremiumManager instead of using stored premium status.
     * @param player The server player to check premium status for
     * @return true if the player has premium access according to the current provider
     */
    public boolean isPremium(ServerPlayer player) {
        return PremiumManager.getInstance().hasPremium(player);
    }
    
    /**
     * Legacy method for backward compatibility.
     * @deprecated Use isPremium(ServerPlayer) instead
     */
    @Deprecated
    public boolean isPremium() {
        return isPremium;
    }
    
    /**
     * Checks if the player has premium access using the current premium provider.
     * @param player The server player to check premium status for
     * @return true if the player has premium access according to the current provider
     */
    public boolean hasPremium(ServerPlayer player) {
        return PremiumManager.getInstance().hasPremium(player);
    }
    
    /**
     * Legacy method for backward compatibility.
     * @deprecated Use hasPremium(ServerPlayer) instead
     */
    @Deprecated
    public boolean hasPremium() { 
        return isPremium; 
    }
    
    /**
     * Gets the stored premium status for migration purposes.
     * This should only be used during migration operations.
     * @return The stored premium status
     */
    public boolean getStoredPremiumStatus() {
        return isPremium;
    }
    
    public Set<Integer> getClaimedFreeRewards() { return new HashSet<>(claimedFreeRewards); }
    public Set<Integer> getClaimedPremiumRewards() { return new HashSet<>(claimedPremiumRewards); }
}
