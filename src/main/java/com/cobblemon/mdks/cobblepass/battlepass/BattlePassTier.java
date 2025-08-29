package com.cobblemon.mdks.cobblepass.battlepass;

import com.cobblemon.mdks.cobblepass.data.Reward;
import com.cobblemon.mdks.cobblepass.data.RewardType;
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies;
import com.cobblemon.mod.common.item.PokemonItem;
import com.cobblemon.mod.common.pokemon.Species;
import com.google.gson.JsonObject;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.TagParser;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

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

    public ItemStack getFreeRewardItem(PlayerBattlePass playerPass, RegistryAccess registryAccess) {
        return getDisplayItem(playerPass, freeReward, false, registryAccess);
    }

    public ItemStack getPremiumRewardItem(PlayerBattlePass playerPass, RegistryAccess registryAccess) {
        return getDisplayItem(playerPass, premiumReward, true, registryAccess);
    }

    private ItemStack getDisplayItem(PlayerBattlePass playerPass, Reward reward, boolean isPremium, RegistryAccess registryAccess) {
        if (reward == null) {
            return ItemStack.EMPTY;
        }

        boolean isClaimed = isPremium ? playerPass.hasClaimedPremiumReward(level) : playerPass.hasClaimedFreeReward(level);
        boolean canClaim = playerPass.getLevel() >= this.level;

        JsonObject displayJson = null;
        if (isClaimed && reward.getClaimedDisplay() != null) {
            displayJson = reward.getClaimedDisplay();
        } else if (!canClaim && reward.getLockedDisplay() != null) {
            displayJson = reward.getLockedDisplay();
        } else if (canClaim && !isClaimed && reward.getClaimableDisplay() != null) {
            displayJson = reward.getClaimableDisplay();
        }

        if (displayJson != null) {
            try {
                String id = displayJson.get("id").getAsString();
                String displayName = displayJson.has("display_name") ? displayJson.get("display_name").getAsString() : null;
                ItemStack stack = ItemStack.parse(registryAccess, TagParser.parseTag("{id:\"" + id + "\",Count:1}")).orElse(ItemStack.EMPTY);
                if (displayName != null) {
                    stack.set(DataComponents.CUSTOM_NAME, net.minecraft.network.chat.Component.literal(displayName));
                }
                return stack;
            } catch (Exception e) {
                // Fall through to default behavior if display parsing fails
            }
        }

        // Fallback behavior when no custom display states are configured
        if (!canClaim) {
            // Show locked state with gray glass pane
            try {
                ItemStack lockedStack = ItemStack.parse(registryAccess, TagParser.parseTag("{id:\"minecraft:gray_stained_glass_pane\",Count:1}")).orElse(ItemStack.EMPTY);
                lockedStack.set(DataComponents.CUSTOM_NAME, net.minecraft.network.chat.Component.literal("§7Locked (Level " + this.level + ")"));
                return lockedStack;
            } catch (Exception e) {
                return ItemStack.EMPTY;
            }
        } else if (isClaimed) {
            // Show claimed state - use the actual reward item but with green tint indication
            ItemStack claimedStack = getActualRewardItem(reward, registryAccess);
            if (!claimedStack.isEmpty()) {
                // Add claimed indicator to the name
                net.minecraft.network.chat.Component originalName = claimedStack.get(DataComponents.CUSTOM_NAME);
                String nameText = originalName != null ? originalName.getString() : claimedStack.getDisplayName().getString();
                claimedStack.set(DataComponents.CUSTOM_NAME, net.minecraft.network.chat.Component.literal("§a✓ " + nameText));
                return claimedStack;
            }
        }

        // Default case: show the actual reward item
        return getActualRewardItem(reward, registryAccess);
    }

    private ItemStack getActualRewardItem(Reward reward, RegistryAccess registryAccess) {
        if (reward.getType() == RewardType.COMMAND) {
            try {
                JsonObject data = reward.getData();
                if (data != null && data.has("id")) {
                    return ItemStack.parse(registryAccess, TagParser.parseTag("{id:\"" + data.get("id").getAsString() + "\",Count:1}")).orElse(ItemStack.EMPTY);
                }
            } catch (CommandSyntaxException ex) {
                return ItemStack.EMPTY;
            }
        } else if (reward.getType() == RewardType.POKEMON) {
            try {
                JsonObject pokemonData = reward.getData();
                String speciesName = pokemonData.get("species").getAsString();
                Species species = PokemonSpecies.INSTANCE.getByName(speciesName);
                if (species != null) {
                    var pokemon = species.create(1);
                    if (pokemonData.has("shiny") && pokemonData.get("shiny").getAsBoolean()) {
                        pokemon.setShiny(true);
                    }
                    if (pokemonData.has("level")) {
                        pokemon.setLevel(pokemonData.get("level").getAsInt());
                    }
                    return PokemonItem.from(pokemon, 1);
                }
            } catch (Exception e) {
                try {
                    return ItemStack.parse(registryAccess, TagParser.parseTag("{id:\"cobblemon:poke_ball\",Count:1}")).orElse(ItemStack.EMPTY);
                } catch (CommandSyntaxException ex) {
                    return ItemStack.EMPTY;
                }
            }
        }

        return reward.getItemStack(registryAccess);
    }

    public boolean hasFreeReward() {
        return freeReward != null;
    }

    public boolean hasPremiumReward() {
        return premiumReward != null;
    }
}
