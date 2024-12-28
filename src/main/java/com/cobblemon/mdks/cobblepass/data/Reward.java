package com.cobblemon.mdks.cobblepass.data;

import com.google.gson.JsonObject;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class Reward {
    private final RewardType type;
    private final String data;
    private final String command;

    public Reward(RewardType type, String data, String command) {
        this.type = type;
        this.data = data;
        this.command = command;
    }

    public RewardType getType() {
        return type;
    }

    public String getData() {
        return data;
    }

    public String getCommand() {
        return command;
    }

    public ItemStack getItemStack(RegistryAccess registryAccess) {
        if (type != RewardType.MINECRAFT_ITEM && type != RewardType.COBBLEMON_ITEM) {
            return ItemStack.EMPTY;
        }

        try {
            CompoundTag tag = TagParser.parseTag(data);
            return ItemStack.parse(registryAccess, tag).orElse(ItemStack.EMPTY);
        } catch (Exception e) {
            return ItemStack.EMPTY;
        }
    }

    public void grant(ServerPlayer player) {
        switch (type) {
            case MINECRAFT_ITEM:
            case COBBLEMON_ITEM:
                ItemStack item = getItemStack(player.level().registryAccess());
                if (!item.isEmpty()) {
                    player.getInventory().add(item);
                }
                break;
            case POKEMON:
            case COMMAND:
                if (command != null && !command.isEmpty()) {
                    // Replace placeholders in command
                    String finalCommand = command
                        .replace("%player%", player.getName().getString())
                        .replace("%uuid%", player.getUUID().toString());
                    
                    // Execute command as player for Pokemon rewards, as server for others
                    CommandSourceStack source = type == RewardType.POKEMON ? 
                        player.createCommandSourceStack() : 
                        player.getServer().createCommandSourceStack();
                    
                    player.getServer().getCommands().performPrefixedCommand(
                        source,
                        finalCommand
                    );
                }
                break;
        }
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("type", type.name());
        json.addProperty("data", data);
        if (command != null) {
            json.addProperty("command", command);
        }
        return json;
    }

    public static Reward fromJson(JsonObject json) {
        RewardType type = RewardType.fromString(json.get("type").getAsString());
        String data = json.get("data").getAsString();
        String command = json.has("command") ? json.get("command").getAsString() : null;
        return new Reward(type, data, command);
    }

    // Factory methods for different reward types
    public static Reward minecraftItem(String nbtData) {
        return new Reward(RewardType.MINECRAFT_ITEM, nbtData, null);
    }

    public static Reward cobblemonItem(String nbtData) {
        return new Reward(RewardType.COBBLEMON_ITEM, nbtData, null);
    }

    public static Reward pokemon(String pokemonData, String spawnCommand) {
        return new Reward(RewardType.POKEMON, pokemonData, spawnCommand);
    }

    public static Reward command(String commandData) {
        return new Reward(RewardType.COMMAND, "", commandData);
    }
}
