package com.cobblemon.mdks.cobblepass;

import org.slf4j.LoggerFactory;

import com.cobblemon.mdks.cobblepass.battlepass.BattlePass;
import com.cobblemon.mdks.cobblepass.command.BattlePassCommand;
import com.cobblemon.mdks.cobblepass.config.Config;
import com.cobblemon.mdks.cobblepass.listeners.CatchPokemonListener;
import com.cobblemon.mdks.cobblepass.listeners.DefeatPokemonListener;
import com.cobblemon.mdks.cobblepass.listeners.EvolvePokemonListener;
import com.cobblemon.mdks.cobblepass.listeners.FishPokemonListener;
import com.cobblemon.mdks.cobblepass.listeners.HatchPokemonListener;
import com.cobblemon.mdks.cobblepass.listeners.ReleasePokemonListener;
import com.cobblemon.mdks.cobblepass.listeners.TradePokemonListener;
import com.cobblemon.mdks.cobblepass.premium.PremiumManager;
import com.cobblemon.mdks.cobblepass.util.CommandsRegistry;
import com.cobblemon.mdks.cobblepass.util.Permissions;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.kyori.adventure.platform.fabric.FabricServerAudiences;
import net.minecraft.server.MinecraftServer;

public class CobblePass implements ModInitializer {
    public static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger("cobblepass");
    public static Config config;
    public static Permissions permissions;
    public static BattlePass battlePass;
    public static MinecraftServer server;
    private static FabricServerAudiences adventure;

    @Override
    public void onInitialize() {
        // Register server starting event to load configs after all mods are loaded
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            CobblePass.server = server;
            CobblePass.adventure = FabricServerAudiences.of(server);

            config = new Config();
            config.load();

            permissions = new Permissions();

            // Initialize premium manager after config is loaded
            PremiumManager.getInstance().reload();

            battlePass = new BattlePass();
            battlePass.init();

            LOGGER.info("CobblePass configuration loaded.");
        });

        // Add commands to registry
        CommandsRegistry.addCommand(new BattlePassCommand());

        // Register commands with Fabric
        net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback.EVENT.register(
            (dispatcher, registryAccess, environment) -> CommandsRegistry.registerCommands(dispatcher)
        );

        LOGGER.info("CobblePass initialized");

        // Register event listeners
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            CatchPokemonListener.register();
            DefeatPokemonListener.register();
            EvolvePokemonListener.register();
            HatchPokemonListener.register();
            TradePokemonListener.register();
            FishPokemonListener.register();
            ReleasePokemonListener.register();
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
            try {
                CobblePass.battlePass.save();
                // Force save configuration data as well
                if (CobblePass.config != null) {
                    CobblePass.config.save();
                }
                CobblePass.LOGGER.info("Saved all battle pass data and configuration");
            } catch (Exception e) {
                CobblePass.LOGGER.error("Failed to save battle pass data during server shutdown", e);
            }
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            if (CobblePass.adventure != null) {
                CobblePass.adventure.close();
                CobblePass.adventure = null;
            }
        });
    }

    public static void reload() {
        config.load();
        permissions = new Permissions();
        
        // Reload premium manager with updated configuration
        PremiumManager.getInstance().reload();
        
        if (battlePass == null) {
            battlePass = new BattlePass();
            battlePass.init();
        } else {
            battlePass.reloadTiers(); // Only reload tier configuration without affecting player data
            battlePass.reloadOnlinePlayers();
        }
        LOGGER.info("CobblePass reloaded - GUI and language configurations updated");
    }

    public static void resetInstance() {
        LOGGER.info("Resetting CobblePass configuration and data in memory...");
        
        // Create new, empty objects to replace the old ones in memory
        config = new Config();
        if (battlePass != null) {
            battlePass.resetData();
        } else {
            battlePass = new BattlePass();
        }
        
        LOGGER.info("CobblePass has been reset. Please create a new Battle Pass to continue.");
    }

    /**
     * Gets the current Minecraft server instance.
     * @return The server instance, or null if not initialized
     */
    public static MinecraftServer getServer() {
        return server;
    }

    public static FabricServerAudiences getAdventure() {
        if (adventure == null) {
            throw new IllegalStateException("Tried to access Adventure without a running server!");
        }
        return adventure;
    }
}
