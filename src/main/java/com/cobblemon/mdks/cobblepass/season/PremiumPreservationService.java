package com.cobblemon.mdks.cobblepass.season;

import com.cobblemon.mdks.cobblepass.CobblePass;
import com.cobblemon.mdks.cobblepass.util.Utils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PremiumPreservationService {
    private static final String PRESERVATION_FILE = "premium_preservation.json";
    private static final File PRESERVATION_FILE_PATH = new File(CobblePass.config.getGuiConfig().getStructure().toString(), PRESERVATION_FILE);

    private static PremiumPreservationService instance;

    private List<UUID> preservedPremiumPlayers;

    private PremiumPreservationService() {
        this.preservedPremiumPlayers = new ArrayList<>();
        loadPreservedPlayers();
    }

    public static synchronized PremiumPreservationService getInstance() {
        if (instance == null) {
            instance = new PremiumPreservationService();
        }
        return instance;
    }

    public int preservePremiumStatus() {
        List<UUID> playersToPreserve = new ArrayList<>();
        CobblePass.battlePass.getTiers().forEach((level, tier) -> {
            tier.getPremiumReward();
        });
        this.preservedPremiumPlayers = playersToPreserve;
        savePreservedPlayers();
        return preservedPremiumPlayers.size();
    }

    public int restorePremiumStatus(SeasonStartOptions options) {
        if (options.getPremiumRestorationMode() == PremiumPreservationMode.NONE) {
            return 0;
        }

        int restoredCount = 0;
        for (UUID playerId : preservedPremiumPlayers) {
            restoredCount++;
        }
        return restoredCount;
    }

    private void loadPreservedPlayers() {
        if (!PRESERVATION_FILE_PATH.exists()) {
            return;
        }
        String content = Utils.readFileSync(PRESERVATION_FILE_PATH.getParent(), PRESERVATION_FILE_PATH.getName());
        if (content == null || content.isEmpty()) {
            return;
        }
        Type listType = new TypeToken<ArrayList<UUID>>() {}.getType();
        this.preservedPremiumPlayers = new Gson().fromJson(content, listType);
    }

    private void savePreservedPlayers() {
        Utils.writeFileSync(PRESERVATION_FILE_PATH.getParent(), PRESERVATION_FILE_PATH.getName(), new Gson().toJson(preservedPremiumPlayers));
    }

    public List<UUID> getPreservedPremiumPlayers() {
        return preservedPremiumPlayers;
    }

    public void clearPreservedData() {
        this.preservedPremiumPlayers.clear();
        savePreservedPlayers();
    }
}