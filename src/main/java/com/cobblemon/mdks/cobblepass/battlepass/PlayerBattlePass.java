package com.cobblemon.mdks.cobblepass.battlepass;

import com.cobblemon.mdks.cobblepass.CobblePass;
import com.cobblemon.mdks.cobblepass.util.Constants;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

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

    public void claimFreeReward(int level) {
        claimedFreeRewards.add(level);
    }

    public void claimPremiumReward(int level) {
        claimedPremiumRewards.add(level);
    }

    public void setPremium(boolean premium) {
        this.isPremium = premium;
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
    public boolean isPremium() {
        return isPremium;
    }
    public boolean hasPremium() { return isPremium; }
    public Set<Integer> getClaimedFreeRewards() { return new HashSet<>(claimedFreeRewards); }
    public Set<Integer> getClaimedPremiumRewards() { return new HashSet<>(claimedPremiumRewards); }
}
