package com.cobblemon.mdks.cobblepass.data;

import com.cobblemon.mdks.cobblepass.CobblePass;
import com.google.gson.JsonObject;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class Reward {
    private final RewardType type;
    private final JsonObject data;
    private final String command;
    private final JsonObject lockedDisplay;
    private final JsonObject claimedDisplay;
    private final JsonObject claimableDisplay;

    public Reward(RewardType type, JsonObject data, String command, JsonObject lockedDisplay, JsonObject claimedDisplay, JsonObject claimableDisplay) {
        this.type = type;
        this.data = data;
        this.command = command;
        this.lockedDisplay = lockedDisplay;
        this.claimedDisplay = claimedDisplay;
        this.claimableDisplay = claimableDisplay;
    }

    public RewardType getType() {
        return type;
    }

    public JsonObject getData() {
        return data;
    }

    public String getCommand() {
        return command;
    }

    public JsonObject getLockedDisplay() {
        return lockedDisplay;
    }

    public JsonObject getClaimedDisplay() {
        return claimedDisplay;
    }

    public JsonObject getClaimableDisplay() {
        return claimableDisplay;
    }

    public ItemStack getItemStack(RegistryAccess registryAccess) {
        if (type != RewardType.ITEM) {
            return ItemStack.EMPTY;
        }

        try {
            CompoundTag tag = TagParser.parseTag(data.toString());
            ItemStack stack = ItemStack.parse(registryAccess, tag).orElse(ItemStack.EMPTY);
            if (tag.contains("Count")) {
                stack.setCount(tag.getInt("Count"));
            }
            return stack;
        } catch (Exception e) {
            return ItemStack.EMPTY;
        }
    }

    public void grant(ServerPlayer player) {
        switch (type) {
            case ITEM:
                ItemStack item = getItemStack(player.level().registryAccess());
                if (!item.isEmpty()) {
                    player.getInventory().add(item);
                }
                break;
            case POKEMON:
                if (data != null && !data.isEmpty()) {
                    try {
                        // Parse Pokemon data
                        JsonObject pokemonData = data;
                        String species = pokemonData.get("species").getAsString();
                        
                        // Build command with attributes
                        StringBuilder cmd = new StringBuilder();
                        cmd.append("givepokemonother ").append(player.getName().getString()).append(" ").append(species);
                        
                        if (pokemonData.has("shiny") && pokemonData.get("shiny").getAsBoolean()) {
                            cmd.append(" shiny");
                        }
                        if (pokemonData.has("level")) {
                            cmd.append(" level=").append(pokemonData.get("level").getAsInt());
                        }
                        if (pokemonData.has("ability")) {
                            cmd.append(" ability=").append(pokemonData.get("ability").getAsString());
                        }
                        
                        // Execute command as server
                        CommandSourceStack source = player.getServer().createCommandSourceStack();
                        player.getServer().getCommands().performPrefixedCommand(source, cmd.toString());
                    } catch (Exception e) {
                        CobblePass.LOGGER.error("Failed to grant Pokemon reward: " + e.getMessage());
                    }
                }
                break;
            case COMMAND:
                if (command != null && !command.isEmpty()) {
                    // Replace placeholders in command
                    String finalCommand = command
                        .replace("%player%", player.getName().getString())
                        .replace("%uuid%", player.getUUID().toString());
                    
                    // Always execute commands as server to ensure proper permissions
                    CommandSourceStack source = player.getServer().createCommandSourceStack();
                    player.getServer().getCommands().performPrefixedCommand(source, finalCommand);
                }
                break;
        }
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("type", type.name());
        json.add("data", data);
        // Only include command for COMMAND type rewards
        if (type == RewardType.COMMAND && command != null) {
            json.addProperty("command", command);
        }
        if (this.lockedDisplay != null) {
            json.add("lockedDisplay", this.lockedDisplay);
        }
        if (this.claimedDisplay != null) {
            json.add("claimedDisplay", this.claimedDisplay);
        }
        if (this.claimableDisplay != null) {
            json.add("claimableDisplay", this.claimableDisplay);
        }
        return json;
    }

    public static Reward fromJson(JsonObject json) {
        RewardType type = RewardType.fromString(json.get("type").getAsString());
        JsonObject data = json.get("data").getAsJsonObject();
        String command = json.has("command") ? json.get("command").getAsString() : null;
        JsonObject lockedDisplay = json.has("lockedDisplay") ? json.get("lockedDisplay").getAsJsonObject() : null;
        JsonObject claimedDisplay = json.has("claimedDisplay") ? json.get("claimedDisplay").getAsJsonObject() : null;
        JsonObject claimableDisplay = json.has("claimableDisplay") ? json.get("claimableDisplay").getAsJsonObject() : null;
        return new Reward(type, data, command, lockedDisplay, claimedDisplay, claimableDisplay);
    }

    // Factory methods for different reward types
    public static Reward item(JsonObject nbtData) {
        return new Reward(RewardType.ITEM, nbtData, null, null, null, null);
    }

    public static Reward pokemon(JsonObject pokemonData) {
        return new Reward(RewardType.POKEMON, pokemonData, null, null, null, null);
    }

    public static Reward command(String commandData, String displayId, String displayName) {
        JsonObject data = new JsonObject();
        data.addProperty("id", displayId); // Item to show in UI
        data.addProperty("display_name", displayName); // Custom name to show in UI
        return new Reward(RewardType.COMMAND, data, commandData, null, null, null);
    }
}
