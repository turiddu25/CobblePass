package com.cobblemon.mdks.cobblepass.util;

import java.util.HashMap;
import java.util.Map;

import com.cobblemon.mdks.cobblepass.CobblePass;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minecraft.network.chat.Component;

public class LangManager {
    private static final MiniMessage miniMessage = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer legacySerializer = LegacyComponentSerializer.builder().hexColors().useUnusualXRepeatedCharacterHexFormat().build();

    private static final Map<String, String> translations = new HashMap<>();
    private static boolean loaded = false;

    public static void load() {
        translations.clear();
        
        String content = Utils.readFileSync(Constants.CONFIG_PATH, Constants.LANG_FILE);
        if (content == null || content.isEmpty()) {
            CobblePass.LOGGER.info("No lang.json found, using default English messages");
            loadDefaults();
            save(); // Create default lang.json
            return;
        }

        try {
            JsonObject json = JsonParser.parseString(content).getAsJsonObject();
            loadFromJson(json);
            loaded = true;
            CobblePass.LOGGER.info("Loaded " + translations.size() + " translations from lang.json");
        } catch (Exception e) {
            CobblePass.LOGGER.error("Failed to load lang.json, using defaults", e);
            loadDefaults();
        }
    }

    private static void loadFromJson(JsonObject json) {
        loadDefaults(); // Load defaults first as fallback
        
        // Override with custom translations
        for (String key : json.keySet()) {
            try {
                translations.put(key, json.get(key).getAsString());
            } catch (Exception e) {
                CobblePass.LOGGER.warn("Invalid translation for key: " + key);
            }
        }
    }

