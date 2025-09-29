package com.gnut.factionsrecruit.util;

import com.gnut.factionsrecruit.FactionsRecruit;
import com.gnut.factionsrecruit.VisualUtils;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Utility class for sending formatted messages using VisualUtils
 */
public class MessageUtil {

    private static final String PREFIX = VisualUtils.createServerGradient("FactionsRecruit") +
            ChatColor.of(VisualUtils.ColorPalette.TEXT_SECONDARY) + " " + VisualUtils.Symbols.ARROW_RIGHT + " ";

    /**
     * Sends a formatted message to a player
     *
     * @param player The player to send the message to
     * @param message The message to send
     */
    public static void sendMessage(Player player, String message) {
        player.sendMessage(PREFIX + ChatColor.of(VisualUtils.ColorPalette.TEXT_PRIMARY) + message);
    }

    /**
     * Sends a success message to a player
     *
     * @param player The player to send the message to
     * @param message The success message
     */
    public static void sendSuccess(Player player, String message) {
        player.sendMessage(PREFIX + VisualUtils.createStatusIndicator(message, true));
    }

    /**
     * Sends an error message to a player
     *
     * @param player The player to send the message to
     * @param message The error message
     */
    public static void sendError(Player player, String message) {
        player.sendMessage(PREFIX + VisualUtils.createStatusIndicator(message, false));
    }

    /**
     * Sends a warning message to a player
     *
     * @param player The player to send the message to
     * @param message The warning message
     */
    public static void sendWarning(Player player, String message) {
        player.sendMessage(PREFIX + ChatColor.of(VisualUtils.ColorPalette.WARNING) +
                VisualUtils.Symbols.HOURGLASS + " " + message);
    }

    /**
     * Sends an info message to a player
     *
     * @param player The player to send the message to
     * @param message The info message
     */
    public static void sendInfo(Player player, String message) {
        player.sendMessage(PREFIX + ChatColor.of(VisualUtils.ColorPalette.INFO) +
                VisualUtils.Symbols.DIAMOND + " " + message);
    }

    /**
     * Sends a formatted message to a command sender
     *
     * @param sender The command sender
     * @param message The message to send
     */
    public static void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(PREFIX + ChatColor.of(VisualUtils.ColorPalette.TEXT_PRIMARY) + message);
    }

    /**
     * Sends a success message to a command sender
     *
     * @param sender The command sender
     * @param message The success message
     */
    public static void sendSuccess(CommandSender sender, String message) {
        sender.sendMessage(PREFIX + VisualUtils.createStatusIndicator(message, true));
    }

    /**
     * Sends an error message to a command sender
     *
     * @param sender The command sender
     * @param message The error message
     */
    public static void sendError(CommandSender sender, String message) {
        sender.sendMessage(PREFIX + VisualUtils.createStatusIndicator(message, false));
    }

    /**
     * Sends a warning message to a command sender
     *
     * @param sender The command sender
     * @param message The warning message
     */
    public static void sendWarning(CommandSender sender, String message) {
        sender.sendMessage(PREFIX + ChatColor.of(VisualUtils.ColorPalette.WARNING) +
                VisualUtils.Symbols.HOURGLASS + " " + message);
    }

    /**
     * Sends an info message to a command sender
     *
     * @param sender The command sender
     * @param message The info message
     */
    public static void sendInfo(CommandSender sender, String message) {
        sender.sendMessage(PREFIX + ChatColor.of(VisualUtils.ColorPalette.INFO) +
                VisualUtils.Symbols.DIAMOND + " " + message);
    }

    /**
     * Sends a formatted message to console
     *
     * @param message The message to send
     * @param success Whether this is a success message
     */
    public static void sendConsoleMessage(String message, boolean success) {
        String prefix = "[FactionsRecruit] ";
        if (success) {
            Bukkit.getLogger().info(prefix + message);
        } else {
            Bukkit.getLogger().warning(prefix + message);
        }
    }

    /**
     * Broadcasts a message to all online players with permission
     *
     * @param message The message to broadcast
     * @param permission The permission required to see the message (null for everyone)
     */
    public static void broadcast(String message, String permission) {
        String formattedMessage = PREFIX + ChatColor.of(VisualUtils.ColorPalette.TEXT_PRIMARY) + message;

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (permission == null || player.hasPermission(permission)) {
                player.sendMessage(formattedMessage);
            }
        }
    }

    /**
     * Sends a formatted message from config
     *
     * @param player The player to send the message to
     * @param configKey The config key under 'messages'
     * @param defaultMessage The default message if config key not found
     */
    public static void sendConfigMessage(Player player, String configKey, String defaultMessage) {
        FactionsRecruit plugin = FactionsRecruit.getInstance();
        String message = plugin.getConfigManager().getMessage(configKey, defaultMessage);
        sendMessage(player, message);
    }

    /**
     * Sends an action bar message to a player
     *
     * @param player The player to send the action bar to
     * @param message The message to display
     */
    public static void sendActionBar(Player player, String message) {
        String formatted = ChatColor.of(VisualUtils.ColorPalette.SERVER_PINK) + message;
        player.sendActionBar(formatted);
    }

    /**
     * Sends a title to a player
     *
     * @param player The player to send the title to
     * @param title The main title text
     * @param subtitle The subtitle text
     * @param fadeIn Fade in time in ticks
     * @param stay Stay time in ticks
     * @param fadeOut Fade out time in ticks
     */
    public static void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        String formattedTitle = VisualUtils.createServerGradient(title);
        String formattedSubtitle = ChatColor.of(VisualUtils.ColorPalette.TEXT_SECONDARY) + subtitle;
        player.sendTitle(formattedTitle, formattedSubtitle, fadeIn, stay, fadeOut);
    }

    /**
     * Creates a clickable text component (for future use with Adventure API)
     *
     * @param text The text to display
     * @param hoverText The text to show on hover
     * @return Formatted text with hover effect
     */
    public static String createHoverText(String text, String hoverText) {
        // For now, just return formatted text
        // Can be enhanced with Adventure API components later
        return ChatColor.of(VisualUtils.ColorPalette.INFO) + text;
    }

    /**
     * Formats a timestamp for display
     *
     * @param timestamp The timestamp in milliseconds
     * @return Formatted time string (e.g., "2 hours ago")
     */
    public static String formatTimeAgo(long timestamp) {
        long now = System.currentTimeMillis();
        long diff = now - timestamp;

        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) {
            return days + " day" + (days > 1 ? "s" : "") + " ago";
        } else if (hours > 0) {
            return hours + " hour" + (hours > 1 ? "s" : "") + " ago";
        } else if (minutes > 0) {
            return minutes + " minute" + (minutes > 1 ? "s" : "") + " ago";
        } else {
            return "just now";
        }
    }

    /**
     * Gets the formatted prefix for messages
     *
     * @return The message prefix
     */
    public static String getPrefix() {
        return PREFIX;
    }
}