package com.gnut.factionsrecruit;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Visual enhancement utilities for creating beautiful GUI elements
 * with gradients, animations, and modern formatting
 */
public class VisualUtils {

    // Color palettes for different themes
    public static class ColorPalette {
        // Server-specific white/pink/red gradient theme
        public static final String SERVER_WHITE = "#FFFFFF";      // Pure White
        public static final String SERVER_PINK = "#FF69B4";       // Hot Pink
        public static final String SERVER_LIGHT_PINK = "#FFB6C1"; // Light Pink
        public static final String SERVER_RED = "#FF1744";        // Bright Red
        public static final String SERVER_DARK_RED = "#D32F2F";   // Dark Red

        // Primary theme colors (white to pink to red gradient)
        public static final String FACTION_PRIMARY = "#FFFFFF";    // White
        public static final String FACTION_SECONDARY = "#FF69B4";  // Hot Pink
        public static final String FACTION_ACCENT = "#FF1744";     // Bright Red

        // Status colors
        public static final String SUCCESS = "#4CAF50";   // Green
        public static final String WARNING = "#FF9800";   // Orange
        public static final String ERROR = "#F44336";     // Red
        public static final String INFO = "#2196F3";      // Blue

        // UI colors (adjusted for white/pink/red theme with chest background compatibility)
        public static final String TEXT_PRIMARY = "#F0F0F0";     // Soft White (not pure white)
        public static final String TEXT_SECONDARY = "#D0D0D0";   // Light Gray (higher contrast)
        public static final String TEXT_MUTED = "#A0A0A0";       // Medium Gray
        public static final String BACKGROUND = "#1A1A1A";       // Dark

        // Server gradient combinations
        public static final String GRADIENT_WHITE_PINK = SERVER_WHITE + ":" + SERVER_PINK;
        public static final String GRADIENT_PINK_RED = SERVER_PINK + ":" + SERVER_RED;
        public static final String GRADIENT_WHITE_RED = SERVER_WHITE + ":" + SERVER_RED;
        public static final String GRADIENT_FULL = SERVER_WHITE + ":" + SERVER_PINK + ":" + SERVER_RED;
    }

    // Unicode symbols for enhanced visual elements
    public static class Symbols {
        public static final String ARROW_RIGHT = "➤";
        public static final String BULLET = "•";
        public static final String DIAMOND = "◆";
        public static final String STAR = "★";
        public static final String HEART = "♥";
        public static final String CHECK = "✓";
        public static final String CROSS = "✗";
        public static final String HOURGLASS = "⧗";
        public static final String SHIELD = "⛨";
        public static final String CROWN = "♛";
        public static final String SWORD = "⚔";
        public static final String DIVIDER = "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬";
        public static final String THIN_DIVIDER = "━━━━━━━━━━━━━━━━━━━━";

        // Server-specific decorative elements
        public static final String SERVER_ARROW_LEFT = "◄";
        public static final String SERVER_ARROW_RIGHT = "►";
        public static final String SERVER_DECORATOR = "┯┷┯┷┯┷┯┷┯┷┯┷┯";
        public static final String SERVER_DIVIDER_LEFT = "┯┷┯┷┯┷┯┷┯┷┯┷┯►";
        public static final String SERVER_DIVIDER_RIGHT = "◄┯┷┯┷┯┷┯┷┯┷┯┷┯";
        public static final String SERVER_DIVIDER_FULL = "┯┷┯┷┯┷┯┷┯┷┯┷┯► ◄┯┷┯┷┯┷┯┷┯┷┯┷┯";
    }

    // Small caps conversion mappings for server theme
    public static class SmallCaps {
        private static final String NORMAL = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        private static final String SMALL_CAPS = "ᴀʙᴄᴅᴇꜰɢʜɪᴊᴋʟᴍɴᴏᴘǫʀꜱᴛᴜᴠᴡxʏᴢ";

        /**
         * Converts normal text to small caps (server theme style)
         */
        public static String convert(String text) {
            if (text == null || text.isEmpty()) return text;

            StringBuilder result = new StringBuilder();
            for (char c : text.toCharArray()) {
                int index = NORMAL.indexOf(Character.toUpperCase(c));
                if (index != -1) {
                    result.append(SMALL_CAPS.charAt(index));
                } else {
                    result.append(c);
                }
            }
            return result.toString();
        }

