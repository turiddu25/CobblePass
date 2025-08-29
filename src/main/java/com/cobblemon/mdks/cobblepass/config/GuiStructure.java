package com.cobblemon.mdks.cobblepass.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class GuiStructure {
    private static final int GUI_ROWS = 6;
    private static final int GUI_COLS = 9;
    
    private List<String> structure;
    private Map<Character, GuiIngredient> ingredients;

    public GuiStructure() {
        this.structure = new ArrayList<>();
        this.ingredients = new HashMap<>();
        generateDefault();
    }

    public static GuiStructure fromJson(JsonObject json) {
        GuiStructure guiStructure = new GuiStructure();
        
        // Load structure using the new parser
        if (json.has("structure") && json.get("structure").isJsonArray()) {
            JsonArray structureArray = json.getAsJsonArray("structure");
            List<String> rawStructure = new ArrayList<>();
            
            // Convert JsonArray to List<String>
            for (int i = 0; i < structureArray.size(); i++) {
                rawStructure.add(structureArray.get(i).getAsString());
            }
            
            // Parse and normalize using GuiStructureParser
            guiStructure.structure = GuiStructureParser.parse(rawStructure);
            
            // Validate the parsed structure
            GuiStructureParser.ValidationResult validation = GuiStructureParser.validateStructure(guiStructure.structure);
            if (!validation.isValid()) {
                com.cobblemon.mdks.cobblepass.CobblePass.LOGGER.error("GUI structure validation failed:");
                for (String error : validation.getErrors()) {
                    com.cobblemon.mdks.cobblepass.CobblePass.LOGGER.error("  - " + error);
                }
                // Fall back to default structure
                guiStructure.generateDefault();
            } else if (validation.hasWarnings()) {
                com.cobblemon.mdks.cobblepass.CobblePass.LOGGER.warn("GUI structure validation warnings:");
                for (String warning : validation.getWarnings()) {
                    com.cobblemon.mdks.cobblepass.CobblePass.LOGGER.warn("  - " + warning);
                }
            }
        } else {
            // No structure provided, use default
            guiStructure.generateDefault();
        }

        // Load ingredients
        if (json.has("ingredients") && json.get("ingredients").isJsonObject()) {
            JsonObject ingredientsJson = json.getAsJsonObject("ingredients");
            guiStructure.ingredients = new HashMap<>();
            
            for (String key : ingredientsJson.keySet()) {
                if (key.length() == 1) {
                    char character = key.charAt(0);
                    JsonObject ingredientJson = ingredientsJson.getAsJsonObject(key);
                    GuiIngredient ingredient = GuiIngredient.fromJson(ingredientJson);
                    guiStructure.ingredients.put(character, ingredient);
                }
            }
        }

        return guiStructure;
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        
        // Save structure
        JsonArray structureArray = new JsonArray();
        for (String row : structure) {
            structureArray.add(row);
        }
        json.add("structure", structureArray);
        
        // Save ingredients
        JsonObject ingredientsJson = new JsonObject();
        for (Map.Entry<Character, GuiIngredient> entry : ingredients.entrySet()) {
            ingredientsJson.add(String.valueOf(entry.getKey()), entry.getValue().toJson());
        }
        json.add("ingredients", ingredientsJson);
        
        return json;
    }

    private void generateDefault() {
        // Create default 6x9 structure in compact format
        structure = List.of(
            "#i #B# P#",
            "         ",
            "#L fffff #",
            "#M rrrrr #",
            "         ",
            "<   #   >"
        );

        // Create default ingredients
        ingredients = new HashMap<>();
        
        // Border decoration
        GuiIngredient border = new GuiIngredient();
        border.setType(GuiIngredient.IngredientType.STATIC_ITEM);
        border.setMaterial("minecraft:light_gray_stained_glass_pane");
        border.setName("lang.gui.decoration.border");
        border.setHideTooltip(true);
        ingredients.put('#', border);

        // Empty space is now just a space character ' ' in the structure string
        // and doesn't need a dedicated ingredient definition.

        // Special placeholders with proper materials
        GuiIngredient xpInfo = new GuiIngredient();
        xpInfo.setType(GuiIngredient.IngredientType.XP_INFO_PLACEHOLDER);
        xpInfo.setMaterial("minecraft:experience_bottle");
        xpInfo.setName("");
        xpInfo.setHideTooltip(false);
        ingredients.put('i', xpInfo);

        GuiIngredient progress = new GuiIngredient();
        progress.setType(GuiIngredient.IngredientType.PROGRESS_PLACEHOLDER);
        progress.setMaterial("minecraft:nether_star");
        progress.setName("");
        progress.setHideTooltip(false);
        ingredients.put('B', progress);

        GuiIngredient premiumStatus = new GuiIngredient();
        premiumStatus.setType(GuiIngredient.IngredientType.PREMIUM_STATUS_PLACEHOLDER);
        premiumStatus.setMaterial("cobblemon:master_ball");
        premiumStatus.setName("");
        premiumStatus.setHideTooltip(false);
        ingredients.put('P', premiumStatus);

        GuiIngredient freeLabel = new GuiIngredient();
        freeLabel.setType(GuiIngredient.IngredientType.FREE_REWARDS_LABEL);
        freeLabel.setMaterial("cobblemon:premier_ball");
        freeLabel.setName("lang.gui.reward.free_label");
        freeLabel.setHideTooltip(false);
        ingredients.put('L', freeLabel);

        GuiIngredient premiumLabel = new GuiIngredient();
        premiumLabel.setType(GuiIngredient.IngredientType.PREMIUM_REWARDS_LABEL);
        premiumLabel.setMaterial("cobblemon:master_ball");
        premiumLabel.setName("lang.gui.reward.premium_label");
        premiumLabel.setHideTooltip(false);
        ingredients.put('M', premiumLabel);

        GuiIngredient freeReward = new GuiIngredient();
        freeReward.setType(GuiIngredient.IngredientType.FREE_REWARD_PLACEHOLDER);
        freeReward.setMaterial("minecraft:chest"); // Default fallback for free rewards
        freeReward.setName("lang.gui.reward.free_placeholder");
        freeReward.setHideTooltip(false);
        ingredients.put('f', freeReward);

        GuiIngredient premiumReward = new GuiIngredient();
        premiumReward.setType(GuiIngredient.IngredientType.PREMIUM_REWARD_PLACEHOLDER);
        premiumReward.setMaterial("minecraft:ender_chest"); // Default fallback for premium rewards
        premiumReward.setName("lang.gui.reward.premium_placeholder");
        premiumReward.setHideTooltip(false);
        ingredients.put('r', premiumReward);

        // Remove status ingredient - no longer needed as status is integrated into reward lore

        GuiIngredient navPrev = new GuiIngredient();
        navPrev.setType(GuiIngredient.IngredientType.NAVIGATION_PREVIOUS);
        navPrev.setMaterial("minecraft:arrow");
        navPrev.setName("lang.gui.navigation.previous");
        navPrev.setHideTooltip(false);
        ingredients.put('<', navPrev);

        GuiIngredient navNext = new GuiIngredient();
        navNext.setType(GuiIngredient.IngredientType.NAVIGATION_NEXT);
        navNext.setMaterial("minecraft:arrow");
        navNext.setName("lang.gui.navigation.next");
        navNext.setHideTooltip(false);
        ingredients.put('>', navNext);
    }

    public char getCharAt(int row, int col) {
        if (row < 0 || row >= structure.size() || col < 0 || col >= GUI_COLS) {
            return ' '; // Default empty
        }
        
        String rowString = structure.get(row);
        if (col >= rowString.length()) {
            return ' ';
        }
        
        return rowString.charAt(col);
    }

    public GuiIngredient getIngredientAt(int row, int col) {
        char character = getCharAt(row, col);
        return ingredients.get(character);
    }

    public List<GuiStructure.SlotInfo> findPlaceholderSlots(GuiIngredient.IngredientType type) {
        List<SlotInfo> slots = new ArrayList<>();
        
        for (int row = 0; row < GUI_ROWS; row++) {
            for (int col = 0; col < GUI_COLS; col++) {
                GuiIngredient ingredient = getIngredientAt(row, col);
                if (ingredient != null && ingredient.getType() == type) {
                    slots.add(new SlotInfo(row, col, row * GUI_COLS + col));
                }
            }
        }
        
        return slots;
    }

    public static class SlotInfo {
        private final int row;
        private final int col;
        private final int slotIndex;

        public SlotInfo(int row, int col, int slotIndex) {
            this.row = row;
            this.col = col;
            this.slotIndex = slotIndex;
        }

        public int getRow() { return row; }
        public int getCol() { return col; }
        public int getSlotIndex() { return slotIndex; }
    }

    // Getters and setters
    public List<String> getStructure() {
        return structure;
    }

    public void setStructure(List<String> structure) {
        this.structure = structure;
    }

    public Map<Character, GuiIngredient> getIngredients() {
        return ingredients;
    }

    public void setIngredients(Map<Character, GuiIngredient> ingredients) {
        this.ingredients = ingredients;
    }

    public static int getGuiRows() {
        return GUI_ROWS;
    }

    public static int getGuiCols() {
        return GUI_COLS;
    }
}
