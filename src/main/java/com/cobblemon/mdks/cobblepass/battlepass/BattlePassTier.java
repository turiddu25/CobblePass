package com.cobblemon.mdks.cobblepass.battlepass;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.RegistryAccess;

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

    public ItemStack getFreeRewardItem(RegistryAccess registryAccess) {
        try {
            CompoundTag tag = TagParser.parseTag(freeReward);
            return ItemStack.parse(registryAccess, tag).orElse(ItemStack.EMPTY);
        } catch (Exception e) {
            return ItemStack.EMPTY;
        }
    }

    public ItemStack getPremiumRewardItem(RegistryAccess registryAccess) {
        try {
            CompoundTag tag = TagParser.parseTag(premiumReward);
            return ItemStack.parse(registryAccess, tag).orElse(ItemStack.EMPTY);
        } catch (Exception e) {
            return ItemStack.EMPTY;
        }
    }

    public boolean hasFreeReward(RegistryAccess registryAccess) {
        return !getFreeRewardItem(registryAccess).isEmpty();
    }

    public boolean hasPremiumReward(RegistryAccess registryAccess) {
        return !getPremiumRewardItem(registryAccess).isEmpty();
    }
}
