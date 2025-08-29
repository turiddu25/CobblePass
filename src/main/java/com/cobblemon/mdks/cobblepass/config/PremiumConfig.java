package com.cobblemon.mdks.cobblepass.config;

import com.cobblemon.mdks.cobblepass.premium.PremiumMode;
import com.cobblemon.mdks.cobblepass.util.Constants;
import com.google.gson.JsonObject;

public class PremiumConfig {
    private PremiumMode mode;
    private String permissionNode;
    private boolean economyEnabled;
    private long premiumCost;
    private boolean autoRenew;
    private boolean preserveOnSeasonChange;

    public PremiumConfig() {
        setDefaults();
    }

    private void setDefaults() {
        this.mode = PremiumMode.ECONOMY;
        this.permissionNode = "cobblepass.premium";
        this.economyEnabled = true;
        this.premiumCost = Constants.DEFAULT_PREMIUM_COST;
        this.autoRenew = false;
        this.preserveOnSeasonChange = true;
    }

    public void fromJson(JsonObject json) {
        if (json.has("mode")) {
            this.mode = PremiumMode.fromConfigValue(json.get("mode").getAsString());
        }
        if (json.has("permissionNode")) {
            this.permissionNode = json.get("permissionNode").getAsString();
        }
        if (json.has("economyEnabled")) {
            this.economyEnabled = json.get("economyEnabled").getAsBoolean();
        }
        if (json.has("premiumCost")) {
            this.premiumCost = json.get("premiumCost").getAsLong();
        }
        if (json.has("autoRenew")) {
            this.autoRenew = json.get("autoRenew").getAsBoolean();
        }
        if (json.has("preserveOnSeasonChange")) {
            this.preserveOnSeasonChange = json.get("preserveOnSeasonChange").getAsBoolean();
        }
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("mode", mode.getConfigValue());
        json.addProperty("permissionNode", permissionNode);
        json.addProperty("economyEnabled", economyEnabled);
        json.addProperty("premiumCost", premiumCost);
        json.addProperty("autoRenew", autoRenew);
        json.addProperty("preserveOnSeasonChange", preserveOnSeasonChange);
        return json;
    }

    public PremiumMode getMode() {
        return mode;
    }

    public String getPermissionNode() {
        return permissionNode;
    }

    public boolean isEconomyEnabled() {
        return economyEnabled;
    }

    public long getPremiumCost() {
        return premiumCost;
    }

    public boolean isAutoRenew() {
        return autoRenew;
    }

    public boolean isPreserveOnSeasonChange() {
        return preserveOnSeasonChange;
    }

    public void setMode(PremiumMode mode) {
        this.mode = mode;
    }

    public void setPermissionNode(String permissionNode) {
        this.permissionNode = permissionNode;
    }

    public void setEconomyEnabled(boolean economyEnabled) {
        this.economyEnabled = economyEnabled;
    }

    public void setPremiumCost(long premiumCost) {
        this.premiumCost = premiumCost;
    }

    public void setAutoRenew(boolean autoRenew) {
        this.autoRenew = autoRenew;
    }

    public void setPreserveOnSeasonChange(boolean preserveOnSeasonChange) {
        this.preserveOnSeasonChange = preserveOnSeasonChange;
    }
}