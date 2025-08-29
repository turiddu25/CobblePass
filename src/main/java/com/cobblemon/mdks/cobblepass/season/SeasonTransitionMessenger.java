package com.cobblemon.mdks.cobblepass.season;

import com.cobblemon.mdks.cobblepass.CobblePass;
import com.cobblemon.mdks.cobblepass.util.LangManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Handles broadcasting of season transition messages to players.
 * Supports customizable messages with placeholder replacement and personalized messaging.
 */
public class SeasonTransitionMessenger {
    private static SeasonTransitionMessenger instance;
    
    // Message types for different transition events
    public enum MessageType {
        SEASON_END,
        SEASON_START,
        PREMIUM_PRESERVED,
        PREMIUM_RESTORED,
        RESET_PROGRESS,
        RESET_COMPLETE
    }
    
    private SeasonTransitionMessenger() {
    }
    
    public static SeasonTransitionMessenger getInstance() {
        if (instance == null) {
            instance = new SeasonTransitionMessenger();
        }
        return instance;
    }
    
    /**
     * Broadcasts a season end message to all online players.
     * 
     * @param seasonNumber The season number that is ending
     */
    public void broadcastSeasonEnd(int seasonNumber) {
        broadcastSeasonEnd(seasonNumber, null);
    }
    
    /**
     * Broadcasts a season end message to all online players with additional info.
     * 
     * @param seasonNumber The season number that is ending
     * @param additionalInfo Additional information about the season end
     */
    public void broadcastSeasonEnd(int seasonNumber, String additionalInfo) {
        try {
            MinecraftServer server = CobblePass.getServer();
            if (server == null) {
                CobblePass.LOGGER.warn("Cannot broadcast season end message - server not available");
                return;
            }
            
            // Create message with placeholders replaced
            Map<String, Object> placeholders = new HashMap<>();
            placeholders.put("seasonNumber", seasonNumber);
            placeholders.put("season", seasonNumber);
            if (additionalInfo != null) {
                placeholders.put("info", additionalInfo);
            }
            
            String message = getMessageWithPlaceholders("lang.season.end.broadcast", placeholders);
            Component messageComponent = Component.literal(message);
            
            // Broadcast to all online players
            List<ServerPlayer> players = server.getPlayerList().getPlayers();
            for (ServerPlayer player : players) {
                player.sendSystemMessage(messageComponent);
            }
            
            CobblePass.LOGGER.info("Broadcasted season end message to " + players.size() + " players");
            
        } catch (Exception e) {
            CobblePass.LOGGER.error("Failed to broadcast season end message", e);
        }
    }
    
    /**
     * Broadcasts a season start message to all online players.
     * 
     * @param seasonNumber The season number that is starting
     * @param seasonInfo Information about the new season
     */
    public void broadcastSeasonStart(int seasonNumber, SeasonInfo seasonInfo) {
        try {
            MinecraftServer server = CobblePass.getServer();
            if (server == null) {
                CobblePass.LOGGER.warn("Cannot broadcast season start message - server not available");
                return;
            }
            
            // Create message with placeholders replaced
            Map<String, Object> placeholders = new HashMap<>();
            placeholders.put("seasonNumber", seasonNumber);
            placeholders.put("season", seasonNumber);
            
            if (seasonInfo != null) {
                placeholders.put("maxLevel", seasonInfo.getMaxLevel());
                placeholders.put("duration", formatDuration(seasonInfo.getDurationDays()));
                placeholders.put("seasonName", seasonInfo.getSeasonName());
                placeholders.put("startDate", formatDate(seasonInfo.getStartDate()));
                if (seasonInfo.getEndDate() != null) {
                    placeholders.put("endDate", formatDate(seasonInfo.getEndDate()));
                }
            }
            
            String message = getMessageWithPlaceholders("lang.season.start.broadcast", placeholders);
            Component messageComponent = Component.literal(message);
            
            // Broadcast to all online players
            List<ServerPlayer> players = server.getPlayerList().getPlayers();
            for (ServerPlayer player : players) {
                player.sendSystemMessage(messageComponent);
            }
            
            CobblePass.LOGGER.info("Broadcasted season start message to " + players.size() + " players");
            
        } catch (Exception e) {
            CobblePass.LOGGER.error("Failed to broadcast season start message", e);
        }
    }
    
