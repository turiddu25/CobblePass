package com.cobblemon.mdks.fabric.util;

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
    
    // Config Paths
    public static final String CONFIG_DIR = "config/cobblepass";
    public static final String CONFIG_PATH = CONFIG_DIR;
    public static final String CONFIG_FILE = "config.json";
    public static final String TIERS_FILE = "tiers.json";
    public static final String PLAYERS_PATH = CONFIG_DIR + "/players";
    public static final String PLAYER_DATA_DIR = PLAYERS_PATH;
    
    // Version Info
    public static final String CONFIG_VERSION = "1.0";
    public static final String PLAYER_DATA_VERSION = "1.0";
    
    // Battle Pass Constants
    public static final int MAX_LEVEL = 100;
    public static final int XP_PER_LEVEL = 1000;
    public static final double XP_MULTIPLIER = 1.1;
    
    // Default Values
    public static final int DEFAULT_MAX_LEVEL = 100;
    public static final int DEFAULT_XP_PER_LEVEL = 1000;
    public static final long DEFAULT_PREMIUM_COST = 1000;
    public static final long DEFAULT_PREMIUM_DURATION = 30 * 24 * 60 * 60 * 1000L; // 30 days in milliseconds
    public static final boolean DEFAULT_ENABLE_PERMISSION_NODES = true;
    
    // Time Constants
    public static final long MILLIS_PER_SECOND = 1000L;
    public static final long MILLIS_PER_MINUTE = MILLIS_PER_SECOND * 60;
    public static final long MILLIS_PER_HOUR = MILLIS_PER_MINUTE * 60;
    public static final long MILLIS_PER_DAY = MILLIS_PER_HOUR * 24;
    
    // Battle Pass Messages
    public static final String MSG_XP_GAINED = PREFIX + "You gained §e%d§f XP! (§e%d§f/§e%d§f)";
    public static final String MSG_LEVEL_UP = SUCCESS_PREFIX + "You reached level §e%d§f!";
    public static final String MSG_REWARD_CLAIM = SUCCESS_PREFIX + "You claimed the reward for level §e%d§f!";
    public static final String MSG_PREMIUM_UNLOCKED = SUCCESS_PREFIX + "You unlocked the Premium Battle Pass!";
    public static final String MSG_NO_REWARD = ERROR_PREFIX + "No reward available at level §e%d§f!";
    public static final String MSG_ALREADY_CLAIMED = ERROR_PREFIX + "You already claimed this reward!";
    public static final String MSG_NOT_PREMIUM = ERROR_PREFIX + "This is a premium reward! Use §e/battlepass premium§f to unlock.";
    public static final String MSG_CONFIG_RELOADED = SUCCESS_PREFIX + "Configuration reloaded!";
}