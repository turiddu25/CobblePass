package com.cobblemon.mdks.cobblepass.battlepass;

import com.cobblemon.mdks.cobblepass.data.Reward;
import com.cobblemon.mdks.cobblepass.data.RewardType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.TagParser;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

public class BattlePassTier {
    private final int level;
    private final Reward freeReward;
    private final Reward premiumReward;

    public BattlePassTier(int level, Reward freeReward, Reward premiumReward) {
        this.level = level;
        this.freeReward = freeReward;
        this.premiumReward = premiumReward;
    }

    public int getLevel() {
        return level;
    }

    public Reward getFreeReward() {
        return freeReward;
    }

    public Reward getPremiumReward() {
        return premiumReward;
    }

    public void grantFreeReward(ServerPlayer player) {
        if (freeReward != null) {
            freeReward.grant(player);
        }
    }

    public void grantPremiumReward(ServerPlayer player) {
        if (premiumReward != null) {
            premiumReward.grant(player);
        }
    }

    public ItemStack getFreeRewardItem(RegistryAccess registryAccess) {
        if (freeReward == null) return ItemStack.EMPTY;
        
        if (freeReward.getType() == RewardType.POKEMON) {
            try {
                // Use Poké Ball as display item for Pokemon rewards
                return ItemStack.parse(registryAccess, TagParser.parseTag("{id:\"cobblemon:poke_ball\",Count:1}")).orElse(ItemStack.EMPTY);
            } catch (CommandSyntaxException e) {
                return ItemStack.EMPTY;
            }
        }
        
        return freeReward.getItemStack(registryAccess);
    }

    public ItemStack getPremiumRewardItem(RegistryAccess registryAccess) {
        if (premiumReward == null) return ItemStack.EMPTY;
        
        if (premiumReward.getType() == RewardType.POKEMON) {
            try {
                // Use Master Ball as display item for premium Pokemon rewards
                return ItemStack.parse(registryAccess, TagParser.parseTag("{id:\"cobblemon:master_ball\",Count:1}")).orElse(ItemStack.EMPTY);
            } catch (CommandSyntaxException e) {
                return ItemStack.EMPTY;
            }
        }
        
        return premiumReward.getItemStack(registryAccess);
    }

    public boolean hasFreeReward() {
        return freeReward != null;
    }

    public boolean hasPremiumReward() {
        return premiumReward != null;
    }
}
