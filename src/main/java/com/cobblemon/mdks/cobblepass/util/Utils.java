package com.cobblemon.mdks.cobblepass.util;

import com.cobblemon.mdks.cobblepass.CobblePass;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Utility methods for common operations
 */
public class Utils {

    /**
     * Create a new Gson instance with pretty printing
     */
    public static Gson newGson() {
        return new GsonBuilder().setPrettyPrinting().create();
    }

    /**
     * Format a duration in milliseconds to a readable string
     */
    public static String formatDuration(long duration) {
        if (duration <= 0) return "permanent";
        
        long days = duration / Constants.MILLIS_PER_DAY;
        duration %= Constants.MILLIS_PER_DAY;
        
        long hours = duration / Constants.MILLIS_PER_HOUR;
        duration %= Constants.MILLIS_PER_HOUR;
        
        long minutes = duration / Constants.MILLIS_PER_MINUTE;
        duration %= Constants.MILLIS_PER_MINUTE;
        
        long seconds = duration / Constants.MILLIS_PER_SECOND;

        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append("d ");
        if (hours > 0) sb.append(hours).append("h ");
        if (minutes > 0) sb.append(minutes).append("m ");
        if (seconds > 0) sb.append(seconds).append("s");

        return sb.toString().trim();
    }

    /**
     * Capitalize the first letter of each word in a string
     */
    public static String capitalizeFirst(String str) {
        if (str == null || str.isEmpty()) return str;

        String[] words = str.toLowerCase().split("_| ");
        StringBuilder result = new StringBuilder();

        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)))
                      .append(word.substring(1))
                      .append(" ");
            }
        }

        return result.toString().trim();
    }

    /**
     * Broadcast a message to all online players
     */
    public static void broadcast(String message) {
        if (CobblePass.server == null) return;
        
        Component component = Component.literal(message);
        List<ServerPlayer> players = new ArrayList<>(CobblePass.server.getPlayerList().getPlayers());
        
        for (ServerPlayer player : players) {
            player.sendSystemMessage(component);
        }
    }

    /**
     * Check if a directory exists and create it if it doesn't
     */
    public static File checkForDirectory(String path) {
        File dir = new File(new File("").getAbsolutePath() + path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    /**
     * Write data to a file asynchronously
     */
    public static CompletableFuture<Boolean> writeFileAsync(String directory, String filename, String data) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Ensure directory exists
                File dir = checkForDirectory(directory);
                Path path = Paths.get(dir.getAbsolutePath(), filename);

                // Write file
                try (FileWriter writer = new FileWriter(path.toFile())) {
                    writer.write(data);
                }
                return true;
            } catch (Exception e) {
                CobblePass.LOGGER.error("Failed to write file: " + filename, e);
                return false;
            }
        });
    }

    /**
     * Read a file asynchronously
     */
    public static CompletableFuture<Boolean> readFileAsync(String directory, String filename, Consumer<String> callback) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Path path = Paths.get(new File("").getAbsolutePath() + "/" + directory, filename);
                File file = path.toFile();

                if (!file.exists()) {
                    callback.accept("");
                    return false;
                }

                String content = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
                callback.accept(content);
                return true;
            } catch (Exception e) {
                CobblePass.LOGGER.error("Failed to read file: " + filename, e);
                callback.accept("");
                return false;
            }
        });
    }

    /**
     * Read a file synchronously
     */
    public static String readFileSync(String directory, String filename) {
        try {
            // Ensure directory exists
            File dir = checkForDirectory("/" + directory);
            Path path = Paths.get(dir.getAbsolutePath(), filename);
            File file = path.toFile();

            if (!file.exists()) {
                return "";
            }

            return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
        } catch (Exception e) {
            CobblePass.LOGGER.error("Failed to read file: " + filename, e);
            return "";
        }
    }

    /**
     * Write to a file synchronously
     */
    public static boolean writeFileSync(String directory, String filename, String data) {
        try {
            // Ensure directory exists
            File dir = checkForDirectory("/" + directory);
            Path path = Paths.get(dir.getAbsolutePath(), filename);

            // Write file
            try (FileWriter writer = new FileWriter(path.toFile())) {
                writer.write(data);
            }
            CobblePass.LOGGER.info("Wrote file: " + path.toAbsolutePath());
            return true;
        } catch (Exception e) {
            CobblePass.LOGGER.error("Failed to write file: " + filename, e);
            return false;
        }
    }

    /**
     * Format a message with color codes
     */
    public static String formatMessage(String message, boolean isPlayer) {
        if (!isPlayer) {
            // Strip color codes for console
            return message.replaceAll("§[0-9a-fk-or]", "").trim();
        }
        return message.trim();
    }

    /**
     * Check if a string can be parsed as an integer
     */
    public static boolean isInteger(String str) {
        if (str == null) return false;
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Check if a string can be parsed as a double
     */
    public static boolean isDouble(String str) {
        if (str == null) return false;
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Parse a duration string (e.g. "30d", "24h", "60m") to milliseconds
     */
    public static long parseDuration(String duration) {
        if (duration == null || duration.isEmpty()) {
            return -1;
        }

        try {
            String value = duration.substring(0, duration.length() - 1);
            char unit = Character.toLowerCase(duration.charAt(duration.length() - 1));
            long amount = Long.parseLong(value);

            return switch (unit) {
                case 'd' -> amount * Constants.MILLIS_PER_DAY;
                case 'h' -> amount * Constants.MILLIS_PER_HOUR;
                case 'm' -> amount * Constants.MILLIS_PER_MINUTE;
                case 's' -> amount * Constants.MILLIS_PER_SECOND;
                default -> -1;
            };
        } catch (Exception e) {
            return -1;
        }
    }
}
