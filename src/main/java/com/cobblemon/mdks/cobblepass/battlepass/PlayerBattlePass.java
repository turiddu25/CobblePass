package com.cobblemon.mdks.cobblepass.battlepass;

import com.cobblemon.mdks.cobblepass.CobblePass;
import com.cobblemon.mdks.cobblepass.util.Constants;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.RegistryAccess;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PlayerBattlePass {
    private final UUID playerId;
    private String version;
    private int level;
    private int xp;
    private boolean isPremium;
    private long premiumExpiry;
    private final Set<Integer> claimedFreeRewards;
    private final Set<Integer> claimedPremiumRewards;

    public PlayerBattlePass(UUID playerId) {
        this.playerId = playerId;
        this.version = Constants.PLAYER_DATA_VERSION;
        this.level = 1;
        this.xp = 0;
        this.isPremium = false;
        this.premiumExpiry = -1;
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
                xp = getXPForNextLevel();
                return;
            }
            xp -= getXPForNextLevel();
            level++;
        }
    }

    private int getXPForNextLevel() {
        return (int)(CobblePass.config.getXpPerLevel() * Math.pow(Constants.XP_MULTIPLIER, level - 1));
    }

    public boolean claimReward(int level, boolean premium, BattlePassTier tier, RegistryAccess registryAccess) {
        if (this.level < level) {
            return false;
        }

        if (premium) {
            if (!isPremium || hasClaimedPremiumReward(level)) {
                return false;
            }
            ItemStack reward = tier.getPremiumRewardItem(registryAccess);
            if (reward.isEmpty()) {
                return false;
            }
            claimedPremiumRewards.add(level);
            return true;
        } else {
            if (hasClaimedFreeReward(level)) {
                return false;
            }
            ItemStack reward = tier.getFreeRewardItem(registryAccess);
            if (reward.isEmpty()) {
                return false;
            }
            claimedFreeRewards.add(level);
            return true;
        }
    }

    public void setPremium(boolean premium, long duration) {
        this.isPremium = premium;
        this.premiumExpiry = duration > 0 ? System.currentTimeMillis() + duration : -1;
    }

    public void checkPremiumExpiry() {
        if (premiumExpiry > 0 && System.currentTimeMillis() > premiumExpiry) {
            isPremium = false;
            premiumExpiry = -1;
        }
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
        json.addProperty("premiumExpiry", premiumExpiry);

        JsonArray freeRewards = new JsonArray();
        claimedFreeRewards.forEach(freeRewards::add);
        json.add("claimedFreeRewards", freeRewards);

        JsonArray premiumRewards = new JsonArray();
        claimedPremiumRewards.forEach(premiumRewards::add);
        json.add("claimedPremiumRewards", premiumRewards);

        return json;
    }

    public void fromJson(JsonObject json) {
        version = json.get("version").getAsString();
        level = json.get("level").getAsInt();
        xp = json.get("xp").getAsInt();
        isPremium = json.get("isPremium").getAsBoolean();
        premiumExpiry = json.get("premiumExpiry").getAsLong();

        claimedFreeRewards.clear();
        json.get("claimedFreeRewards").getAsJsonArray()
                .forEach(e -> claimedFreeRewards.add(e.getAsInt()));

        claimedPremiumRewards.clear();
        json.get("claimedPremiumRewards").getAsJsonArray()
                .forEach(e -> claimedPremiumRewards.add(e.getAsInt()));

        checkPremiumExpiry();
    }

    // Getters
    public UUID getPlayerId() { return playerId; }
    public String getVersion() { return version; }
    public int getLevel() { return level; }
    public int getXP() { return xp; }
    public boolean isPremium() {
        checkPremiumExpiry();
        return isPremium;
    }
    public long getPremiumExpiry() { return premiumExpiry; }
    public Set<Integer> getClaimedFreeRewards() { return new HashSet<>(claimedFreeRewards); }
    public Set<Integer> getClaimedPremiumRewards() { return new HashSet<>(claimedPremiumRewards); }
    public boolean hasPremium() { return isPremium(); }
}
