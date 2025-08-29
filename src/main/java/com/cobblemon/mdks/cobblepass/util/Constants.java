package com.cobblemon.mdks.cobblepass.util;

import java.util.Arrays;
import java.util.List;

public class Constants {
    // Mod Info
    public static final String MOD_ID = "cobblepass";
    public static final String MOD_NAME = "Cobblemon Battle Pass";
    
    // Command Info
    public static final List<String> COMMAND_ALIASES = Arrays.asList("bp", "pass");
    
    // Message Prefixes
    public static final String PREFIX = "§b[BattlePass] §f";
    public static final String ERROR_PREFIX = "§c[BattlePass] §f";
    public static final String SUCCESS_PREFIX = "§a[BattlePass] §f";
    
    // Permission Nodes
    public static final String PERM_COMMAND_BASE = "battlepass.command";
    public static final String PERM_COMMAND_ADMIN = "battlepass.admin";
    public static final String PERM_COMMAND_RELOAD = "battlepass.reload";
    public static final String PERM_COMMAND_START_SEASON = "battlepass.season.start";
    
    // Config Paths
    public static final String CONFIG_DIR = "config/cobblepass";
    public static final String CONFIG_PATH = CONFIG_DIR;
    public static final String CONFIG_FILE = "config.json";
    public static final String TIERS_FILE = "tiers.json";
    public static final String LANG_FILE = "lang.json";
    public static final String GUI_FILE = "gui.json";
    public static final String PLAYERS_PATH = CONFIG_DIR + "/players";
    public static final String PLAYER_DATA_DIR = PLAYERS_PATH;
    
    // Version Info
    public static final String CONFIG_VERSION = "1.0";
    public static final String PLAYER_DATA_VERSION = "1.0";
    
    // Battle Pass Constants
    public static final int MAX_LEVEL = 10;
    
    // Default Values
    public static final int DEFAULT_MAX_LEVEL = 10;
    public static final int DEFAULT_CATCH_XP = 100;
    public static final int DEFAULT_DEFEAT_XP = 50;
    public static final int DEFAULT_EVOLVE_XP = 75;
    public static final int DEFAULT_HATCH_XP = 50;
    public static final int DEFAULT_TRADE_XP = 25;
    public static final int DEFAULT_FISH_XP = 20;
    public static final int DEFAULT_CATCH_LEGENDARY_XP = 500;
    public static final int DEFAULT_CATCH_SHINY_XP = 250;
    public static final int DEFAULT_CATCH_ULTRABEAST_XP = 300;
    public static final int DEFAULT_CATCH_MYTHICAL_XP = 400;
    public static final int DEFAULT_CATCH_PARADOX_XP = 200;
    public static final int DEFAULT_RELEASE_XP = 10;
    public static final int DEFAULT_CRAFT_VANILLA_XP = 5;
    public static final int DEFAULT_CRAFT_MODDED_XP = 10;
    public static final long DEFAULT_PREMIUM_COST = 1000;
    public static final boolean DEFAULT_ENABLE_PERMISSION_NODES = true;
    
    // Time Constants
    public static final long MILLIS_PER_SECOND = 1000L;
    public static final long MILLIS_PER_MINUTE = MILLIS_PER_SECOND * 60;
    public static final long MILLIS_PER_HOUR = MILLIS_PER_MINUTE * 60;
    public static final long MILLIS_PER_DAY = MILLIS_PER_HOUR * 24;
    
    // Battle Pass Messages (now using localization keys)
    public static final String MSG_XP_GAINED = "lang.command.xp_gained";
    public static final String MSG_LEVEL_UP = "lang.command.level_up";
    public static final String MSG_REWARD_CLAIM = "lang.command.reward_claim";
    public static final String MSG_PREMIUM_UNLOCKED = "lang.command.premium_unlocked";
    public static final String MSG_NO_REWARD = "lang.command.no_reward";
    public static final String MSG_ALREADY_CLAIMED = "lang.command.already_claimed";
    public static final String MSG_ALREADY_CLAIMED_LEVEL = "lang.command.already_claimed_level";
    public static final String MSG_LEVEL_NOT_REACHED = "lang.command.level_not_reached";
    public static final String MSG_NOT_PREMIUM = "lang.command.not_premium";
    public static final String MSG_CONFIG_RELOADED = "lang.command.config_reloaded";
    
    // Season Messages (now using localization keys)
    public static final String MSG_SEASON_STARTED = "lang.season.started";
    public static final String MSG_SEASON_ALREADY_ACTIVE = "lang.season.already_active";
    public static final String MSG_NO_ACTIVE_SEASON = "lang.season.no_active";
    public static final String MSG_SEASON_TIME_REMAINING = "lang.season.time_remaining";
    
    // Season Reset Messages
    public static final String MSG_SEASON_RESET_CONFIRM = "lang.season.reset.confirm.message";
    public static final String MSG_SEASON_RESET_PROGRESS = "lang.season.reset.progress.operation";
    public static final String MSG_SEASON_RESET_COMPLETE = "lang.season.reset.complete.summary";
    public static final String MSG_SEASON_END_BROADCAST = "lang.season.end.broadcast";
    public static final String MSG_SEASON_START_BROADCAST = "lang.season.start.broadcast";
    
    // Premium Preservation Messages
    public static final String MSG_PREMIUM_PRESERVED = "lang.season.premium.preserved";
    public static final String MSG_PREMIUM_RESTORED = "lang.season.premium.restored";
    public static final String MSG_PREMIUM_SYNC_COMPLETE = "lang.season.premium.sync.complete";
}
