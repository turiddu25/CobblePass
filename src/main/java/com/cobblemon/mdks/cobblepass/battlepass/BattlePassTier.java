package com.cobblemon.mdks.cobblepass.battlepass;

import net.minecraft.nbt.TagParser;
import net.minecraft.world.item.ItemStack;

public class BattlePassTier {
    private final int level;
    private final String freeReward;
    private final String premiumReward;

    public BattlePassTier(int level, String freeReward, String premiumReward) {
        this.level = level;
        this.freeReward = freeReward;
        this.premiumReward = premiumReward;
    }

    public int getLevel() {
        return level;
    }

    public String getFreeReward() {
        return freeReward;
    }

    public String getPremiumReward() {
        return premiumReward;
    }

    public ItemStack getFreeRewardItem() {
        try {
            return ItemStack.of(TagParser.parseTag(freeReward));
        } catch (Exception e) {
            return ItemStack.EMPTY;
        }
    }

    public ItemStack getPremiumRewardItem() {
        try {
            return ItemStack.of(TagParser.parseTag(premiumReward));
        } catch (Exception e) {
            return ItemStack.EMPTY;
        }
    }

    public boolean hasFreeReward() {
        return !getFreeRewardItem().isEmpty();
    }

    public boolean hasPremiumReward() {
        return !getPremiumRewardItem().isEmpty();
    }
}
