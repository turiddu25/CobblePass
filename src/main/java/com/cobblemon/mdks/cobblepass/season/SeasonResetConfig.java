package com.cobblemon.mdks.cobblepass.season;

import com.google.gson.JsonObject;

public class SeasonResetConfig {
    private boolean resetLevels;
    private boolean resetXP;
    private boolean resetClaimedRewards;
    private PremiumPreservationMode premiumPreservationMode;

    public SeasonResetConfig() {
        setDefaults();
    }

    private void setDefaults() {
        this.resetLevels = true;
        this.resetXP = true;
        this.resetClaimedRewards = true;
        this.premiumPreservationMode = PremiumPreservationMode.PRESERVE_ALL;
    }

    public void fromJson(JsonObject json) {
        if (json.has("resetLevels")) {
            this.resetLevels = json.get("resetLevels").getAsBoolean();
        }
        if (json.has("resetXP")) {
            this.resetXP = json.get("resetXP").getAsBoolean();
        }
        if (json.has("resetClaimedRewards")) {
            this.resetClaimedRewards = json.get("resetClaimedRewards").getAsBoolean();
        }
        if (json.has("premiumPreservationMode")) {
            this.premiumPreservationMode = PremiumPreservationMode.fromConfigValue(json.get("premiumPreservationMode").getAsString());
        }
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("resetLevels", resetLevels);
        json.addProperty("resetXP", resetXP);
        json.addProperty("resetClaimedRewards", resetClaimedRewards);
        json.addProperty("premiumPreservationMode", premiumPreservationMode.getConfigValue());
        return json;
    }

    public boolean validate() {
        return true;
    }

    public boolean isResetLevels() {
        return resetLevels;
    }

    public boolean isResetXP() {
        return resetXP;
    }

    public boolean isResetClaimedRewards() {
        return resetClaimedRewards;
    }

    public PremiumPreservationMode getPremiumPreservationMode() {
        return premiumPreservationMode;
    }
}