    /**
     * Sends personalized messages to specific players.
     * 
     * @param player The player to send the message to
     * @param messageType The type of message to send
     */
    public void sendPersonalizedMessage(ServerPlayer player, MessageType messageType) {
        sendPersonalizedMessage(player, messageType, new HashMap<>());
    }
    
    /**
     * Sends personalized messages to specific players with custom placeholders.
     * 
     * @param player The player to send the message to
     * @param messageType The type of message to send
     * @param customPlaceholders Additional placeholders for the message
     */
    public void sendPersonalizedMessage(ServerPlayer player, MessageType messageType, Map<String, Object> customPlaceholders) {
        try {
            if (player == null) {
                CobblePass.LOGGER.warn("Cannot send personalized message - player is null");
                return;
            }
            
            String messageKey = getMessageKeyForType(messageType);
            if (messageKey == null) {
                CobblePass.LOGGER.warn("Unknown message type: " + messageType);
                return;
            }
            
            // Add player-specific placeholders
            Map<String, Object> placeholders = new HashMap<>(customPlaceholders);
            placeholders.put("playerName", player.getName().getString());
            placeholders.put("player", player.getName().getString());
            
            String message = getMessageWithPlaceholders(messageKey, placeholders);
            Component messageComponent = Component.literal(message);
            
            player.sendSystemMessage(messageComponent);
            
            CobblePass.LOGGER.debug("Sent personalized message to " + player.getName().getString() + ": " + messageType);
            
        } catch (Exception e) {
            CobblePass.LOGGER.error("Failed to send personalized message to " + 
                                  (player != null ? player.getName().getString() : "unknown"), e);
        }
    }
    
    /**
     * Sends personalized messages to multiple players.
     * 
     * @param playerUUIDs List of player UUIDs to send messages to
     * @param messageType The type of message to send
     * @param customPlaceholders Additional placeholders for the message
     */
    public void sendPersonalizedMessages(List<UUID> playerUUIDs, MessageType messageType, Map<String, Object> customPlaceholders) {
        try {
            MinecraftServer server = CobblePass.getServer();
            if (server == null) {
                CobblePass.LOGGER.warn("Cannot send personalized messages - server not available");
                return;
            }
            
            int messagesSent = 0;
            for (UUID playerUUID : playerUUIDs) {
                ServerPlayer player = server.getPlayerList().getPlayer(playerUUID);
                if (player != null) {
                    sendPersonalizedMessage(player, messageType, customPlaceholders);
                    messagesSent++;
                }
            }
            
            CobblePass.LOGGER.info("Sent " + messagesSent + " personalized messages of type " + messageType);
            
        } catch (Exception e) {
            CobblePass.LOGGER.error("Failed to send personalized messages", e);
        }
    }
    
    /**
     * Broadcasts a progress update message during reset operations.
     * 
     * @param progress Progress percentage (0-100)
     * @param currentOperation Description of current operation
     */
    public void broadcastResetProgress(int progress, String currentOperation) {
        try {
            MinecraftServer server = CobblePass.getServer();
            if (server == null) {
                return; // Silently fail during reset operations
            }
            
            Map<String, Object> placeholders = new HashMap<>();
            placeholders.put("progress", progress);
            placeholders.put("operation", currentOperation);
            
            String message = getMessageWithPlaceholders("lang.season.reset.progress", placeholders);
            Component messageComponent = Component.literal(message);
            
            // Send to all online players
            List<ServerPlayer> players = server.getPlayerList().getPlayers();
            for (ServerPlayer player : players) {
                player.sendSystemMessage(messageComponent);
            }
            
        } catch (Exception e) {
            CobblePass.LOGGER.error("Failed to broadcast reset progress", e);
        }
    }
    
