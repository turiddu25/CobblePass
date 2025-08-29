package com.cobblemon.mdks.cobblepass.premium;

import java.util.Arrays;
import java.util.List;

import com.cobblemon.mdks.cobblepass.CobblePass;
import com.cobblemon.mdks.cobblepass.battlepass.PlayerBattlePass;
import com.cobblemon.mdks.cobblepass.util.Constants;
import com.cobblemon.mdks.cobblepass.util.Utils;
import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.permission.CobblemonPermission;
import com.cobblemon.mod.common.api.permission.PermissionLevel;

import net.minecraft.server.level.ServerPlayer;

/**
 * Premium provider implementation that handles premium access through permission nodes.
 * Players with the configured permission node automatically have premium access.
 * Includes fallback handling when permission system is unavailable.
 */
public class PermissionPremiumProvider implements PremiumProvider {
    
    private CobblemonPermission premiumPermission;
    private boolean permissionSystemAvailable;
    
    @Override
    public boolean hasPremium(ServerPlayer player) {
        if (!CobblePass.config.isSeasonActive()) {
            return false;
        }

        if (!this.permissionSystemAvailable) {
            // When the permission system is unavailable, we cannot reliably check for premium.
            // Falling back to stored data was the source of the bug, as it could be stale.
            // It is safer to report that the player does not have premium and log a warning.
            CobblePass.LOGGER.warn("Permission system is unavailable. Cannot check premium status for " + player.getName().getString() + ".");
            return false;
        }

        try {
            // The permission system is the single source of truth.
            return Cobblemon.INSTANCE.getPermissionValidator().hasPermission(player, this.premiumPermission);
        } catch (Exception e) {
            // If there's an error checking permissions, we must assume the player does not have premium.
            // The fallback to stored data caused misleading information.
            CobblePass.LOGGER.error("An error occurred while checking permissions for " + player.getName().getString() + ". Defaulting to no premium.", e);
            return false;
        }
    }
    
    @Override
    public boolean grantPremium(ServerPlayer player) {
        if (!CobblePass.config.isSeasonActive()) {
            return false;
        }
        
        // In permission mode, premium is granted through permission nodes
        // This method stores the premium status locally for fallback purposes
        // and when permission system is unavailable
        
        PlayerBattlePass playerPass = CobblePass.battlePass.getPlayerPassWithoutMigration(player);
        
        // Check if player already has premium through permissions
        if (permissionSystemAvailable && hasPremium(player)) {
            // Sync the stored status with permission status
            if (!playerPass.hasPremium()) {
                playerPass.setPremium(true);
                savePlayerData(player, playerPass);
            }
            return true;
        }
        
        // Grant premium access locally (for fallback scenarios)
        playerPass.setPremium(true);
        savePlayerData(player, playerPass);
        
        return true;
    }
    
    @Override
    public boolean revokePremium(ServerPlayer player) {
        if (!CobblePass.config.isSeasonActive()) {
            return false;
        }
        
        // In permission mode, premium should be revoked through permission management
        // This method only updates the local stored status for fallback purposes
        
        PlayerBattlePass playerPass = CobblePass.battlePass.getPlayerPassWithoutMigration(player);
        
        if (!playerPass.hasPremium()) {
            return false; // Player doesn't have premium to revoke
        }
        
        // Revoke premium access locally (for fallback scenarios)
        playerPass.setPremium(false);
        savePlayerData(player, playerPass);
        
        return true;
    }
    
    @Override
    public String getStatusMessage(ServerPlayer player) {
        if (!CobblePass.config.isSeasonActive()) {
            return "§cNo active season";
        }

        String permissionNode = CobblePass.config.getPremiumConfig().getPermissionNode();

        if (!this.permissionSystemAvailable) {
            return "§cPermission System Unavailable §8- §7Contact administrator";
        }

        boolean hasPerm = this.hasPremium(player);

        if (hasPerm) {
            return "§aPremium Active §7(Permission: " + permissionNode + ")";
        } else {
            return "§7Premium Unavailable §8- §7Requires permission: §e" + permissionNode;
        }
    }
    
    @Override
    public List<String> getBulkOperationCommands() {
        String permissionNode = CobblePass.config.getPremiumConfig().getPermissionNode();
        
        return Arrays.asList(
            "§6Bulk Permission Operations:",
            "§3/battlepass admin premium grant <player> §7- Grant premium locally (fallback)",
            "§3/battlepass admin premium revoke <player> §7- Revoke premium locally (fallback)",
            "§3/battlepass admin premium list §7- List all premium players",
            "§3/battlepass admin premium sync §7- Sync permission status with stored data",
            "§7",
            "§ePermission Management (use your permission plugin):",
            "§7Grant permission: §e" + permissionNode,
            "§7Revoke permission: Remove §e" + permissionNode,
            "§7Current permission system: §e" + (permissionSystemAvailable ? "Available" : "Unavailable")
        );
    }
    
