package com.cobblemon.mdks.cobblepass.util;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.permission.CobblemonPermission;
import com.cobblemon.mod.common.api.permission.PermissionLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles permission management for the battle pass system
 */
public class Permissions {
    private final Map<String, CobblemonPermission> permissions;

    public Permissions() {
        this.permissions = new HashMap<>();
        registerPermissions();
    }

    /**
     * Register all permissions used by the mod
     */
    private void registerPermissions() {
        // Base command permission
        register(Constants.PERM_COMMAND_BASE, PermissionLevel.NONE);
        
        // Admin permissions
        register(Constants.PERM_COMMAND_ADMIN, PermissionLevel.CHEAT_COMMANDS_AND_COMMAND_BLOCKS);
        register(Constants.PERM_COMMAND_RELOAD, PermissionLevel.CHEAT_COMMANDS_AND_COMMAND_BLOCKS);
        
        // Subcommand permissions
        register("battlepass.claim", PermissionLevel.NONE);
        register("battlepass.view", PermissionLevel.NONE);
        register("battlepass.addxp", PermissionLevel.CHEAT_COMMANDS_AND_COMMAND_BLOCKS);
        register("battlepass.premium", PermissionLevel.CHEAT_COMMANDS_AND_COMMAND_BLOCKS);
    }

    /**
     * Register a new permission
     */
    private void register(String node, PermissionLevel level) {
        permissions.put(node, new CobblemonPermission(node, level));
    }

    /**
     * Get a permission by its node
     */
    public CobblemonPermission getPermission(String node) {
        return permissions.get(node);
    }

    /**
     * Check if a player has a permission
     */
    public boolean hasPermission(ServerPlayer player, CobblemonPermission permission) {
        return Cobblemon.INSTANCE.getPermissionValidator().hasPermission(player, permission);
    }

    /**
     * Check if a player has a permission by node
     */
    public boolean hasPermission(ServerPlayer player, String node) {
        CobblemonPermission permission = getPermission(node);
        return permission != null && hasPermission(player, permission);
    }
}
