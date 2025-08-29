package com.cobblemon.mdks.cobblepass.season;

public class SeasonStatus {
    private final boolean active;
    private final int seasonNumber;
    private final long startTime;
    private final long endTime;

    public SeasonStatus(boolean active, int seasonNumber, long startTime, long endTime) {
        this.active = active;
        this.seasonNumber = seasonNumber;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public boolean isActive() {
        return active;
    }

    public int getSeasonNumber() {
        return seasonNumber;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }
}