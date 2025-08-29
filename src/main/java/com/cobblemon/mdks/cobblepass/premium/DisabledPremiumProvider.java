package com.cobblemon.mdks.cobblepass.premium;

import java.util.Arrays;
import java.util.List;

import com.cobblemon.mdks.cobblepass.CobblePass;

import net.minecraft.server.level.ServerPlayer;

/**
 * Premium provider implementation that grants premium access to all players.
 * When this provider is active, all premium reward checks return true,
 * effectively disabling premium restrictions for the entire server.
 */
public class DisabledPremiumProvider implements PremiumProvider {
    
    @Override
    public boolean hasPremium(ServerPlayer player) {
        // In disabled mode, all players have premium access
        // Only check if season is active to maintain consistency with other providers
        return CobblePass.config.isSeasonActive();
    }
    
    @Override
    public boolean grantPremium(ServerPlayer player) {
        // In disabled mode, premium is always granted automatically
        // Return true if season is active, false otherwise
        return CobblePass.config.isSeasonActive();
    }
    
    @Override
    public boolean revokePremium(ServerPlayer player) {
        // In disabled mode, premium cannot be revoked from individual players
        // The only way to disable premium is to change the premium mode
        return false;
    }
    
    @Override
    public String getStatusMessage(ServerPlayer player) {
        if (!CobblePass.config.isSeasonActive()) {
            return "§cNo active season";
        }
        
        return "§aPremium Active §7(Disabled Mode - All players have premium access)";
    }
    
    @Override
    public List<String> getBulkOperationCommands() {
        return Arrays.asList(
            "§6Disabled Mode Operations:",
            "§7Premium access is automatically granted to all players",
            "§7No individual premium management is available in this mode",
            "§7To manage premium access, switch to ECONOMY or PERMISSION mode",
            "§3/battlepass admin premium list §7- List all players (all have premium)",
            "§3/battlepass admin mode <economy|permission> §7- Switch to different premium mode"
        );
    }
    
    @Override
    public PremiumMode getMode() {
        return PremiumMode.DISABLED;
    }
    
    @Override
    public void initialize() {
        CobblePass.LOGGER.info("Disabled Premium Provider initialized - All players have premium access");
        CobblePass.LOGGER.info("Premium restrictions are disabled for all battle pass rewards");
    }
    
    @Override
    public void shutdown() {
        CobblePass.LOGGER.info("Disabled Premium Provider shutdown");
    }
    
    /**
     * Checks if premium restrictions are currently disabled.
     * This is a convenience method that always returns true when this provider is active.
     * @return true, indicating premium restrictions are disabled
     */
    public boolean isPremiumDisabled() {
        return true;
    }
    
    /**
     * Gets the total number of players with premium access.
     * In disabled mode, this returns the count of online players since all have premium.
     * @return The number of players with premium access (all online players)
     */
    public int getTotalPremiumPlayers() {
        if (!CobblePass.config.isSeasonActive()) {
            return 0;
        }
        
        // In disabled mode, all online players have premium access
        return CobblePass.server.getPlayerList().getPlayerCount();
    }
    
    /**
     * Gets information about the disabled premium mode for administrative purposes.
     * @return A formatted string with mode information
     */
    public String getModeInfo() {
        return "§6Premium Mode: §eDisabled\n" +
               "§7All players automatically have premium access\n" +
               "§7Premium rewards are available to everyone\n" +
               "§7No economy or permission requirements";
    }
}