    @Override
    public PremiumMode getMode() {
        return PremiumMode.PERMISSION;
    }
    
    @Override
    public void initialize() {
        String permissionNode = CobblePass.config.getPremiumConfig().getPermissionNode();
        
        try {
            // Create the permission object
            this.premiumPermission = new CobblemonPermission(permissionNode, PermissionLevel.NONE);
            
            // Test if permission system is available
            this.permissionSystemAvailable = testPermissionSystem();
            
            if (permissionSystemAvailable) {
                CobblePass.LOGGER.info("Permission Premium Provider initialized - Permission node: " + permissionNode);
            } else {
                CobblePass.LOGGER.warn("Permission Premium Provider initialized with limited functionality - " +
                    "Permission system unavailable, using fallback mode");
            }
            
        } catch (Exception e) {
            CobblePass.LOGGER.error("Failed to initialize Permission Premium Provider", e);
            this.permissionSystemAvailable = false;
        }
    }
    
    @Override
    public void shutdown() {
        CobblePass.LOGGER.info("Permission Premium Provider shutdown");
        this.premiumPermission = null;
        this.permissionSystemAvailable = false;
    }
    
    /**
     * Tests if the permission system is available and functional.
     * @return true if permission system is working, false otherwise
     */
    private boolean testPermissionSystem() {
        try {
            // Try to access the permission validator
            if (Cobblemon.INSTANCE.getPermissionValidator() == null) {
                return false;
            }
            
            // Permission system appears to be available
            return true;
            
        } catch (Exception e) {
            CobblePass.LOGGER.debug("Permission system test failed", e);
            return false;
        }
    }
    
    /**
     * Synchronizes permission-based premium status with stored player data.
     * This is useful for ensuring consistency between permission system and local storage.
     * @param player The player to synchronize
     * @return true if synchronization was successful
     */
    public boolean syncPremiumStatus(ServerPlayer player) {
        if (!permissionSystemAvailable) {
            return false;
        }
        
        try {
            boolean hasPermission = Cobblemon.INSTANCE.getPermissionValidator().hasPermission(player, premiumPermission);
            PlayerBattlePass playerPass = CobblePass.battlePass.getPlayerPassWithoutMigration(player);
            
            // Update stored status to match permission status
            if (hasPermission != playerPass.hasPremium()) {
                playerPass.setPremium(hasPermission);
                savePlayerData(player, playerPass);
                
                CobblePass.LOGGER.debug("Synced premium status for player " + player.getName().getString() + 
                    ": " + hasPermission);
            }
            
            return true;
            
        } catch (Exception e) {
            CobblePass.LOGGER.error("Failed to sync premium status for player " + player.getName().getString(), e);
            return false;
        }
    }
    
    /**
     * Gets the current permission node being used for premium access.
     * @return The permission node string
     */
    public String getPermissionNode() {
        return CobblePass.config.getPremiumConfig().getPermissionNode();
    }
    
    /**
     * Checks if the permission system is currently available.
     * @return true if permission system is functional
     */
    public boolean isPermissionSystemAvailable() {
        return permissionSystemAvailable;
    }
    
    /**
     * Performs a bulk synchronization of all online players' premium status.
     * @return Number of players synchronized
     */
    public int bulkSyncPremiumStatus() {
        if (!permissionSystemAvailable) {
            return 0;
        }
        
        int syncedCount = 0;
        
        try {
            // Get all online players and sync their premium status
            for (ServerPlayer player : CobblePass.server.getPlayerList().getPlayers()) {
                if (syncPremiumStatus(player)) {
                    syncedCount++;
                }
            }
            
            CobblePass.LOGGER.info("Bulk premium status sync completed: " + syncedCount + " players synchronized");
            
        } catch (Exception e) {
            CobblePass.LOGGER.error("Failed to perform bulk premium status sync", e);
        }
        
        return syncedCount;
    }
    
    /**
     * Saves player battle pass data to disk.
     * @param player The player whose data to save
     * @param playerPass The player's battle pass data
     */
    private void savePlayerData(ServerPlayer player, PlayerBattlePass playerPass) {
        String filename = player.getUUID() + ".json";
        Utils.writeFileSync(Constants.PLAYER_DATA_DIR, filename,
                Utils.newGson().toJson(playerPass.toJson()));
    }
}