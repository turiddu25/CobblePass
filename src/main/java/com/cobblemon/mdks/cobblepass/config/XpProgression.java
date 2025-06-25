package com.cobblemon.mdks.cobblepass.config;

import com.cobblemon.mdks.cobblepass.CobblePass;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
import java.util.HashMap;
import java.util.Map;

public class XpProgression {
    private String mode;
    private int xpPerLevel;
    private double xpMultiplier;
    private Map<Integer, Integer> manualXpValues;

    public XpProgression() {
        this.mode = "FORMULA";
        this.xpPerLevel = 1000;
        this.xpMultiplier = 1.1;
        this.manualXpValues = new HashMap<>();
    }

    public String getMode() {
        return mode;
    }

    public int getXpPerLevel() {
        return xpPerLevel;
    }

    public double getXpMultiplier() {
        return xpMultiplier;
    }

    public int getManualXpForLevel(int level) {
        // If a level isn't defined, return a huge number to prevent leveling up
        // and log an error to the console.
        if (!manualXpValues.containsKey(level)) {
            CobblePass.LOGGER.warn("XP for level " + level + " is not defined in manualXpValues in config.json! Please define it.");
            return Integer.MAX_VALUE;
        }
        int xp = manualXpValues.getOrDefault(level, Integer.MAX_VALUE);
        if (xp <= 0) {
            CobblePass.LOGGER.warn("XP for level " + level + " is configured as " + xp + ", which is not allowed. Please set a positive value.");
            return Integer.MAX_VALUE;
        }
        return xp;
    }

    public void fromJson(JsonObject json) {
        if (json.has("mode")) {
            this.mode = json.get("mode").getAsString();
        }
        if (json.has("xpPerLevel")) {
            this.xpPerLevel = json.get("xpPerLevel").getAsInt();
        }
        if (json.has("xpMultiplier")) {
            this.xpMultiplier = json.get("xpMultiplier").getAsDouble();
        }
        if (json.has("manualXpValues") && json.get("manualXpValues").isJsonObject()) {
            JsonObject manualValues = json.getAsJsonObject("manualXpValues");
            for (Map.Entry<String, JsonElement> entry : manualValues.entrySet()) {
                manualXpValues.put(Integer.parseInt(entry.getKey()), entry.getValue().getAsInt());
            }
        }
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("mode", mode);
        json.addProperty("xpPerLevel", xpPerLevel);
        json.addProperty("xpMultiplier", xpMultiplier);
        JsonObject manualValues = new JsonObject();
        for (Map.Entry<Integer, Integer> entry : manualXpValues.entrySet()) {
            manualValues.addProperty(entry.getKey().toString(), entry.getValue());
        }
        json.add("manualXpValues", manualValues);
        return json;
    }
}