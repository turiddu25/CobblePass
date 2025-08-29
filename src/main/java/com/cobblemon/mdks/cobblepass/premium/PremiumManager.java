package com.cobblemon.mdks.cobblepass.premium;

import com.cobblemon.mdks.cobblepass.CobblePass;
import com.cobblemon.mdks.cobblepass.season.PremiumPreservationService;
import com.cobblemon.mdks.cobblepass.season.SeasonStartOptions;
import net.minecraft.server.level.ServerPlayer;
import java.util.*;

/**
 * Manages premium access control through different provider implementations.
 * Handles switching between premium modes and delegating premium operations.
 * Integrates with season management for premium status preservation.
 */
public class PremiumManager {
    private static PremiumManager instance;
    private PremiumProvider currentProvider;
    private final Map<PremiumMode, PremiumProvider> providers;
    
    // Season transition integration
    private final PremiumPreservationService preservationService;
    private final Set<UUID> pendingRestorations;
    
    private PremiumManager() {
        this.providers = new HashMap<>();
        this.preservationService = PremiumPreservationService.getInstance();
        this.pendingRestorations = new HashSet<>();
        initializeProviders();
    }
    
    public static PremiumManager getInstance() {
        if (instance == null) {
            instance = new PremiumManager();
        }
        return instance;
    }
    
    private void initializeProviders() {
        providers.put(PremiumMode.ECONOMY, new EconomyPremiumProvider());
        providers.put(PremiumMode.PERMISSION, new PermissionPremiumProvider());
        providers.put(PremiumMode.DISABLED, new DisabledPremiumProvider());
        
        // Initialize with the configured mode
        PremiumMode configuredMode = CobblePass.config.getPremiumConfig().getMode();
        switchToMode(configuredMode);
    }
    
    /**
     * Switches to a different premium mode.
     * @param mode The new premium mode to switch to
     */
    public void switchToMode(PremiumMode mode) {
        if (currentProvider != null) {
            currentProvider.shutdown();
        }
        
        currentProvider = providers.get(mode);
        if (currentProvider != null) {
            currentProvider.initialize();
            CobblePass.LOGGER.info("Switched to premium mode: " + mode.getConfigValue());
        } else {
            CobblePass.LOGGER.error("Unknown premium mode: " + mode);
            // Fallback to disabled mode
            currentProvider = providers.get(PremiumMode.DISABLED);
            currentProvider.initialize();
        }
    }
    
    /**
     * Checks if a player has premium access using the current provider.
     * @param player The player to check
     * @return true if the player has premium access
     */
    public boolean hasPremium(ServerPlayer player) {
        if (currentProvider == null) {
            return false;
        }
        return currentProvider.hasPremium(player);
    }
    
    /**
     * Grants premium access to a player using the current provider.
     * @param player The player to grant premium access to
     * @return true if premium was successfully granted
     */
    public boolean grantPremium(ServerPlayer player) {
        if (currentProvider == null) {
            return false;
        }
        return currentProvider.grantPremium(player);
    }
    
    /**
     * Revokes premium access from a player using the current provider.
     * @param player The player to revoke premium access from
     * @return true if premium was successfully revoked
     */
    public boolean revokePremium(ServerPlayer player) {
        if (currentProvider == null) {
            return false;
        }
        return currentProvider.revokePremium(player);
    }
    
    /**
     * Gets the status message for a player's premium access.
     * @param player The player to get status for
     * @return A human-readable status message
     */
    public String getStatusMessage(ServerPlayer player) {
        if (currentProvider == null) {
            return "Premium system not initialized";
        }
        return currentProvider.getStatusMessage(player);
    }
    
    /**
     * Gets the current premium mode.
     * @return The currently active premium mode
     */
    public PremiumMode getCurrentMode() {
        return currentProvider != null ? currentProvider.getMode() : PremiumMode.DISABLED;
    }
    
    /**
     * Gets the current premium provider.
     * @return The currently active premium provider
     */
    public PremiumProvider getCurrentProvider() {
        return currentProvider;
    }
    
    /**
     * Reinitializes the premium manager with updated configuration.
     */
    public void reload() {
        PremiumMode configuredMode = CobblePass.config.getPremiumConfig().getMode();
        if (getCurrentMode() != configuredMode) {
            switchToMode(configuredMode);
        }
    }
    
    // ===== SEASON TRANSITION INTEGRATION =====
    
    /**
     * Prepares for season transition by preserving current premium status.
     * This should be called before a season reset operation.
     * 
     * @return The number of premium players preserved
     */
    public int prepareForSeasonTransition() {
        try {
            CobblePass.LOGGER.info("Preparing premium system for season transition");
            
            // Clear any pending restorations from previous transitions
            pendingRestorations.clear();
            
            // Preserve current premium status
            int preservedCount = preservationService.preservePremiumStatus();
            
            CobblePass.LOGGER.info("Premium system ready for season transition - " + preservedCount + " players preserved");
            return preservedCount;
            
        } catch (Exception e) {
            CobblePass.LOGGER.error("Error preparing premium system for season transition", e);
            return 0;
        }
    }
    
    /**
     * Handles premium status restoration after a season transition.
     * This should be called after a new season has been started.
     * 
     * @param options The season start options containing restoration preferences
     * @return The number of players whose premium status was restored
     */
    public int handleSeasonTransition(SeasonStartOptions options) {
        try {
            CobblePass.LOGGER.info("Handling premium system season transition with mode: " + 
                    options.getPremiumRestorationMode());
            
            int restoredCount = preservationService.restorePremiumStatus(options);
            
            // Track players who need restoration when they join
            List<UUID> preservedPlayers = preservationService.getPreservedPremiumPlayers();
            pendingRestorations.addAll(preservedPlayers);
            
            CobblePass.LOGGER.info("Premium system season transition complete - " + restoredCount + " players restored");
            return restoredCount;
            
        } catch (Exception e) {
            CobblePass.LOGGER.error("Error handling premium system season transition", e);
            return 0;
        }
    }
    