        /**
         * Creates a server-styled title with small caps and decorative elements
         */
        public static String createServerTitle(String text) {
            String smallCapsText = convert(text);
            return Symbols.SERVER_DIVIDER_LEFT + " " + smallCapsText + " " + Symbols.SERVER_DIVIDER_RIGHT;
        }

        /**
         * Creates a server-styled section header with small caps
         */
        public static String createSectionHeader(String text) {
            return convert(text);
        }

        /**
         * Creates a server-styled button text with small caps
         */
        public static String createButtonText(String text) {
            return convert(text);
        }
    }

    /**
     * Creates a gradient text effect between two colors
     */
    public static String createGradient(String text, String startColor, String endColor) {
        if (text == null || text.isEmpty()) return text;

        Color start = Color.decode(startColor);
        Color end = Color.decode(endColor);

        StringBuilder result = new StringBuilder();
        int length = text.length();

        for (int i = 0; i < length; i++) {
            char c = text.charAt(i);
            if (c == ' ') {
                result.append(c);
                continue;
            }

            // Calculate color interpolation
            float ratio = length > 1 ? (float) i / (length - 1) : 0;
            int r = (int) (start.getRed() + ratio * (end.getRed() - start.getRed()));
            int g = (int) (start.getGreen() + ratio * (end.getGreen() - start.getGreen()));
            int b = (int) (start.getBlue() + ratio * (end.getBlue() - start.getBlue()));

            String hexColor = String.format("#%02x%02x%02x", r, g, b);
            result.append(ChatColor.of(hexColor)).append(c);
        }

        return result.toString();
    }

    /**
     * Creates a rainbow gradient effect
     */
    public static String createRainbow(String text) {
        if (text == null || text.isEmpty()) return text;

        StringBuilder result = new StringBuilder();
        String[] colors = {"#FF0000", "#FF8000", "#FFFF00", "#80FF00", "#00FF00", "#00FF80", "#00FFFF", "#0080FF", "#0000FF", "#8000FF", "#FF00FF", "#FF0080"};

        int colorIndex = 0;
        for (char c : text.toCharArray()) {
            if (c != ' ') {
                result.append(ChatColor.of(colors[colorIndex % colors.length])).append(c);
                colorIndex++;
            } else {
                result.append(c);
            }
        }

        return result.toString();
    }

    /**
     * Creates an animated text effect (for titles)
     */
    public static String createAnimatedTitle(String text, int frame) {
        String[] effects = {
            createGradient(text, ColorPalette.FACTION_PRIMARY, ColorPalette.FACTION_SECONDARY),
            createGradient(text, ColorPalette.FACTION_SECONDARY, ColorPalette.FACTION_ACCENT),
            createGradient(text, ColorPalette.FACTION_ACCENT, ColorPalette.FACTION_PRIMARY)
        };

        return effects[frame % effects.length];
    }

    /**
     * Creates a status indicator with color and symbol
     */
    public static String createStatusIndicator(String status, boolean isPositive) {
        String color = isPositive ? ColorPalette.SUCCESS : ColorPalette.ERROR;
        String symbol = isPositive ? Symbols.CHECK : Symbols.CROSS;
        return ChatColor.of(color) + symbol + " " + status;
    }

    /**
     * Creates a progress bar
     */
    public static String createProgressBar(int current, int max, int barLength, String filledColor, String emptyColor) {
        if (max <= 0) return "";

        int filled = Math.min((current * barLength) / max, barLength);
        int empty = barLength - filled;

        StringBuilder bar = new StringBuilder();
        bar.append(ChatColor.of(filledColor));
        for (int i = 0; i < filled; i++) {
            bar.append("█");
        }
        bar.append(ChatColor.of(emptyColor));
        for (int i = 0; i < empty; i++) {
            bar.append("░");
        }

        return bar.toString();
    }

    /**
     * Creates a formatted lore section with title and content
     */
    public static List<String> createLoreSection(String title, List<String> content) {
        List<String> lore = new ArrayList<>();

        // Add section title with gradient
        lore.add(createGradient(title, ColorPalette.FACTION_PRIMARY, ColorPalette.FACTION_SECONDARY));

        // Add content with proper indentation and styling
        for (String line : content) {
            lore.add(ChatColor.of(ColorPalette.TEXT_SECONDARY) + "  " + Symbols.ARROW_RIGHT + " " + line);
        }

        // Add spacing
        lore.add("");

        return lore;
    }