    private static void loadDefaults() {
    // ===================================
    //  GUI Text
    // ===================================
    translations.put("lang.gui.title", "Battle Pass");
    translations.put("lang.gui.decoration.border", " ");
    translations.put("lang.gui.tier_title", "<bold>Tier %d</bold>");
    translations.put("lang.gui.reward.free_label", "<bold>Free Reward</bold>");
    translations.put("lang.gui.reward.premium_label", "<bold>Premium Reward</bold>");
    translations.put("lang.gui.navigation.previous", "<gray>Previous Page</gray>");
    translations.put("lang.gui.navigation.next", "<gray>Next Page</gray>");

    // ===================================
    //  GUI - Progress & Info Panels
    // ===================================
    translations.put("lang.gui.progress.name", "<bold>Your Progress</bold>");
    translations.put("lang.gui.progress.level", "<gray>Level</gray> <yellow>%d</yellow>");
    translations.put("lang.gui.progress.xp", "<gray>XP:</gray> <yellow>%d/%d</yellow>");
    translations.put("lang.gui.progress.time_remaining", "<gray>Time Remaining:</gray> <yellow>%s</yellow>");
    translations.put("lang.gui.xp_info.name", "<bold>XP Sources</bold>");
    translations.put("lang.gui.xp_info.description", "<gray>Earn XP by completing various activities.</gray>");
    translations.put("lang.gui.info.title", "<aqua>Battle Pass Info</aqua>");

    // ===================================
    //  GUI - Premium Panel & Status
    // ===================================
    translations.put("lang.gui.premium.name", "<gold>Premium Pass</gold>");
    translations.put("lang.gui.premium.active", "<green>Active</green>");
    translations.put("lang.gui.premium.inactive", "<red>Inactive</red>");
    translations.put("lang.gui.premium.season", "<gray>Season:</gray> <yellow>%d</yellow>");
    translations.put("lang.gui.premium.no_season", "<gray>No active season</gray>");
    translations.put("lang.gui.premium.click_info", "<gray>Click for more info</gray>");
    translations.put("lang.gui.premium.command_info", "<gray>Use <yellow>/battlepass premium</yellow> to purchase.</gray>");

    // ===================================
    //  GUI - Item Lore Status Text
    // ===================================
    translations.put("lang.gui.status.in_lore.available", "<green>Available</green>");
    translations.put("lang.gui.status.in_lore.claimed", "<green>Claimed</green>");
    translations.put("lang.gui.status.in_lore.not_reached", "<red>Level not reached</red>");
    translations.put("lang.gui.status.in_lore.requires_premium", "<red>Requires Premium</red>");
    translations.put("lang.gui.status.in_lore.purchase_prompt", "<gray>Purchase at our store!</gray>");

    // ===================================
    //  Reward Text
    // ===================================
    translations.put("lang.reward.item", "<gray>Item</gray>");
    translations.put("lang.reward.item_format", "<white>%s <gray>x%d</gray>");
    translations.put("lang.reward.pokemon", "<gray>Pokemon</gray>");
    translations.put("lang.reward.level", "<gray>Level: %d</gray>");
    translations.put("lang.reward.shiny", "<gold>Shiny</gold>");

    // ===================================
    //  General Command Responses
    // ===================================
    translations.put("lang.command.must_be_player", "<red>[BattlePass]</red> This command must be run by a player!");
    translations.put("lang.command.config_reloaded", "<green>[BattlePass]</green> Configuration reloaded!");
    translations.put("lang.command.xp_gained", "<aqua>[BattlePass]</aqua> You gained <yellow>%d</yellow> XP! (<yellow>%d</yellow>/<yellow>%d</yellow>)");
    translations.put("lang.command.level_up", "<green>[BattlePass]</green> You reached level <yellow>%d</yellow>!");

    // ===================================
    //  Reward Claiming Commands
    // ===================================
    translations.put("lang.command.reward_claim", "<green>[BattlePass]</green> You claimed the reward for level <yellow>%d</yellow>!");
    translations.put("lang.command.no_reward", "<red>[BattlePass]</red> No reward available at level <yellow>%d</yellow>!");
    translations.put("lang.command.already_claimed", "<red>[BattlePass]</red> You already claimed this reward!");
    translations.put("lang.command.already_claimed_level", "<red>[BattlePass]</red> You already claimed the reward for level <yellow>%d</yellow>!");
    translations.put("lang.command.level_not_reached", "<red>[BattlePass]</red> You haven't reached level <yellow>%d</yellow> yet!");

    // ===================================
    //  Premium Commands
    // ===================================
    translations.put("lang.command.premium_unlocked", "<green>[BattlePass]</green> You unlocked the Premium Battle Pass!");
    translations.put("lang.command.not_premium", "<red>[BattlePass]</red> This is a premium reward! Use <yellow>/battlepass premium</yellow> to unlock.");
    translations.put("lang.command.no_premium_access", "You do not have premium access.");

    // ===================================
    //  Season State & Time
    // ===================================
    translations.put("lang.season.started", "<green>[BattlePass]</green> Battle Pass Season %d has begun!");
    translations.put("lang.season.already_active", "<red>[BattlePass]</red> A battle pass season is already active! (Season %d)");
    translations.put("lang.season.no_active", "<red>[BattlePass]</red> No battle pass season is currently active!");
    translations.put("lang.season.time_remaining", "<aqua>[BattlePass]</aqua> Season %d ends in %s");
    translations.put("lang.season.end.broadcast", "<red>[BattlePass]</red> Season {seasonNumber} has ended! Thank you for participating.");
    translations.put("lang.season.end.broadcast.detailed", "<red>[BattlePass]</red> Season {seasonNumber} has ended after {duration} days! Final stats: {totalPlayers} participants, {maxLevelReached} max level reached.");
    translations.put("lang.season.start.broadcast", "<green>[BattlePass]</green> Season {seasonNumber} has begun! Duration: {duration}, Max Level: {maxLevel}");
    translations.put("lang.season.start.broadcast.detailed", "<green>[BattlePass]</green> Welcome to Season {seasonNumber}! Duration: {duration} days, Max Level: {maxLevel}, Premium rewards available!");
    translations.put("lang.season.end.personal", "<red>[BattlePass]</red> Hello {playerName}, Season {seasonNumber} has ended! Your final level: {finalLevel}");
    translations.put("lang.season.start.personal", "<green>[BattlePass]</green> Welcome to Season {seasonNumber}, {playerName}! Good luck reaching level {maxLevel}!");
    translations.put("lang.season.transition.countdown", "<yellow>[BattlePass]</yellow> Season {seasonNumber} ends in {timeRemaining}! Make sure to claim your rewards.");
    translations.put("lang.season.transition.maintenance", "<gray>[BattlePass]</gray> Season transition in progress. Battle Pass temporarily unavailable.");

    // ===================================
    //  Season Reset
    // ===================================
    translations.put("lang.season.reset.confirm.title", "<red>[BattlePass]</red> Confirm Season Reset");
    translations.put("lang.season.reset.confirm.message", "<gray>Are you sure you want to end the current season? This will reset all player progress.</gray>");
    translations.put("lang.season.reset.confirm.warning", "<red><bold>WARNING:</bold></red> <gray>This action cannot be undone!</gray>");
    translations.put("lang.season.reset.confirm.impact", "<gray>This will affect <yellow>{playerCount}</yellow> players and reset <yellow>{totalProgress}</yellow> total progress.</gray>");
    translations.put("lang.season.reset.confirm.backup", "<gray>A backup will be created before the reset operation.</gray>");
    translations.put("lang.season.reset.confirm.premium", "<gray>Premium status preservation: <yellow>{preservationMode}</yellow></gray>");
    translations.put("lang.season.reset.confirm.proceed", "<green>Type 'CONFIRM' to proceed with the season reset.</green>");
    translations.put("lang.season.reset.confirm.cancel", "<gray>Type 'CANCEL' or wait 30 seconds to cancel.</gray>");
    translations.put("lang.season.reset.progress.starting", "<aqua>[BattlePass]</aqua> Initiating season reset operation...");
    translations.put("lang.season.reset.progress.backup", "<aqua>[BattlePass]</aqua> Creating player data backup... {progress}%");
    translations.put("lang.season.reset.progress.preserving", "<aqua>[BattlePass]</aqua> Preserving premium status... {progress}%");
    translations.put("lang.season.reset.progress.clearing", "<aqua>[BattlePass]</aqua> Clearing player progress... {progress}%");
    translations.put("lang.season.reset.progress.restoring", "<aqua>[BattlePass]</aqua> Restoring premium status... {progress}%");
    translations.put("lang.season.reset.progress.finalizing", "<aqua>[BattlePass]</aqua> Finalizing season reset... {progress}%");
    translations.put("lang.season.reset.progress.operation", "<aqua>[BattlePass]</aqua> Season reset in progress... {progress}% - {operation}");
    translations.put("lang.season.reset.progress.personal", "<aqua>[BattlePass]</aqua> Hi {playerName}, season reset is {progress}% complete.");
    translations.put("lang.season.reset.complete.title", "<green>[BattlePass]</green> Season Reset Complete!");
    translations.put("lang.season.reset.complete.summary", "<green>[BattlePass]</green> Season reset complete! {playerCount} players reset, {premiumCount} premium status preserved.");
    translations.put("lang.season.reset.complete.details", "<gray>Reset Summary: {totalPlayers} players, {premiumPreserved} premium preserved, {backupFiles} backup files created.</gray>");
    translations.put("lang.season.reset.complete.duration", "<gray>Operation completed in {duration} seconds.</gray>");
    translations.put("lang.season.reset.complete.backup_location", "<gray>Player data backup saved to: {backupPath}</gray>");
    translations.put("lang.season.reset.complete.new_season", "<gray>New season {newSeasonId} is now ready to start.</gray>");
    translations.put("lang.season.reset.complete.personal", "<green>[BattlePass]</green> Season reset complete, {playerName}! Welcome to Season {newSeason}.");

    // ===================================
    //  Season Premium Management
    // ===================================
    translations.put("lang.season.premium.preserved", "<green>[BattlePass]</green> Your premium status has been preserved for the new season!");
    translations.put("lang.season.premium.restored", "<green>[BattlePass]</green> Your premium access has been restored from permissions.");
    translations.put("lang.season.premium.restored.detailed", "<green>[BattlePass]</green> Welcome back, {playerName}! Your premium access has been restored for Season {seasonNumber}.");
    translations.put("lang.season.premium.preservation.started", "<aqua>[BattlePass]</aqua> Preserving premium status for {premiumCount} players...");
    translations.put("lang.season.premium.preservation.complete", "<green>[BattlePass]</green> Premium status preserved for {premiumCount} players.");
    translations.put("lang.season.premium.sync.started", "<aqua>[BattlePass]</aqua> Syncing premium status from permissions...");
    translations.put("lang.season.premium.sync.complete", "<green>[BattlePass]</green> Premium status synced for {syncedCount} players from permissions.");
    translations.put("lang.season.premium.lost", "<red>[BattlePass]</red> Your premium status was not preserved for the new season.");
    translations.put("lang.season.premium.lost.reason", "<gray>Reason: {reason}</gray>");
    translations.put("lang.season.premium.migration.success", "<green>[BattlePass]</green> {playerCount} premium players successfully migrated to new season.");
    translations.put("lang.season.premium.migration.partial", "<yellow>[BattlePass]</yellow> {successCount} of {totalCount} premium players migrated successfully. {failedCount} failed.");

    // ===================================
    //  Errors & Validation
    // ===================================
    translations.put("lang.season.reset.error.no_permission", "<red>[BattlePass]</red> You don't have permission to reset seasons.");
    translations.put("lang.season.reset.error.no_active_season", "<red>[BattlePass]</red> No active season to reset.");
    translations.put("lang.season.reset.error.already_in_progress", "<red>[BattlePass]</red> A season reset is already in progress.");
    translations.put("lang.season.reset.error.backup_failed", "<red>[BattlePass]</red> Failed to create player data backup. Reset cancelled.");
    translations.put("lang.season.reset.error.operation_failed", "<red>[BattlePass]</red> Season reset failed: {error}");
    translations.put("lang.season.reset.error.rollback_initiated", "<yellow>[BattlePass]</yellow> Reset failed. Initiating rollback to previous state...");
    translations.put("lang.season.reset.error.rollback_complete", "<green>[BattlePass]</green> Rollback complete. Player data restored to pre-reset state.");
    translations.put("lang.season.reset.error.rollback_failed", "<red>[BattlePass]</red> CRITICAL: Rollback failed! Manual intervention required. Backup location: {backupPath}");
    translations.put("lang.season.validation.filesystem_check", "<gray>[BattlePass]</gray> Validating file system permissions...");
    translations.put("lang.season.validation.config_check", "<gray>[BattlePass]</gray> Validating season configuration...");
    translations.put("lang.season.validation.player_data_check", "<gray>[BattlePass]</gray> Validating player data integrity...");
    translations.put("lang.season.validation.passed", "<green>[BattlePass]</green> All validation checks passed. Ready to proceed.");
    translations.put("lang.season.validation.failed", "<red>[BattlePass]</red> Validation failed: {reason}");
    translations.put("lang.season.validation.warning", "<yellow>[BattlePass]</yellow> Validation warning: {warning}");

    // ===================================
    //  XP Sources
    // ===================================
    translations.put("lang.gui.info.lore.catch", "<gray>- Catch Pokémon: </gray><yellow>%d XP</yellow>");
    translations.put("lang.gui.info.lore.defeat", "<gray>- Defeat Pokémon: </gray><yellow>%d XP</yellow>");
    translations.put("lang.gui.info.lore.evolve", "<gray>- Evolve Pokémon: </gray><yellow>%d XP</yellow>");
    translations.put("lang.gui.info.lore.hatch", "<gray>- Hatch Eggs: </gray><yellow>%d XP</yellow>");
    translations.put("lang.gui.info.lore.trade", "<gray>- Trade Pokémon: </gray><yellow>%d XP</yellow>");
    translations.put("lang.gui.info.lore.fish", "<gray>- Fish Pokémon: </gray><yellow>%d XP</yellow>");
    translations.put("lang.gui.info.lore.release", "<gray>- Release Pokémon: </gray><yellow>%d XP</yellow>");
    translations.put("lang.gui.info.lore.craft_vanilla", "<gray>- Craft Vanilla Items: </gray><yellow>%d XP</yellow>");
    translations.put("lang.gui.info.lore.craft_modded", "<gray>- Craft Modded Items: </gray><yellow>%d XP</yellow>");
    translations.put("lang.gui.info.lore.catch_legendary", "<gray>- Catch Legendary: </gray><yellow>%d XP</yellow>");
    translations.put("lang.gui.info.lore.catch_shiny", "<gray>- Catch Shiny: </gray><yellow>%d XP</yellow>");
    translations.put("lang.gui.info.lore.catch_ultrabeast", "<gray>- Catch Ultra Beast: </gray><yellow>%d XP</yellow>");
    translations.put("lang.gui.info.lore.catch_mythical", "<gray>- Catch Mythical: </gray><yellow>%d XP</yellow>");
    translations.put("lang.gui.info.lore.catch_paradox", "<gray>- Catch Paradox: </gray><yellow>%d XP</yellow>");
}

