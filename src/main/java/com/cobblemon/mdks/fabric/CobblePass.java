package com.cobblemon.mdks.fabric;

import com.cobblemon.mdks.fabric.battlepass.BattlePass;
import com.cobblemon.mdks.fabric.config.Config;
import com.cobblemon.mdks.fabric.util.CommandsRegistry;
import com.cobblemon.mdks.fabric.util.Permissions;
import net.fabricmc.api.ModInitializer;
import net.minecraft.server.MinecraftServer;
import org.slf4j.LoggerFactory;

public class CobblePass implements ModInitializer {
    public static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger("cobblepass");
    public static Config config;
    public static Permissions permissions;
    public static BattlePass battlePass;
    public static MinecraftServer server;

    @Override
    public void onInitialize() {
        // Load config
        config = new Config();
        config.load();

        // Initialize permissions
        permissions = new Permissions();

        // Initialize battle pass
        battlePass = new BattlePass();
        battlePass.loadTiers();

        // Register commands
        net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback.EVENT.register(
            (dispatcher, registryAccess, environment) -> CommandsRegistry.registerCommands(dispatcher)
        );

        LOGGER.info("CobblePass initialized");
    }

    public static void reload() {
        config.load();
        permissions = new Permissions();
        battlePass.loadTiers();
        LOGGER.info("CobblePass reloaded");
    }
}