    /**
     * Creates a divider line for lore
     */
    public static String createDivider(String color) {
        return ChatColor.of(color) + Symbols.THIN_DIVIDER;
    }

    /**
     * Creates an enhanced item with gradient name and formatted lore
     */
    public static ItemStack createEnhancedItem(Material material, String name, List<String> lore, boolean enchanted) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            // Set gradient name
            meta.setDisplayName(createGradient(name, ColorPalette.FACTION_PRIMARY, ColorPalette.FACTION_ACCENT));

            // Set enhanced lore
            if (lore != null && !lore.isEmpty()) {
                List<String> enhancedLore = new ArrayList<>();
                enhancedLore.add(createDivider(ColorPalette.FACTION_SECONDARY));
                enhancedLore.addAll(lore);
                enhancedLore.add(createDivider(ColorPalette.FACTION_SECONDARY));
                meta.setLore(enhancedLore);
            }

            // Add enchantment glow if requested
            if (enchanted) {
                meta.addEnchant(Enchantment.UNBREAKING, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }

            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * Creates a faction-themed item
     */
    public static ItemStack createFactionItem(Material material, String factionName, String leader, int members, int maxMembers) {
        List<String> lore = Arrays.asList(
            ChatColor.of(ColorPalette.TEXT_SECONDARY) + Symbols.CROWN + " Leader: " + ChatColor.of(ColorPalette.TEXT_PRIMARY) + leader,
            ChatColor.of(ColorPalette.TEXT_SECONDARY) + Symbols.SHIELD + " Members: " + ChatColor.of(ColorPalette.TEXT_PRIMARY) + members + "/" + maxMembers,
            "",
            ChatColor.of(ColorPalette.INFO) + Symbols.ARROW_RIGHT + " Click to view details"
        );

        return createEnhancedItem(material, factionName, lore, true);
    }

    /**
     * Creates a player-themed item
     */
    public static ItemStack createPlayerItem(Material material, String playerName, String experience, String timezone, String skills) {
        List<String> lore = Arrays.asList(
            ChatColor.of(ColorPalette.TEXT_SECONDARY) + Symbols.HOURGLASS + " Experience: " + ChatColor.of(ColorPalette.TEXT_PRIMARY) + experience,
            ChatColor.of(ColorPalette.TEXT_SECONDARY) + Symbols.DIAMOND + " Timezone: " + ChatColor.of(ColorPalette.TEXT_PRIMARY) + timezone,
            ChatColor.of(ColorPalette.TEXT_SECONDARY) + Symbols.SWORD + " Skills: " + ChatColor.of(ColorPalette.TEXT_PRIMARY) + skills,
            "",
            ChatColor.of(ColorPalette.INFO) + Symbols.ARROW_RIGHT + " Click to view profile"
        );

        return createEnhancedItem(material, playerName, lore, false);
    }

    /**
     * Creates a status item with appropriate styling
     */
    public static ItemStack createStatusItem(Material material, String title, String status, boolean isActive) {
        String statusColor = isActive ? ColorPalette.SUCCESS : ColorPalette.ERROR;
        String statusSymbol = isActive ? Symbols.CHECK : Symbols.CROSS;

        List<String> lore = Arrays.asList(
            ChatColor.of(ColorPalette.TEXT_SECONDARY) + "Status: " + ChatColor.of(statusColor) + statusSymbol + " " + status,
            "",
            ChatColor.of(ColorPalette.INFO) + Symbols.ARROW_RIGHT + " Click to toggle"
        );

        return createEnhancedItem(material, title, lore, isActive);
    }

    /**
     * Converts legacy color codes to modern hex colors
     */
    public static String modernizeColors(String text) {
        return text
            .replace("&a", ChatColor.of(ColorPalette.SUCCESS).toString())
            .replace("&c", ChatColor.of(ColorPalette.ERROR).toString())
            .replace("&e", ChatColor.of(ColorPalette.WARNING).toString())
            .replace("&b", ChatColor.of(ColorPalette.INFO).toString())
            .replace("&6", ChatColor.of(ColorPalette.FACTION_PRIMARY).toString())
            .replace("&7", ChatColor.of(ColorPalette.TEXT_SECONDARY).toString())
            .replace("&8", ChatColor.of(ColorPalette.TEXT_MUTED).toString())
            .replace("&f", ChatColor.of(ColorPalette.TEXT_PRIMARY).toString());
    }

    /**
     * Creates a clickable action lore line
     */
    public static String createActionLore(String action, String color) {
        return ChatColor.of(color) + Symbols.ARROW_RIGHT + " " + action;
    }

    // ============= SERVER-SPECIFIC CONVENIENCE METHODS =============

    /**
     * Creates a server-themed gradient with white/pink/red colors
     */
    public static String createServerGradient(String text) {
        return createGradient(text, ColorPalette.SERVER_WHITE, ColorPalette.SERVER_RED);
    }

    /**
     * Creates a server-themed title with small caps, gradient, and decorative elements
     */
    public static String createServerTitle(String text) {
        String smallCapsText = SmallCaps.convert(text);
        String gradientText = createGradient(smallCapsText, ColorPalette.SERVER_WHITE, ColorPalette.SERVER_PINK);
        return ChatColor.of(ColorPalette.SERVER_PINK) + Symbols.SERVER_DIVIDER_LEFT + " " +
               gradientText + " " +
               ChatColor.of(ColorPalette.SERVER_PINK) + Symbols.SERVER_DIVIDER_RIGHT;
    }

    /**
     * Creates a server-themed section header with small caps and gradient
     */
    public static String createServerSectionHeader(String text) {
        String smallCapsText = SmallCaps.convert(text);
        return createGradient(smallCapsText, ColorPalette.SERVER_WHITE, ColorPalette.SERVER_PINK);
    }

    /**
     * Creates a server-themed button name with small caps
     */
    public static String createServerButtonName(String text) {
        String smallCapsText = SmallCaps.convert(text);
        return createGradient(smallCapsText, ColorPalette.SERVER_PINK, ColorPalette.SERVER_RED);
    }

    /**
     * Creates a server-themed divider line
     */
    public static String createServerDivider() {
        return ChatColor.of(ColorPalette.SERVER_PINK) + Symbols.SERVER_DECORATOR;
    }

    /**
     * Creates a server-themed enhanced item with small caps name and server styling
     */
    public static ItemStack createServerItem(Material material, String name, List<String> lore, boolean enchanted) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            // Set server-themed gradient name with small caps
            String serverName = createServerButtonName(name);
            meta.setDisplayName(serverName);

            // Set enhanced lore with server dividers
            if (lore != null && !lore.isEmpty()) {
                List<String> enhancedLore = new ArrayList<>();
                enhancedLore.add(createServerDivider());
                enhancedLore.addAll(lore);
                enhancedLore.add(createServerDivider());
                meta.setLore(enhancedLore);
            }

            // Add enchantment glow if requested
            if (enchanted) {
                meta.addEnchant(Enchantment.UNBREAKING, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }

            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * Creates a server-themed lore section with small caps title
     */
    public static List<String> createServerLoreSection(String title, List<String> content) {
        List<String> lore = new ArrayList<>();

        // Add section title with server styling
        lore.add(createServerSectionHeader(title));

        // Add content with proper indentation and styling
        for (String line : content) {
            lore.add(ChatColor.of(ColorPalette.TEXT_SECONDARY) + "  " + Symbols.ARROW_RIGHT + " " + line);
        }

        // Add spacing
        lore.add("");

        return lore;
    }

    /**
     * Creates a compact server-themed title for GUI constraints (under 30 chars)
     * Uses minimal decorative elements to stay within Minecraft's chest UI limits
     */
    public static String createCompactServerTitle(String text) {
        String smallCapsText = SmallCaps.convert(text);
        String gradientText = createGradient(smallCapsText, ColorPalette.SERVER_WHITE, ColorPalette.SERVER_PINK);
        // Use minimal decorators: ► text ◄ (keeps total under 30 chars)
        return ChatColor.of(ColorPalette.SERVER_PINK) + Symbols.SERVER_ARROW_RIGHT + " " +
               gradientText + " " +
               ChatColor.of(ColorPalette.SERVER_PINK) + Symbols.SERVER_ARROW_LEFT;
    }

    /**
     * Creates a server-themed status indicator with small caps
     */
    public static String createServerStatusIndicator(String status, boolean isPositive) {
        String color = isPositive ? ColorPalette.SUCCESS : ColorPalette.ERROR;
        String symbol = isPositive ? Symbols.CHECK : Symbols.CROSS;
        String smallCapsStatus = SmallCaps.convert(status);
        return ChatColor.of(color) + symbol + " " + smallCapsStatus;
    }
}