    public static void save() {
        JsonObject json = new JsonObject();
        
        // Save all current translations
        for (Map.Entry<String, String> entry : translations.entrySet()) {
            json.addProperty(entry.getKey(), entry.getValue());
        }

        Utils.writeFileSync(Constants.CONFIG_PATH, Constants.LANG_FILE,
                Utils.newGson().toJson(json));
    }

    public static String getRaw(String key, Object... args) {
        String translation = translations.get(key);
        
        // Fallback to key itself if translation not found
        if (translation == null) {
            CobblePass.LOGGER.warn("Missing translation for key: " + key);
            return "§c[MISSING] §f" + key;
        }
        
        if (args.length > 0) {
            try {
                return String.format(translation, args);
            } catch (Exception e) {
                CobblePass.LOGGER.warn("Invalid format string for key: " + key + " - " + e.getMessage());
                return translation + " " + java.util.Arrays.toString(args);
            }
        }
        
        return translation;
    }
    
    public static String getRaw(String key, Map<String, Object> placeholders) {
        String translation = getRaw(key);
        
        if (placeholders != null && !placeholders.isEmpty()) {
            for (Map.Entry<String, Object> entry : placeholders.entrySet()) {
                String placeholder = "{" + entry.getKey() + "}";
                String value = entry.getValue() != null ? entry.getValue().toString() : "";
                translation = translation.replace(placeholder, value);
            }
        }
        
        return translation;
    }

    public static Component get(String key, Object... args) {
        String formattedString = getRaw(key, args);
        return asComponent(formattedString);
    }

    public static Component get(String key, Map<String, Object> placeholders) {
        String formattedString = getRaw(key, placeholders);
        return asComponent(formattedString);
    }

    public static Component asComponent(String text) {
        net.kyori.adventure.text.Component adventureComponent = miniMessage.deserialize(text);
        return CobblePass.getAdventure().toNative(adventureComponent);
    }

    // public static String componentToString(Component component) {
    //     net.kyori.adventure.text.Component adventureComponent = CobblePass.getAdventure().fromNative(component);
    //     return miniMessage.serialize(adventureComponent);
    // }

    public static String getLegacy(String key, Object... args) {
        String formattedString = getRaw(key, args);
        net.kyori.adventure.text.Component adventureComponent = miniMessage.deserialize(formattedString);
        return legacySerializer.serialize(adventureComponent);
    }

    public static boolean isLoaded() {
        return loaded;
    }

    public static void reload() {
        load();
    }
}
