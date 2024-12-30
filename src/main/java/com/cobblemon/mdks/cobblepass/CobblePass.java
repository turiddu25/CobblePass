package com.cobblemon.mdks.cobblepass;

import com.cobblemon.mdks.cobblepass.battlepass.BattlePass;
import com.cobblemon.mdks.cobblepass.command.BattlePassCommand;
import com.cobblemon.mdks.cobblepass.config.Config;
import com.cobblemon.mdks.cobblepass.util.CommandsRegistry;
import com.cobblemon.mdks.cobblepass.util.Permissions;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import com.cobblemon.mdks.cobblepass.listeners.CatchPokemonListener;
import com.cobblemon.mdks.cobblepass.listeners.DefeatPokemonListener;
import com.cobblemon.mod.common.api.Priority;
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

        // Initialize battle pass (tiers are loaded in constructor)
        battlePass = new BattlePass();

        // Add commands to registry
        CommandsRegistry.addCommand(new BattlePassCommand());

        // Register commands with Fabric
        net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback.EVENT.register(
            (dispatcher, registryAccess, environment) -> CommandsRegistry.registerCommands(dispatcher)
        );

        LOGGER.info("CobblePass initialized");

        // Register event listeners
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            CobblePass.server = server;
            CatchPokemonListener.register();
            DefeatPokemonListener.register();
        });
    }

    public static void reload() {
        config.load();
        permissions = new Permissions();
        if (battlePass == null) {
            battlePass = new BattlePass();
        } else {
            battlePass.reloadTiers(); // Only reload tier configuration without affecting player data
        }
        LOGGER.info("CobblePass reloaded");
    }
}
