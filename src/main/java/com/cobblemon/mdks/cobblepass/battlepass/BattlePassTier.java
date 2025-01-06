package com.cobblemon.mdks.cobblepass.battlepass;

import com.cobblemon.mdks.cobblepass.data.Reward;
import com.cobblemon.mdks.cobblepass.data.RewardType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.TagParser;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.cobblemon.mod.common.pokemon.Species;
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies;
import com.cobblemon.mod.common.item.PokemonItem;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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
        
        if (freeReward.getType() == RewardType.COMMAND) {
            try {
                JsonObject data = freeReward.getData();
                if (data != null && data.has("id")) {
                    return ItemStack.parse(registryAccess, TagParser.parseTag("{id:\"" + data.get("id").getAsString() + "\",Count:1}")).orElse(ItemStack.EMPTY);
                }
            } catch (CommandSyntaxException ex) {
                return ItemStack.EMPTY;
            }
        } else if (freeReward.getType() == RewardType.POKEMON) {
            try {
                // Parse Pokemon data to get species
                JsonObject pokemonData = freeReward.getData();
                String speciesName = pokemonData.get("species").getAsString();
                
                // Create Pokemon instance
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
                // Fallback to Poké Ball if parsing fails
                try {
                    return ItemStack.parse(registryAccess, TagParser.parseTag("{id:\"cobblemon:poke_ball\",Count:1}")).orElse(ItemStack.EMPTY);
                } catch (CommandSyntaxException ex) {
                    return ItemStack.EMPTY;
                }
            }
        }
        
        return freeReward.getItemStack(registryAccess);
    }

    public ItemStack getPremiumRewardItem(RegistryAccess registryAccess) {
        if (premiumReward == null) return ItemStack.EMPTY;
        
        if (premiumReward.getType() == RewardType.COMMAND) {
            try {
                JsonObject data = premiumReward.getData();
                if (data != null && data.has("id")) {
                    return ItemStack.parse(registryAccess, TagParser.parseTag("{id:\"" + data.get("id").getAsString() + "\",Count:1}")).orElse(ItemStack.EMPTY);
                }
            } catch (CommandSyntaxException ex) {
                return ItemStack.EMPTY;
            }
        } else if (premiumReward.getType() == RewardType.POKEMON) {
            try {
                // Parse Pokemon data to get species
                JsonObject pokemonData = premiumReward.getData();
                String speciesName = pokemonData.get("species").getAsString();
                
                // Create Pokemon instance
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
                // Fallback to Master Ball if parsing fails
                try {
                    return ItemStack.parse(registryAccess, TagParser.parseTag("{id:\"cobblemon:master_ball\",Count:1}")).orElse(ItemStack.EMPTY);
                } catch (CommandSyntaxException ex) {
                    return ItemStack.EMPTY;
                }
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
