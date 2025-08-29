package com.cobblemon.mdks.cobblepass.config;

import java.util.Arrays;

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
            migrateGuiIfNeeded();
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
        
        // Create XP Info ingredient with proper type for dynamic XP values
        GuiIngredient infoIngredient = new GuiIngredient();
        infoIngredient.setType(GuiIngredient.IngredientType.XP_INFO_PLACEHOLDER);
        infoIngredient.setMaterial("minecraft:experience_bottle");
        infoIngredient.setName("lang.gui.info.title");
        infoIngredient.setLore(Arrays.asList(
                "lang.gui.info.lore.catch",
                "lang.gui.info.lore.defeat",
                "lang.gui.info.lore.evolve",
                "lang.gui.info.lore.hatch",
                "lang.gui.info.lore.trade",
                "lang.gui.info.lore.fish",
                "lang.gui.info.lore.release",
                "lang.gui.info.lore.catch_legendary",
                "lang.gui.info.lore.catch_shiny",
                "lang.gui.info.lore.catch_ultrabeast",
                "lang.gui.info.lore.catch_mythical",
                "lang.gui.info.lore.catch_paradox"
        ));
        this.structure.getIngredients().put('i', infoIngredient);
    }

    private void migrateGuiIfNeeded() {
        GuiIngredient xpInfo = this.structure.getIngredients().get('i');
        if (xpInfo != null && (xpInfo.getLore() == null || xpInfo.getLore().isEmpty())) {
            xpInfo.setName("lang.gui.info.title");
            xpInfo.setLore(Arrays.asList(
                    "lang.gui.info.lore.catch",
                    "lang.gui.info.lore.defeat",
                    "lang.gui.info.lore.evolve",
                    "lang.gui.info.lore.hatch",
                    "lang.gui.info.lore.trade",
                    "lang.gui.info.lore.fish",
                    "lang.gui.info.lore.release",
                    "lang.gui.info.lore.catch_legendary",
                    "lang.gui.info.lore.catch_shiny",
                    "lang.gui.info.lore.catch_ultrabeast",
                    "lang.gui.info.lore.catch_mythical",
                    "lang.gui.info.lore.catch_paradox"
            ));
            this.structure.getIngredients().put('i', xpInfo);
            save();
            CobblePass.LOGGER.info("Migrated GUI configuration to include new XP sources.");
        }
    }

    private void validateConfiguration() {
        // Validate that essential placeholders exist
        if (structure == null) {
            CobblePass.LOGGER.warn("GUI structure is null, regenerating default");
            structure = new GuiStructure();
            return;
        }

        // Use comprehensive validation
        GuiValidator.ValidationResult validation = GuiValidator.validateComplete(
            structure.getStructure(), 
            structure.getIngredients()
        );
        
        // Log validation results
        if (!validation.isValid()) {
            CobblePass.LOGGER.error("GUI configuration validation failed:");
            for (String error : validation.getErrors()) {
                CobblePass.LOGGER.error("  - " + error);
            }
            
            // Generate recovery instructions
            String recoveryInstructions = GuiValidator.generateRecoveryInstructions(validation);
            CobblePass.LOGGER.error("Recovery instructions:\n" + recoveryInstructions);
            
            // Fall back to default structure
            CobblePass.LOGGER.warn("Falling back to default GUI structure due to validation errors");
            structure = new GuiStructure();
        } else if (validation.hasWarnings()) {
            CobblePass.LOGGER.warn("GUI configuration validation warnings:");
            for (String warning : validation.getWarnings()) {
                CobblePass.LOGGER.warn("  - " + warning);
            }
        } else {
            CobblePass.LOGGER.info("GUI configuration validation passed successfully");
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
