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
        PREMIUM_REWARDS_LABEL
    }

    private IngredientType type;
    private String material;
    private String name;
    private List<String> lore;
    private int customModelData;
    private boolean hideTooltip;

    public GuiIngredient() {
        this.type = IngredientType.STATIC_ITEM;
        this.material = "minecraft:gray_stained_glass_pane";
        this.name = "";
        this.lore = new ArrayList<>();
        this.customModelData = 0;
        this.hideTooltip = true;
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
        
        return json;
    }

    public ItemStack createItemStack() {
        // Parse material string to get item
        ItemStack stack;
        try {
            ResourceLocation itemId = ResourceLocation.parse(material);
            var item = BuiltInRegistries.ITEM.get(itemId);
            if (item == Items.AIR) {
                stack = new ItemStack(Items.GRAY_STAINED_GLASS_PANE);
            } else {
                stack = new ItemStack(item);
            }
        } catch (Exception e) {
            stack = new ItemStack(Items.GRAY_STAINED_GLASS_PANE);
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
}