package com.cobblemon.mdks.cobblepass.season;

public class SeasonResetOptions {
    private boolean resetLevels;
    private boolean resetXP;
    private boolean resetClaimedRewards;
    private PremiumPreservationMode premiumPreservationMode;
    private boolean validateBeforeReset;
    private boolean preservePremiumStatus;
    private boolean syncFromPermissions;
    private boolean broadcastMessages;
    private boolean createBackup;

    public SeasonResetOptions() {
        this.resetLevels = true;
        this.resetXP = true;
        this.resetClaimedRewards = true;
        this.premiumPreservationMode = PremiumPreservationMode.PRESERVE_ALL;
        this.validateBeforeReset = true;
        this.preservePremiumStatus = true;
        this.syncFromPermissions = false;
        this.broadcastMessages = true;
        this.createBackup = true;
    }

    public SeasonResetOptions(boolean preservePremiumStatus, boolean syncFromPermissions,
                             boolean broadcastMessages, boolean createBackup,
                             boolean validateBeforeReset, PremiumPreservationMode premiumPreservationMode) {
        this.preservePremiumStatus = preservePremiumStatus;
        this.syncFromPermissions = syncFromPermissions;
        this.broadcastMessages = broadcastMessages;
        this.createBackup = createBackup;
        this.validateBeforeReset = validateBeforeReset;
        this.premiumPreservationMode = premiumPreservationMode;
        this.resetLevels = true;
        this.resetXP = true;
        this.resetClaimedRewards = true;
    }

    public boolean isResetLevels() {
        return resetLevels;
    }

    public void setResetLevels(boolean resetLevels) {
        this.resetLevels = resetLevels;
    }

    public boolean isResetXP() {
        return resetXP;
    }

    public void setResetXP(boolean resetXP) {
        this.resetXP = resetXP;
    }

    public boolean isResetClaimedRewards() {
        return resetClaimedRewards;
    }

    public void setResetClaimedRewards(boolean resetClaimedRewards) {
        this.resetClaimedRewards = resetClaimedRewards;
    }

    public PremiumPreservationMode getPremiumPreservationMode() {
        return premiumPreservationMode;
    }

    public void setPremiumPreservationMode(PremiumPreservationMode premiumPreservationMode) {
        this.premiumPreservationMode = premiumPreservationMode;
    }

    public boolean isValidateBeforeReset() {
        return validateBeforeReset;
    }

    public void setValidateBeforeReset(boolean validateBeforeReset) {
        this.validateBeforeReset = validateBeforeReset;
    }

    public boolean isCreateBackup() {
        return createBackup;
    }

    public void setCreateBackup(boolean createBackup) {
        this.createBackup = createBackup;
    }

    public PremiumPreservationMode getPreservationMode() {
        return premiumPreservationMode;
    }

    public void setPreservationMode(PremiumPreservationMode premiumPreservationMode) {
        this.premiumPreservationMode = premiumPreservationMode;
    }

    public boolean isBroadcastMessages() {
        return broadcastMessages;
    }

    public void setBroadcastMessages(boolean broadcastMessages) {
        this.broadcastMessages = broadcastMessages;
    }

    public boolean isPreservePremiumStatus() {
        return preservePremiumStatus;
    }

    public void setPreservePremiumStatus(boolean preservePremiumStatus) {
        this.preservePremiumStatus = preservePremiumStatus;
    }

    public boolean isSyncFromPermissions() {
        return syncFromPermissions;
    }

    public void setSyncFromPermissions(boolean syncFromPermissions) {
        this.syncFromPermissions = syncFromPermissions;
    }
}