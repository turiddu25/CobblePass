package com.cobblemon.mdks.cobblepass.season;

import java.util.ArrayList;
import java.util.List;

public class SeasonResetSummary {
    private int playersReset;
    private int premiumPreserved;
    private String backupPath;
    private int backupFilesCreated;
    private long resetTimestamp;
    private String previousSeasonId;
    private String newSeasonId;
    private List<String> operationDetails;

    public SeasonResetSummary(int playersReset, int premiumPreserved, String backupPath) {
        this.playersReset = playersReset;
        this.premiumPreserved = premiumPreserved;
        this.backupPath = backupPath != null ? backupPath : "";
        this.operationDetails = new ArrayList<>();
        this.resetTimestamp = System.currentTimeMillis();
        this.previousSeasonId = "0";
        this.newSeasonId = "1";
        this.backupFilesCreated = 0;
    }

    public SeasonResetSummary() {
        this(0, 0, "");
    }

    public int getPlayersReset() {
        return playersReset;
    }

    public int getPremiumPreserved() {
        return premiumPreserved;
    }

    public String getBackupPath() {
        return backupPath;
    }

    public void setBackupPath(String backupPath) {
        this.backupPath = backupPath;
    }

    public int getBackupFilesCreated() {
        return backupFilesCreated;
    }

    public void setBackupFilesCreated(int backupFilesCreated) {
        this.backupFilesCreated = backupFilesCreated;
    }

    public int getTotalPlayersReset() {
        return playersReset;
    }

    public void setTotalPlayersReset(int playersReset) {
        this.playersReset = playersReset;
    }

    public int getPremiumPlayersPreserved() {
        return premiumPreserved;
    }

    public void setPremiumPlayersPreserved(int premiumPreserved) {
        this.premiumPreserved = premiumPreserved;
    }

    public long getResetTimestamp() {
        return resetTimestamp;
    }

    public void setResetTimestamp(long resetTimestamp) {
        this.resetTimestamp = resetTimestamp;
    }

    public String getPreviousSeasonId() {
        return previousSeasonId;
    }

    public void setPreviousSeasonId(String previousSeasonId) {
        this.previousSeasonId = previousSeasonId;
    }

    public String getNewSeasonId() {
        return newSeasonId;
    }

    public void setNewSeasonId(String newSeasonId) {
        this.newSeasonId = newSeasonId;
    }

    public List<String> getOperationDetails() {
        return operationDetails;
    }

    public void addOperationDetail(String detail) {
        operationDetails.add(detail);
    }

    public String generateSummaryText() {
        StringBuilder summary = new StringBuilder();
        summary.append("Season Reset Summary:\n");
        summary.append("- Players reset: ").append(playersReset).append("\n");
        summary.append("- Premium players preserved: ").append(premiumPreserved).append("\n");
        summary.append("- Backup files created: ").append(backupFilesCreated).append("\n");
        summary.append("- Backup path: ").append(backupPath).append("\n");
        summary.append("- Previous season: ").append(previousSeasonId).append("\n");
        summary.append("- New season: ").append(newSeasonId).append("\n");
        
        if (!operationDetails.isEmpty()) {
            summary.append("Operation Details:\n");
            for (String detail : operationDetails) {
                summary.append("- ").append(detail).append("\n");
            }
        }
        
        return summary.toString();
    }
}