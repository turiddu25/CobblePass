package com.cobblemon.mdks.cobblepass.premium;

import net.minecraft.server.level.ServerPlayer;
import java.util.List;
import java.util.Map;

/**
 * Interface for handling premium mode transitions and data migration.
 * Implementations handle the complex process of switching between different
 * premium modes while preserving player data and ensuring smooth transitions.
 */
public interface PremiumMigrationHandler {
    
    /**
     * Migrates premium data from one mode to another.
     * @param fromMode The current premium mode
     * @param toMode The target premium mode
     * @return true if migration was successful, false otherwise
     */
    boolean migratePremiumData(PremiumMode fromMode, PremiumMode toMode);
    
    /**
     * Gets a list of players who currently have premium access.
     * @param mode The premium mode to check
     * @return List of players with premium access in the specified mode
     */
    List<ServerPlayer> getPremiumPlayers(PremiumMode mode);
    
    /**
     * Creates a backup of current premium data before migration.
     * @param mode The current premium mode
     * @return true if backup was successful, false otherwise
     */
    boolean createPremiumBackup(PremiumMode mode);
    
    /**
     * Validates that a migration can be performed safely.
     * @param fromMode The current premium mode
     * @param toMode The target premium mode
     * @return Map of validation results with any issues found
     */
    Map<String, String> validateMigration(PremiumMode fromMode, PremiumMode toMode);
    
    /**
     * Rolls back a failed migration to the previous state.
     * @param fromMode The mode that was being migrated from
     * @param toMode The mode that migration failed to reach
     * @return true if rollback was successful, false otherwise
     */
    boolean rollbackMigration(PremiumMode fromMode, PremiumMode toMode);
    
    /**
     * Preserves premium status for players during season transitions.
     * @param players List of players whose premium status should be preserved
     * @return true if preservation was successful, false otherwise
     */
    boolean preservePremiumStatus(List<ServerPlayer> players);
    
    /**
     * Gets migration statistics and information.
     * @return Map containing migration statistics and status information
     */
    Map<String, Object> getMigrationStats();
}