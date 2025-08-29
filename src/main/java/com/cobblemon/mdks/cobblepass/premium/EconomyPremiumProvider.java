package com.cobblemon.mdks.cobblepass.premium;

import com.cobblemon.mdks.cobblepass.CobblePass;
import com.cobblemon.mdks.cobblepass.battlepass.PlayerBattlePass;
import com.cobblemon.mdks.cobblepass.util.Constants;
import com.cobblemon.mdks.cobblepass.util.EconomyUtils;
import com.cobblemon.mdks.cobblepass.util.Utils;
import net.minecraft.server.level.ServerPlayer;

import java.util.Arrays;
import java.util.List;

/**
 * Premium provider implementation that handles premium access through economy purchases.
 * This maintains backward compatibility with the existing economy-based premium system.
 */
public class EconomyPremiumProvider implements PremiumProvider {
    
    @Override
    public boolean hasPremium(ServerPlayer player) {
        if (!CobblePass.config.isSeasonActive()) {
            return false;
        }
        
        PlayerBattlePass playerPass = CobblePass.battlePass.getPlayerPass(player);
        return playerPass.hasPremium();
    }
    
    @Override
    public boolean grantPremium(ServerPlayer player) {
        if (!CobblePass.config.isSeasonActive()) {
            return false;
        }
        
        // Check if economy system is available
        if (!EconomyUtils.isEconomyAvailable()) {
            CobblePass.LOGGER.warn("Cannot grant premium - economy system not available");
            return false;
        }
        
        long cost = CobblePass.config.getPremiumConfig().getPremiumCost();
        
        // Check if economy is enabled for this mode
        if (!CobblePass.config.getPremiumConfig().isEconomyEnabled()) {
            return false;
        }
        
        // Check if player has sufficient balance
        if (!EconomyUtils.hasBalance(player.getUUID(), cost)) {
            return false;
        }
        
        // Deduct funds
        if (!EconomyUtils.withdraw(player.getUUID(), cost)) {
            return false;
        }
        
        // Grant premium access
        PlayerBattlePass playerPass = CobblePass.battlePass.getPlayerPass(player);
        playerPass.setPremium(true);
        
        // Save player data immediately
        savePlayerData(player, playerPass);
        
        return true;
    }
    
    @Override
    public boolean revokePremium(ServerPlayer player) {
        if (!CobblePass.config.isSeasonActive()) {
            return false;
        }
        
        PlayerBattlePass playerPass = CobblePass.battlePass.getPlayerPass(player);
        if (!playerPass.hasPremium()) {
            return false; // Player doesn't have premium to revoke
        }
        
        playerPass.setPremium(false);
        
        // Save player data immediately
        savePlayerData(player, playerPass);
        
        return true;
    }
    
    @Override
    public String getStatusMessage(ServerPlayer player) {
        if (!CobblePass.config.isSeasonActive()) {
            return "§cNo active season";
        }
        
        // Check if economy system is available
        if (!EconomyUtils.isEconomyAvailable()) {
            return "§cEconomy system not available";
        }
        
        PlayerBattlePass playerPass = CobblePass.battlePass.getPlayerPass(player);
        long cost = CobblePass.config.getPremiumConfig().getPremiumCost();
        
        if (playerPass.hasPremium()) {
            return "§aPremium Active §7(Season " + CobblePass.config.getCurrentSeason() + ")";
        } else {
            boolean hasBalance = EconomyUtils.hasBalance(player.getUUID(), cost);
            String costStr = EconomyUtils.formatCurrency(cost);
            
            if (hasBalance) {
                return "§7Premium Available §8- §6" + costStr + " §7(/battlepass premium buy)";
            } else {
                return "§cInsufficient Funds §8- §6" + costStr + " §7required";
            }
        }
    }
    
    @Override
    public List<String> getBulkOperationCommands() {
        return Arrays.asList(
            "§6Bulk Economy Operations:",
            "§3/battlepass admin premium grant <player> §7- Grant premium to player (no cost)",
            "§3/battlepass admin premium revoke <player> §7- Revoke premium from player",
            "§3/battlepass admin premium list §7- List all premium players",
            "§3/battlepass admin premium refund <player> §7- Refund premium purchase"
        );
    }
    
    @Override
    public PremiumMode getMode() {
        return PremiumMode.ECONOMY;
    }
    
    @Override
    public void initialize() {
        if (EconomyUtils.isEconomyAvailable()) {
            CobblePass.LOGGER.info("Economy Premium Provider initialized - Premium cost: " +
                EconomyUtils.formatCurrency(CobblePass.config.getPremiumConfig().getPremiumCost()));
        } else {
            CobblePass.LOGGER.warn("Economy Premium Provider initialized but economy system not available");
        }
    }
    
    @Override
    public void shutdown() {
        CobblePass.LOGGER.info("Economy Premium Provider shutdown");
    }
    
    /**
     * Checks if a player can afford premium access.
     * @param player The player to check
     * @return true if the player has sufficient balance
     */
    public boolean canAffordPremium(ServerPlayer player) {
        if (!EconomyUtils.isEconomyAvailable()) {
            return false;
        }
        long cost = CobblePass.config.getPremiumConfig().getPremiumCost();
        return EconomyUtils.hasBalance(player.getUUID(), cost);
    }
    
    /**
     * Gets the cost of premium access.
     * @return The cost in the server's economy currency
     */
    public long getPremiumCost() {
        return CobblePass.config.getPremiumConfig().getPremiumCost();
    }
    
    /**
     * Refunds a premium purchase to a player.
     * @param player The player to refund
     * @return true if refund was successful
     */
    public boolean refundPremium(ServerPlayer player) {
        if (!EconomyUtils.isEconomyAvailable()) {
            CobblePass.LOGGER.warn("Cannot refund premium - economy system not available");
            return false;
        }
        
        PlayerBattlePass playerPass = CobblePass.battlePass.getPlayerPass(player);
        if (!playerPass.hasPremium()) {
            return false; // Player doesn't have premium to refund
        }
        
        // Note: This is a simplified refund - in a real implementation,
        // you might want to track purchase history to ensure proper refunds
        long refundAmount = CobblePass.config.getPremiumConfig().getPremiumCost();
        
        // Add funds back to player account
        try {
            if (EconomyUtils.deposit(player.getUUID(), refundAmount)) {
                // Revoke premium access
                playerPass.setPremium(false);
                savePlayerData(player, playerPass);
                return true;
            } else {
                CobblePass.LOGGER.error("Failed to deposit refund for player " + player.getName().getString());
                return false;
            }
        } catch (Exception e) {
            CobblePass.LOGGER.error("Failed to refund premium for player " + player.getName().getString(), e);
            return false;
        }
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