    /**
     * Broadcasts a reset completion message with summary.
     * 
     * @param summary The reset operation summary
     */
    public void broadcastResetComplete(SeasonResetSummary summary) {
        try {
            MinecraftServer server = CobblePass.getServer();
            if (server == null) {
                CobblePass.LOGGER.warn("Cannot broadcast reset complete message - server not available");
                return;
            }
            
            Map<String, Object> placeholders = new HashMap<>();
            placeholders.put("playerCount", summary.getTotalPlayersReset());
            placeholders.put("premiumCount", summary.getPremiumPlayersPreserved());
            placeholders.put("previousSeason", summary.getPreviousSeasonId());
            placeholders.put("newSeason", summary.getNewSeasonId());
            
            String message = getMessageWithPlaceholders("lang.season.reset.complete", placeholders);
            Component messageComponent = Component.literal(message);
            
            // Broadcast to all online players
            List<ServerPlayer> players = server.getPlayerList().getPlayers();
            for (ServerPlayer player : players) {
                player.sendSystemMessage(messageComponent);
            }
            
            CobblePass.LOGGER.info("Broadcasted reset complete message to " + players.size() + " players");
            
        } catch (Exception e) {
            CobblePass.LOGGER.error("Failed to broadcast reset complete message", e);
        }
    }
    
    /**
     * Gets a message with placeholders replaced.
     * 
     * @param messageKey The language key for the message
     * @param placeholders Map of placeholder values
     * @return The message with placeholders replaced
     */
    private String getMessageWithPlaceholders(String messageKey, Map<String, Object> placeholders) {
        String message = LangManager.getRaw(messageKey);
        
        // Replace placeholders in the format {placeholder}
        for (Map.Entry<String, Object> entry : placeholders.entrySet()) {
            String placeholder = "{" + entry.getKey() + "}";
            String value = String.valueOf(entry.getValue());
            message = message.replace(placeholder, value);
        }
        
        return message;
    }
    
    /**
     * Gets the message key for a specific message type.
     * 
     * @param messageType The message type
     * @return The corresponding language key
     */
    private String getMessageKeyForType(MessageType messageType) {
        switch (messageType) {
            case SEASON_END:
                return "lang.season.end.personal";
            case SEASON_START:
                return "lang.season.start.personal";
            case PREMIUM_PRESERVED:
                return "lang.season.premium.preserved";
            case PREMIUM_RESTORED:
                return "lang.season.premium.restored";
            case RESET_PROGRESS:
                return "lang.season.reset.progress.personal";
            case RESET_COMPLETE:
                return "lang.season.reset.complete.personal";
            default:
                return null;
        }
    }
    
    /**
     * Formats a duration in days to a human-readable string.
     * 
     * @param days Number of days
     * @return Formatted duration string
     */
    private String formatDuration(long days) {
        if (days <= 0) {
            return "Indefinite";
        } else if (days == 1) {
            return "1 day";
        } else if (days < 7) {
            return days + " days";
        } else if (days < 30) {
            long weeks = days / 7;
            long remainingDays = days % 7;
            if (remainingDays == 0) {
                return weeks + (weeks == 1 ? " week" : " weeks");
            } else {
                return weeks + (weeks == 1 ? " week" : " weeks") + " and " + 
                       remainingDays + (remainingDays == 1 ? " day" : " days");
            }
        } else {
            long months = days / 30;
            long remainingDays = days % 30;
            if (remainingDays == 0) {
                return months + (months == 1 ? " month" : " months");
            } else {
                return months + (months == 1 ? " month" : " months") + " and " + 
                       remainingDays + (remainingDays == 1 ? " day" : " days");
            }
        }
    }
    
    /**
     * Formats a LocalDateTime to a readable date string.
     * 
     * @param dateTime The date to format
     * @return Formatted date string
     */
    private String formatDate(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "Unknown";
        }
        return dateTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"));
    }
    
    /**
     * Information about a season for message placeholders.
     */
    public static class SeasonInfo {
        private final int maxLevel;
        private final long durationDays;
        private final String seasonName;
        private final LocalDateTime startDate;
        private final LocalDateTime endDate;
        
        public SeasonInfo(int maxLevel, long durationDays, String seasonName, 
                         LocalDateTime startDate, LocalDateTime endDate) {
            this.maxLevel = maxLevel;
            this.durationDays = durationDays;
            this.seasonName = seasonName;
            this.startDate = startDate;
            this.endDate = endDate;
        }
        
        public int getMaxLevel() { return maxLevel; }
        public long getDurationDays() { return durationDays; }
        public String getSeasonName() { return seasonName; }
        public LocalDateTime getStartDate() { return startDate; }
        public LocalDateTime getEndDate() { return endDate; }
    }
}