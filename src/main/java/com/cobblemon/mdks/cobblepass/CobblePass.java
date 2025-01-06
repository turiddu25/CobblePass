package com.cobblemon.mdks.cobblepass;

import com.cobblemon.mdks.cobblepass.battlepass.BattlePass;
import com.cobblemon.mdks.cobblepass.command.BattlePassCommand;
import com.cobblemon.mdks.cobblepass.config.Config;
import com.cobblemon.mdks.cobblepass.util.CommandsRegistry;
import com.cobblemon.mdks.cobblepass.util.Permissions;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
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

        // Initialize battle pass and load all existing player data
        battlePass = new BattlePass();
        battlePass.init(); // Important: Initialize battle pass data

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

        // Register player join event
        ServerPlayConnectionEvents.JOIN.register((handler, sender, mcServer) -> {
            // Load player's battle pass data when they join
            CobblePass.battlePass.loadPlayerPass(handler.getPlayer().getUUID().toString());
            CobblePass.LOGGER.info("Loaded battle pass data for player " + handler.getPlayer().getName().getString());
        });

        // Register player disconnect event
        ServerPlayConnectionEvents.DISCONNECT.register((handler, mcServer) -> {
            // Save player's battle pass data when they leave
            String playerName = handler.getPlayer().getName().getString();
            String uuid = handler.getPlayer().getUUID().toString();
            CobblePass.battlePass.savePlayerPass(uuid);
            CobblePass.LOGGER.info("Saved battle pass data for player " + playerName);
        });

        // Register server stopping event to save all data
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            CobblePass.battlePass.save();
            CobblePass.LOGGER.info("Saved all battle pass data");
        });
    }

    public static void reload() {
        config.load();
        permissions = new Permissions();
        if (battlePass == null) {
            battlePass = new BattlePass();
            battlePass.init();
        } else {
            battlePass.reloadTiers(); // Only reload tier configuration without affecting player data
        }
        LOGGER.info("CobblePass reloaded");
    }
}
