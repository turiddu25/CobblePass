package com.cobblemon.mdks.cobblepass.premium;

import net.minecraft.server.level.ServerPlayer;
import java.util.List;

/**
 * Interface for providing premium access control functionality.
 * Different implementations handle premium access through various methods
 * such as economy purchases, permission nodes, or disabled mode.
 */
public interface PremiumProvider {
    
    /**
     * Checks if a player currently has premium access.
     * @param player The player to check
     * @return true if the player has premium access, false otherwise
     */
    boolean hasPremium(ServerPlayer player);
    
    /**
     * Grants premium access to a player.
     * @param player The player to grant premium access to
     * @return true if premium was successfully granted, false otherwise
     */
    boolean grantPremium(ServerPlayer player);
    
    /**
     * Revokes premium access from a player.
     * @param player The player to revoke premium access from
     * @return true if premium was successfully revoked, false otherwise
     */
    boolean revokePremium(ServerPlayer player);
    
    /**
     * Gets a status message describing the player's current premium status.
     * @param player The player to get status for
     * @return A human-readable status message
     */
    String getStatusMessage(ServerPlayer player);
    
    /**
     * Gets a list of available bulk operation commands for this provider.
     * @return List of command descriptions for bulk operations
     */
    List<String> getBulkOperationCommands();
    
    /**
     * Gets the premium mode this provider handles.
     * @return The PremiumMode this provider implements
     */
    PremiumMode getMode();
    
    /**
     * Initializes the provider with any necessary setup.
     * Called when the provider is first activated.
     */
    void initialize();
    
    /**
     * Cleans up resources when the provider is deactivated.
     * Called when switching to a different premium mode.
     */
    void shutdown();
}