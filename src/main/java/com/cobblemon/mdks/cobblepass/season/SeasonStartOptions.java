package com.cobblemon.mdks.cobblepass.season;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SeasonStartOptions {
    private List<UUID> preservedPremiumPlayers;
    private PremiumPreservationMode premiumRestorationMode;
    private String seasonId;
    private String seasonName;
    private int maxLevel;
    private boolean restorePremiumStatus;
    private boolean broadcastStartMessage;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Map<String, Object> customProperties;

    public SeasonStartOptions(List<UUID> preservedPremiumPlayers) {
        this.preservedPremiumPlayers = preservedPremiumPlayers != null ? preservedPremiumPlayers : new ArrayList<>();
        this.premiumRestorationMode = PremiumPreservationMode.PRESERVE_ALL;
        this.seasonId = "default";
        this.seasonName = "Default Season";
        this.maxLevel = 100;
        this.restorePremiumStatus = true;
        this.broadcastStartMessage = true;
        this.startTime = LocalDateTime.now();
        this.customProperties = new HashMap<>();
    }

    public SeasonStartOptions() {
        this(new ArrayList<>());
    }

    public SeasonStartOptions(String seasonId, String seasonName) {
        this();
        this.seasonId = seasonId;
        this.seasonName = seasonName;
    }

    public List<UUID> getPreservedPremiumPlayers() {
        return preservedPremiumPlayers;
    }

    public PremiumPreservationMode getPremiumRestorationMode() {
        return premiumRestorationMode;
    }

    public void setPremiumRestorationMode(PremiumPreservationMode premiumRestorationMode) {
        this.premiumRestorationMode = premiumRestorationMode;
    }

    public String getSeasonId() {
        return seasonId;
    }

    public void setSeasonId(String seasonId) {
        this.seasonId = seasonId;
    }

    public String getSeasonName() {
        return seasonName;
    }

    public void setSeasonName(String seasonName) {
        this.seasonName = seasonName;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public void setMaxLevel(int maxLevel) {
        this.maxLevel = maxLevel;
    }

    public int getSeasonDurationDays() {
        return 60;
    }

    public boolean isRestorePremiumStatus() {
        return restorePremiumStatus;
    }

    public void setRestorePremiumStatus(boolean restorePremiumStatus) {
        this.restorePremiumStatus = restorePremiumStatus;
    }

    public boolean isBroadcastStartMessage() {
        return broadcastStartMessage;
    }

    public void setBroadcastStartMessage(boolean broadcastStartMessage) {
        this.broadcastStartMessage = broadcastStartMessage;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public boolean hasDefinedEndTime() {
        return endTime != null;
    }

    public Map<String, Object> getCustomProperties() {
        return customProperties;
    }

    public Object getCustomProperty(String key) {
        return customProperties.get(key);
    }

    public void addCustomProperty(String key, Object value) {
        customProperties.put(key, value);
    }

    // Builder pattern for complex construction
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private SeasonStartOptions options;

        public Builder() {
            this.options = new SeasonStartOptions();
        }

        public Builder seasonId(String seasonId) {
            options.setSeasonId(seasonId);
            return this;
        }

        public Builder seasonName(String seasonName) {
            options.setSeasonName(seasonName);
            return this;
        }

        public Builder maxLevel(int maxLevel) {
            options.setMaxLevel(maxLevel);
            return this;
        }

        public Builder startTime(LocalDateTime startTime) {
            options.setStartTime(startTime);
            return this;
        }

        public Builder endTime(LocalDateTime endTime) {
            options.setEndTime(endTime);
            return this;
        }

        public Builder broadcastStartMessage(boolean broadcast) {
            options.setBroadcastStartMessage(broadcast);
            return this;
        }

        public Builder addCustomProperty(String key, Object value) {
            options.addCustomProperty(key, value);
            return this;
        }

        public Builder premiumRestorationMode(PremiumPreservationMode mode) {
            options.setPremiumRestorationMode(mode);
            return this;
        }

        public Builder restorePremiumStatus(boolean restore) {
            options.setRestorePremiumStatus(restore);
            return this;
        }

        public Builder customProperty(String key, Object value) {
            options.addCustomProperty(key, value);
            return this;
        }

        public SeasonStartOptions build() {
            return options;
        }
    }
}