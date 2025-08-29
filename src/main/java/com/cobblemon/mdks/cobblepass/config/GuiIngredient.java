package com.cobblemon.mdks.cobblepass.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.util.Unit;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class GuiIngredient {
    public enum IngredientType {
        STATIC_ITEM,
        FREE_REWARD_PLACEHOLDER,
        PREMIUM_REWARD_PLACEHOLDER,
        STATUS_PLACEHOLDER,
        XP_INFO_PLACEHOLDER,
        PROGRESS_PLACEHOLDER,
        PREMIUM_STATUS_PLACEHOLDER,
        NAVIGATION_PREVIOUS,
        NAVIGATION_NEXT,
        FREE_REWARDS_LABEL,
        PREMIUM_REWARDS_LABEL,
        CUSTOM_ITEM,                // Custom display item with optional command
        COMMAND_BUTTON             // Dedicated command execution button
    }

    private IngredientType type;
    private String material;
    private String name;
    private List<String> lore;
    private int customModelData;
    private boolean hideTooltip;
    private String command;                // Command to execute on click
    private String permission;             // Required permission to click
    private boolean executeAsPlayer;       // Execute as player (true) or server (false)
    private String clickMessage;           // Message to show when clicked
    private String noPermissionMessage;    // Message when lacking permission

    public GuiIngredient() {
        this.type = IngredientType.STATIC_ITEM;
        this.material = "minecraft:gray_stained_glass_pane";
        this.name = "";
        this.lore = new ArrayList<>();
        this.customModelData = 0;
        this.hideTooltip = true;
        this.command = null;
        this.permission = null;
        this.executeAsPlayer = true;
        this.clickMessage = null;
        this.noPermissionMessage = "§cYou don't have permission to use this!";
    }

    public static GuiIngredient fromJson(JsonObject json) {
        GuiIngredient ingredient = new GuiIngredient();
        
        if (json.has("type")) {
            try {
                ingredient.type = IngredientType.valueOf(json.get("type").getAsString().toUpperCase());
            } catch (IllegalArgumentException e) {
                ingredient.type = IngredientType.STATIC_ITEM;
            }
        }

        if (json.has("material")) {
            ingredient.material = json.get("material").getAsString();
        }

        if (json.has("name")) {
            ingredient.name = json.get("name").getAsString();
        }

        if (json.has("lore") && json.get("lore").isJsonArray()) {
            JsonArray loreArray = json.getAsJsonArray("lore");
            ingredient.lore = new ArrayList<>();
            for (int i = 0; i < loreArray.size(); i++) {
                ingredient.lore.add(loreArray.get(i).getAsString());
            }
        }

        if (json.has("customModelData")) {
            ingredient.customModelData = json.get("customModelData").getAsInt();
        }

        if (json.has("hideTooltip")) {
            ingredient.hideTooltip = json.get("hideTooltip").getAsBoolean();
        }

        // Load command-related properties
        if (json.has("command")) {
            ingredient.command = json.get("command").getAsString();
        }

        if (json.has("permission")) {
            ingredient.permission = json.get("permission").getAsString();
        }

        if (json.has("executeAsPlayer")) {
            ingredient.executeAsPlayer = json.get("executeAsPlayer").getAsBoolean();
        }

        if (json.has("clickMessage")) {
            ingredient.clickMessage = json.get("clickMessage").getAsString();
        }

        if (json.has("noPermissionMessage")) {
            ingredient.noPermissionMessage = json.get("noPermissionMessage").getAsString();
        }

        return ingredient;
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("type", type.name());
        json.addProperty("material", material);
        json.addProperty("name", name);
        
        JsonArray loreArray = new JsonArray();
        for (String loreLine : lore) {
            loreArray.add(loreLine);
        }
        json.add("lore", loreArray);
        
        json.addProperty("customModelData", customModelData);
        json.addProperty("hideTooltip", hideTooltip);

        // Save command-related properties
        if (command != null && !command.isEmpty()) {
            json.addProperty("command", command);
        }
        if (permission != null && !permission.isEmpty()) {
            json.addProperty("permission", permission);
        }
        json.addProperty("executeAsPlayer", executeAsPlayer);
        if (clickMessage != null && !clickMessage.isEmpty()) {
            json.addProperty("clickMessage", clickMessage);
        }
        if (noPermissionMessage != null && !noPermissionMessage.isEmpty()) {
            json.addProperty("noPermissionMessage", noPermissionMessage);
        }
        
        return json;
    }

    public ItemStack createItemStack() {
        // Parse material string to get item
        ItemStack stack;
        try {
            // Handle empty or null material
            if (material == null || material.trim().isEmpty()) {
                stack = getDefaultItemForType();
            } else {
                ResourceLocation itemId = ResourceLocation.parse(material);
                var item = BuiltInRegistries.ITEM.get(itemId);
                if (item == Items.AIR) {
                    stack = getDefaultItemForType();
                } else {
                    stack = new ItemStack(item);
                }
            }
        } catch (Exception e) {
            // Log the error for debugging
            com.cobblemon.mdks.cobblepass.CobblePass.LOGGER.warn("Invalid material '{}' for GUI ingredient type {}, using default", material, type);
            stack = getDefaultItemForType();
        }

        // Set custom name if provided
        if (name != null && !name.isEmpty()) {
            stack.set(DataComponents.CUSTOM_NAME, Component.literal(name));
        }

        // Set lore if provided
        if (lore != null && !lore.isEmpty()) {
            List<Component> loreComponents = new ArrayList<>();
            for (String loreLine : lore) {
                loreComponents.add(Component.literal(loreLine));
            }
            stack.set(DataComponents.LORE, new ItemLore(loreComponents));
        }

        // Set custom model data if provided
        if (customModelData > 0) {
            stack.set(DataComponents.CUSTOM_MODEL_DATA, new net.minecraft.world.item.component.CustomModelData(customModelData));
        }

        // Hide tooltip if specified
        if (hideTooltip) {
            stack.set(DataComponents.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE);
        }

        return stack;
    }

    /**
     * Gets a sensible default item based on the ingredient type
     */
    private ItemStack getDefaultItemForType() {
        return switch (type) {
            case FREE_REWARD_PLACEHOLDER -> new ItemStack(Items.CHEST);
            case PREMIUM_REWARD_PLACEHOLDER -> new ItemStack(Items.ENDER_CHEST);
            case STATUS_PLACEHOLDER -> new ItemStack(Items.BOOK);
            case XP_INFO_PLACEHOLDER -> new ItemStack(Items.EXPERIENCE_BOTTLE);
            case PROGRESS_PLACEHOLDER -> new ItemStack(Items.NETHER_STAR);
            case PREMIUM_STATUS_PLACEHOLDER -> new ItemStack(Items.DIAMOND);
            case NAVIGATION_PREVIOUS, NAVIGATION_NEXT -> new ItemStack(Items.ARROW);
            case FREE_REWARDS_LABEL -> new ItemStack(Items.CHEST);
            case PREMIUM_REWARDS_LABEL -> new ItemStack(Items.ENDER_CHEST);
            case CUSTOM_ITEM -> new ItemStack(Items.PAPER);
            case COMMAND_BUTTON -> new ItemStack(Items.STONE_BUTTON);
            default -> new ItemStack(Items.GRAY_STAINED_GLASS_PANE);
        };
    }

    // Getters and setters
    public IngredientType getType() {
        return type;
    }

    public void setType(IngredientType type) {
        this.type = type;
    }

    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getLore() {
        return lore;
    }

    public void setLore(List<String> lore) {
        this.lore = lore;
    }

    public int getCustomModelData() {
        return customModelData;
    }

    public void setCustomModelData(int customModelData) {
        this.customModelData = customModelData;
    }

    public boolean isHideTooltip() {
        return hideTooltip;
    }

    public void setHideTooltip(boolean hideTooltip) {
        this.hideTooltip = hideTooltip;
    }

    // New getters and setters for command functionality
    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public boolean isExecuteAsPlayer() {
        return executeAsPlayer;
    }

    public void setExecuteAsPlayer(boolean executeAsPlayer) {
        this.executeAsPlayer = executeAsPlayer;
    }

    public String getClickMessage() {
        return clickMessage;
    }

    public void setClickMessage(String clickMessage) {
        this.clickMessage = clickMessage;
    }

    public String getNoPermissionMessage() {
        return noPermissionMessage;
    }

    public void setNoPermissionMessage(String noPermissionMessage) {
        this.noPermissionMessage = noPermissionMessage;
    }

    /**
     * Checks if this ingredient has a command to execute
     */
    public boolean hasCommand() {
        return command != null && !command.isEmpty();
    }

    /**
     * Checks if this ingredient is clickable (has command or is interactive type)
     */
    public boolean isClickable() {
        return hasCommand() ||
               type == IngredientType.COMMAND_BUTTON ||
               type == IngredientType.CUSTOM_ITEM ||
               type == IngredientType.NAVIGATION_PREVIOUS ||
               type == IngredientType.NAVIGATION_NEXT ||
               type == IngredientType.PREMIUM_STATUS_PLACEHOLDER;
    }
}