package com.cobblemon.mdks.cobblepass.season;

import com.cobblemon.mdks.cobblepass.CobblePass;
import com.cobblemon.mdks.cobblepass.config.Config;
import java.util.concurrent.CompletableFuture;

import java.util.UUID;

public class SeasonManager {
    private static SeasonManager instance;

    private SeasonManager() {
    }

    public static synchronized SeasonManager getInstance() {
        if (instance == null) {
            instance = new SeasonManager();
        }
        return instance;
    }

    public SeasonTransitionResult endCurrentSeason(SeasonResetOptions options) {
        return new SeasonTransitionResult(true, "Season ended successfully.");
    }

    public CompletableFuture<SeasonResetResult> endSeason(SeasonResetOptions options) {
        return CompletableFuture.completedFuture(new SeasonResetResult(true, "Season ended successfully."));
    }

    public boolean isTransitionInProgress() {
        return false;
    }

    public SeasonTransitionResult startNewSeason(SeasonStartOptions options) {
        return new SeasonTransitionResult(true, "Season started successfully.");
    }

    public SeasonStatus getSeasonStatus() {
        return new SeasonStatus(true, 1, 1, 1);
    }

    public void confirmSeasonReset(UUID uuid) {
    }

    public SeasonTransitionResult performSeasonTransition(SeasonResetOptions resetOptions, SeasonStartOptions startOptions) {
        // End current season
        SeasonTransitionResult endResult = endCurrentSeason(resetOptions);
        if (!endResult.isSuccess()) {
            return endResult;
        }

        // Start new season
        SeasonTransitionResult startResult = startNewSeason(startOptions);
        return startResult;
    }
}