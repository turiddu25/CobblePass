package com.cobblemon.mdks.cobblepass.config;

import com.cobblemon.mdks.cobblepass.CobblePass;
import com.cobblemon.mdks.cobblepass.util.Constants;
import com.cobblemon.mdks.cobblepass.util.Utils;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class GuiConfig {
    private GuiStructure structure;
    private String title;
    private boolean enableCustomGui;

    public GuiConfig() {
        this.structure = new GuiStructure();
        this.title = "lang.gui.title";
        this.enableCustomGui = true;
    }

    public void load() {
        // Ensure config directory exists
        Utils.checkForDirectory("/" + Constants.CONFIG_PATH);
        
        String content = Utils.readFileSync(Constants.CONFIG_PATH, Constants.GUI_FILE);
        if (content == null || content.isEmpty()) {
            CobblePass.LOGGER.info("No gui.json found, generating default GUI configuration");
            generateDefault();
            save();
            return;
        }

        try {
            JsonObject json = JsonParser.parseString(content).getAsJsonObject();
            loadFromJson(json);
            validateConfiguration();
            CobblePass.LOGGER.info("Loaded GUI configuration from gui.json");
        } catch (Exception e) {
            CobblePass.LOGGER.error("Failed to load gui.json, using default configuration", e);
            generateDefault();
            save();
        }
    }

    private void loadFromJson(JsonObject json) {
        // Load basic settings
        if (json.has("title")) {
            this.title = json.get("title").getAsString();
        }

        if (json.has("enableCustomGui")) {
            this.enableCustomGui = json.get("enableCustomGui").getAsBoolean();
        }

        // Load structure
        if (json.has("structure")) {
            this.structure = GuiStructure.fromJson(json.getAsJsonObject("structure"));
        } else {
            this.structure = new GuiStructure();
        }
    }

    private void generateDefault() {
        this.structure = new GuiStructure();
        this.title = "lang.gui.title";
        this.enableCustomGui = true;
    }

    private void validateConfiguration() {
        // Validate that essential placeholders exist
        if (structure == null) {
            CobblePass.LOGGER.warn("GUI structure is null, regenerating default");
            structure = new GuiStructure();
            return;
        }

        // Check for essential placeholder types
        boolean hasRewardSlots = !structure.findPlaceholderSlots(GuiIngredient.IngredientType.FREE_REWARD_PLACEHOLDER).isEmpty() ||
                                !structure.findPlaceholderSlots(GuiIngredient.IngredientType.PREMIUM_REWARD_PLACEHOLDER).isEmpty();
        
        if (!hasRewardSlots) {
            CobblePass.LOGGER.warn("GUI configuration has no reward slots, this may cause issues");
        }

        // Validate title
        if (title == null || title.isEmpty()) {
            CobblePass.LOGGER.warn("GUI title is empty, using default");
            title = "lang.gui.title";
        }
    }

    public void save() {
        JsonObject json = new JsonObject();
        json.addProperty("title", title);
        json.addProperty("enableCustomGui", enableCustomGui);
        json.add("structure", structure.toJson());

        Utils.writeFileSync(Constants.CONFIG_PATH, Constants.GUI_FILE,
                Utils.newGson().toJson(json));
    }

    public GuiStructure getStructure() {
        return structure;
    }

    public void setStructure(GuiStructure structure) {
        this.structure = structure;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isEnableCustomGui() {
        return enableCustomGui;
    }

    public void setEnableCustomGui(boolean enableCustomGui) {
        this.enableCustomGui = enableCustomGui;
    }

    public void reload() {
        load();
    }
}