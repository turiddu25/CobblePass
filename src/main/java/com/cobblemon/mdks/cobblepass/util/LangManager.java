package com.cobblemon.mdks.cobblepass.util;

import com.cobblemon.mdks.cobblepass.CobblePass;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.network.chat.Component;

import java.util.HashMap;
import java.util.Map;

public class LangManager {
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
        // Command messages
        translations.put("lang.command.xp_gained", "§b[BattlePass] §fYou gained §e%d§f XP! (§e%d§f/§e%d§f)");
        translations.put("lang.command.level_up", "§a[BattlePass] §fYou reached level §e%d§f!");
        translations.put("lang.command.reward_claim", "§a[BattlePass] §fYou claimed the reward for level §e%d§f!");
        translations.put("lang.command.premium_unlocked", "§a[BattlePass] §fYou unlocked the Premium Battle Pass!");
        translations.put("lang.command.no_reward", "§c[BattlePass] §fNo reward available at level §e%d§f!");
        translations.put("lang.command.already_claimed", "§c[BattlePass] §fYou already claimed this reward!");
        translations.put("lang.command.already_claimed_level", "§c[BattlePass] §fYou already claimed the reward for level §e%d§f!");
        translations.put("lang.command.level_not_reached", "§c[BattlePass] §fYou haven't reached level §e%d§f yet!");
        translations.put("lang.command.not_premium", "§c[BattlePass] §fThis is a premium reward! Use §e/battlepass premium§f to unlock.");
        translations.put("lang.command.config_reloaded", "§a[BattlePass] §fConfiguration reloaded!");
        translations.put("lang.command.must_be_player", "§c[BattlePass] §fThis command must be run by a player!");

        // Season messages
        translations.put("lang.season.started", "§a[BattlePass] §fBattle Pass Season %d has begun!");
        translations.put("lang.season.already_active", "§c[BattlePass] §fA battle pass season is already active! (Season %d)");
        translations.put("lang.season.no_active", "§c[BattlePass] §fNo battle pass season is currently active!");
        translations.put("lang.season.time_remaining", "§b[BattlePass] §fSeason %d ends in %s");

        // GUI elements
        translations.put("lang.gui.title", "§3Battle Pass");
        translations.put("lang.gui.xp_info.name", "§aHow to Earn XP");
        translations.put("lang.gui.xp_info.description", "§7Earn XP by performing these actions:");
        translations.put("lang.gui.xp_info.catch", "  §b• Catch Pokémon: §e+%d XP");
        translations.put("lang.gui.xp_info.defeat", "  §b• Defeat Pokémon: §e+%d XP");
        translations.put("lang.gui.xp_info.evolve", "  §b• Evolve Pokémon: §e+%d XP");
        translations.put("lang.gui.xp_info.hatch", "  §b• Hatch Eggs: §e+%d XP");
        translations.put("lang.gui.xp_info.trade", "  §b• Trade Pokémon: §e+%d XP");

        translations.put("lang.gui.progress.name", "§bBattle Pass Progress");
        translations.put("lang.gui.progress.level", "§3Level: §f%d");
        translations.put("lang.gui.progress.xp", "§3XP: §f%d§7/§f%d");
        translations.put("lang.gui.progress.time_remaining", "§3Time Remaining: §b%s");

        translations.put("lang.gui.premium.name", "§6Premium Status");
        translations.put("lang.gui.premium.active", "§aYou have the Premium Pass!");
        translations.put("lang.gui.premium.inactive", "§cPremium Pass Inactive");
        translations.put("lang.gui.premium.click_info", "§7Click to learn more!");
        translations.put("lang.gui.premium.season", "§3Season %d");
        translations.put("lang.gui.premium.no_season", "§cNo active season");
        translations.put("lang.gui.premium.command_info", "§7Use §f/battlepass premium §7to unlock premium rewards.");

        translations.put("lang.gui.reward.free", "§aFree Reward");
        translations.put("lang.gui.reward.premium", "§6Premium Reward");
        translations.put("lang.gui.reward.free_label", "§aFree Rewards");
        translations.put("lang.gui.reward.premium_label", "§6Premium Rewards");

        translations.put("lang.gui.status.name", "§3Level %d Status");
        translations.put("lang.gui.status.requires_premium", "§cRequires Premium Pass");
        translations.put("lang.gui.status.purchase_info", "§7Purchase with §e/bp premium buy");
        translations.put("lang.gui.status.not_reached", "§7Not Reached");
        translations.put("lang.gui.status.claimed", "§6Claimed");
        translations.put("lang.gui.status.available", "§aAvailable to Claim");

        // New integrated status keys
        translations.put("lang.gui.tier_title", "§aTier %d");
        translations.put("lang.gui.status.in_lore.claimed", "§aStatus: Claimed");
        translations.put("lang.gui.status.in_lore.available", "§eStatus: Available to Claim");
        translations.put("lang.gui.status.in_lore.not_reached", "§cStatus: Locked (Requires Level %d)");
        translations.put("lang.gui.status.in_lore.requires_premium", "§6Status: Premium Locked");
        translations.put("lang.gui.status.in_lore.purchase_prompt", "§7Purchase the pass to unlock.");

        translations.put("lang.gui.navigation.previous", "§f← Previous Page");
        translations.put("lang.gui.navigation.next", "§fNext Page →");

        translations.put("lang.gui.decoration.border", "§7 ");

        // Reward types
        translations.put("lang.reward.item", "§7Item");
        translations.put("lang.reward.pokemon", "§7Pokemon");
        translations.put("lang.reward.level", "§7Level: §f%d");
        translations.put("lang.reward.shiny", "§6✦ Shiny");
        translations.put("lang.reward.mod_item", "§8%s Item");
        translations.put("lang.reward.count", "§7%dx %s");
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

    public static String get(String key, Object... args) {
        String translation = translations.get(key);
        
        // Fallback to key itself if translation not found
        if (translation == null) {
            CobblePass.LOGGER.warn("Missing translation for key: " + key);
            translation = key.replace("lang.", "").replace("_", " ").replace(".", " ");
            // Basic formatting for missing keys
            String[] words = translation.split(" ");
            StringBuilder formatted = new StringBuilder();
            for (String word : words) {
                if (formatted.length() > 0) formatted.append(" ");
                formatted.append(word.substring(0, 1).toUpperCase()).append(word.substring(1));
            }
            translation = "§c[MISSING] §f" + formatted.toString();
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

    public static Component getComponent(String key, Object... args) {
        return Component.literal(get(key, args));
    }

    public static boolean isLoaded() {
        return loaded;
    }

    public static void reload() {
        load();
    }
}