    /**
     * Handles premium status migration for existing players.
     * This ensures that players who had premium in the previous season
     * get their status restored according to the preservation mode.
     * 
     * @param player The player to check for premium migration
     * @return true if premium status was migrated for this player
     */
    public boolean handlePremiumMigration(ServerPlayer player) {
        UUID playerId = player.getUUID();
        
        // Check if this player needs premium restoration
        if (!pendingRestorations.contains(playerId)) {
            return false;
        }
        
        try {
            // Attempt to restore premium status
            boolean restored = currentProvider.grantPremium(player);
            
            if (restored) {
                // Remove from pending restorations
                pendingRestorations.remove(playerId);
                CobblePass.LOGGER.info("Migrated premium status for player: " + player.getName().getString());
                return true;
            } else {
                CobblePass.LOGGER.warn("Failed to migrate premium status for player: " + player.getName().getString());
                return false;
            }
            
        } catch (Exception e) {
            CobblePass.LOGGER.error("Error migrating premium status for player: " + player.getName().getString(), e);
            return false;
        }
    }
    
    /**
     * Validates premium restoration and handles error scenarios.
     * This ensures that the premium system is in a consistent state after season transitions.
     * 
     * @return A validation report containing any issues found
     */
    public PremiumValidationReport validatePremiumRestoration() {
        PremiumValidationReport report = new PremiumValidationReport();
        
        try {
            // Check for players with pending restorations
            report.pendingRestorations = new ArrayList<>(pendingRestorations);
            
            // Check for inconsistencies between preserved data and current state
            List<UUID> preservedPlayers = preservationService.getPreservedPremiumPlayers();
            
            // Count online players with premium
            int onlinePremiumCount = 0;
            if (CobblePass.getServer() != null) {
                for (ServerPlayer player : CobblePass.getServer().getPlayerList().getPlayers()) {
                    if (hasPremium(player)) {
                        onlinePremiumCount++;
                    }
                }
            }
            
            report.preservedPlayerCount = preservedPlayers.size();
            report.onlinePremiumCount = onlinePremiumCount;
            report.pendingRestorationsCount = pendingRestorations.size();
            report.currentMode = getCurrentMode();
            
            // Check for potential issues
            if (report.pendingRestorationsCount > 0) {
                report.warnings.add("There are " + report.pendingRestorationsCount + " players with pending premium restorations");
            }
            
            if (report.preservedPlayerCount == 0 && report.onlinePremiumCount > 0) {
                report.warnings.add("No preserved players found but " + report.onlinePremiumCount + " online players have premium");
            }
            
            report.success = true;
            
        } catch (Exception e) {
            report.success = false;
            report.error = e.getMessage();
            CobblePass.LOGGER.error("Error validating premium restoration", e);
        }
        
        return report;
    }
    
    /**
     * Gets the list of players with pending premium restorations.
     * 
     * @return Set of UUIDs for players awaiting premium restoration
     */
    public Set<UUID> getPendingRestorations() {
        return new HashSet<>(pendingRestorations);
    }
    
    /**
     * Clears all pending premium restorations.
     * This should be called after successful restoration or when cleaning up.
     */
    public void clearPendingRestorations() {
        pendingRestorations.clear();
        CobblePass.LOGGER.info("Cleared all pending premium restorations");
    }
    
    /**
     * Forces premium restoration for a specific player.
     * This can be used for manual intervention when automatic restoration fails.
     * 
     * @param player The player to force premium restoration for
     * @return true if restoration was successful
     */
    public boolean forcePremiumRestoration(ServerPlayer player) {
        UUID playerId = player.getUUID();
        
        try {
            // Check if player was in the preserved list
            List<UUID> preservedPlayers = preservationService.getPreservedPremiumPlayers();
            if (!preservedPlayers.contains(playerId)) {
                CobblePass.LOGGER.warn("Player " + player.getName().getString() + " was not in preserved premium list");
                return false;
            }
            
            // Grant premium status
            boolean granted = currentProvider.grantPremium(player);
            
            if (granted) {
                // Remove from pending restorations
                pendingRestorations.remove(playerId);
                CobblePass.LOGGER.info("Forced premium restoration for player: " + player.getName().getString());
                return true;
            } else {
                CobblePass.LOGGER.error("Failed to force premium restoration for player: " + player.getName().getString());
                return false;
            }
            
        } catch (Exception e) {
            CobblePass.LOGGER.error("Error forcing premium restoration for player: " + player.getName().getString(), e);
            return false;
        }
    }
    
    /**
     * Report class for premium validation results.
     */
    public static class PremiumValidationReport {
        public boolean success = false;
        public String error = null;
        public List<String> warnings = new ArrayList<>();
        public List<UUID> pendingRestorations = new ArrayList<>();
        public int preservedPlayerCount = 0;
        public int onlinePremiumCount = 0;
        public int pendingRestorationsCount = 0;
        public PremiumMode currentMode = PremiumMode.DISABLED;
        
        public boolean hasWarnings() {
            return !warnings.isEmpty();
        }
        
        public boolean hasErrors() {
            return !success || error != null;
        }
    }
}