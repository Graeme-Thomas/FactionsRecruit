package com.gnut.factionsrecruit.integration;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.logging.Logger;

/**
 * Integration utility for PlaceholderAPI to fetch faction information
 * Supports both Kore and FactionsUUID placeholders with automatic fallback
 */
public class PlaceholderAPIIntegration {
    private static final Logger logger = Bukkit.getLogger();
    private static boolean placeholderAPIAvailable = false;
    private static boolean koreAvailable = false;
    private static boolean factionsUUIDAvailable = false;

    /**
     * Initialize PlaceholderAPI integration
     * Checks which placeholder expansions are available
     */
    public static void initialize() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            placeholderAPIAvailable = true;
            logger.info("[FactionsRecruit] PlaceholderAPI found! Checking for faction expansions...");

            // Check for Kore expansion (preferred for FTop data)
            koreAvailable = isExpansionAvailable("kore");
            if (koreAvailable) {
                logger.info("[FactionsRecruit] Kore expansion detected - will use for FTop data");
            }

            // Check for FactionsUUID expansion
            factionsUUIDAvailable = isExpansionAvailable("factionsuuid");
            if (factionsUUIDAvailable) {
                logger.info("[FactionsRecruit] FactionsUUID expansion detected");
            }

            if (!koreAvailable && !factionsUUIDAvailable) {
                logger.warning("[FactionsRecruit] No faction expansions found! Faction integration will be limited.");
            }
        } else {
            logger.warning("[FactionsRecruit] PlaceholderAPI not found! Faction integration disabled.");
        }
    }

    /**
     * Check if a specific PlaceholderAPI expansion is available
     */
    private static boolean isExpansionAvailable(String expansion) {
        if (!placeholderAPIAvailable) return false;

        try {
            // Test by setting a placeholder
            String test = PlaceholderAPI.setPlaceholders(null, "%" + expansion + "_test%");
            // If it returns the same string, expansion is not registered
            return !test.equals("%" + expansion + "_test%");
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if PlaceholderAPI is available
     */
    public static boolean isAvailable() {
        return placeholderAPIAvailable;
    }

    /**
     * Check if player is in a faction
     */
    public static boolean isInFaction(Player player) {
        if (!placeholderAPIAvailable) return false;

        String factionName = parsePlaceholder(player, "%factionsuuid_faction_name%");
        return factionName != null && !factionName.isEmpty() && !factionName.equals("Wilderness");
    }

    /**
     * Check if player is the leader of their faction
     */
    public static boolean isFactionLeader(Player player) {
        if (!placeholderAPIAvailable || !isInFaction(player)) return false;

        String leaderName = parsePlaceholder(player, "%factionsuuid_faction_leader%");
        return leaderName != null && leaderName.equalsIgnoreCase(player.getName());
    }

    /**
     * Get comprehensive faction information for a player
     * Uses Kore placeholders when available, falls back to FactionsUUID
     */
    public static FactionInfo getFactionInfo(Player player) {
        if (!placeholderAPIAvailable || !isInFaction(player)) {
            return null;
        }

        FactionInfo.Builder builder = FactionInfo.builder();

        // Basic faction info (always from FactionsUUID)
        builder.factionName(parsePlaceholder(player, "%factionsuuid_faction_name%"));
        builder.factionDescription(parsePlaceholder(player, "%factionsuuid_faction_description%"));
        builder.leaderName(parsePlaceholder(player, "%factionsuuid_faction_leader%"));
        builder.founded(parsePlaceholder(player, "%factionsuuid_faction_founded%"));

        // Try to get internal faction ID
        String internalId = parsePlaceholder(player, "%factionsuuid_faction_internal_id%");
        if (internalId != null && !internalId.isEmpty()) {
            try {
                // FactionsUUID uses string IDs, try to parse as UUID if possible
                // Otherwise, hash the string to create a deterministic UUID
                UUID factionId = UUID.nameUUIDFromBytes(internalId.getBytes());
                builder.factionId(factionId);
            } catch (Exception e) {
                // Ignore if can't create UUID
            }
        }

        // Member counts
        builder.onlineMembers(parseIntPlaceholder(player, "%factionsuuid_faction_online%"));
        builder.offlineMembers(parseIntPlaceholder(player, "%factionsuuid_faction_offline%"));
        builder.totalMembers(parseIntPlaceholder(player, "%factionsuuid_faction_size%"));

        // Faction value and FTop position - prefer Kore, fallback to FactionsUUID
        if (koreAvailable) {
            // Try Kore placeholders first (correct placeholders from Kore)
            builder.factionValue(parsePlaceholder(player, "%kore_factionstop_place_value_player%",
                                parsePlaceholder(player, "%factionsuuid_faction_land_value%")));
            builder.ftopPosition(parsePlaceholder(player, "%kore_factionstop_player_place%",
                                parsePlaceholder(player, "N/A")));
            // Kore doesn't have age, use founded date
            builder.factionAge(parsePlaceholder(player, "%factionsuuid_faction_founded%"));
        } else {
            builder.factionValue(parsePlaceholder(player, "%factionsuuid_faction_land_value%"));
            builder.ftopPosition("N/A"); // FactionsUUID doesn't have FTop position
            builder.factionAge(parsePlaceholder(player, "%factionsuuid_faction_founded%"));
        }

        // Status flags
        String joining = parsePlaceholder(player, "%factionsuuid_faction_joining%");
        builder.isJoining(joining != null && (joining.equalsIgnoreCase("true") || joining.equalsIgnoreCase("open")));

        String peaceful = parsePlaceholder(player, "%factionsuuid_faction_peaceful%");
        builder.isPeaceful(peaceful != null && peaceful.equalsIgnoreCase("true"));

        // Claims and land
        builder.claims(parseIntPlaceholder(player, "%factionsuuid_faction_claims%"));
        builder.maxClaims(parseIntPlaceholder(player, "%factionsuuid_faction_maxclaims%"));

        // Economy
        builder.bankBalance(parseDoublePlaceholder(player, "%factionsuuid_faction_bank_balance%"));

        // Power/DTR
        builder.power(parsePlaceholder(player, "%factionsuuid_faction_power%"));
        builder.maxPower(parsePlaceholder(player, "%factionsuuid_faction_powermax%"));

        return builder.build();
    }

    /**
     * Get faction name for a player
     */
    public static String getFactionName(Player player) {
        if (!placeholderAPIAvailable) return null;
        return parsePlaceholder(player, "%factionsuuid_faction_name%");
    }

    /**
     * Get faction leader name for a player's faction
     */
    public static String getFactionLeader(Player player) {
        if (!placeholderAPIAvailable) return null;
        return parsePlaceholder(player, "%factionsuuid_faction_leader%");
    }

    /**
     * Get number of online faction members
     */
    public static int getOnlineMembers(Player player) {
        if (!placeholderAPIAvailable) return 0;
        return parseIntPlaceholder(player, "%factionsuuid_faction_online%");
    }

    /**
     * Get total faction member count
     */
    public static int getTotalMembers(Player player) {
        if (!placeholderAPIAvailable) return 0;
        return parseIntPlaceholder(player, "%factionsuuid_faction_size%");
    }

    /**
     * Get faction value (worth)
     * Prefers Kore placeholder, falls back to FactionsUUID land value
     */
    public static String getFactionValue(Player player) {
        if (!placeholderAPIAvailable) return "0";

        if (koreAvailable) {
            String koreValue = parsePlaceholder(player, "%kore_factionstop_place_value_player%");
            if (koreValue != null && !koreValue.isEmpty() && !koreValue.equals("0")) {
                return koreValue;
            }
        }

        return parsePlaceholder(player, "%factionsuuid_faction_land_value%");
    }

    /**
     * Get faction FTop position
     * Only available from Kore expansion
     */
    public static String getFtopPosition(Player player) {
        if (!placeholderAPIAvailable || !koreAvailable) return "N/A";
        return parsePlaceholder(player, "%kore_factionstop_player_place%", "N/A");
    }

    /**
     * Get faction age
     * Uses FactionsUUID founded date (Kore doesn't have age)
     */
    public static String getFactionAge(Player player) {
        if (!placeholderAPIAvailable) return "Unknown";
        return parsePlaceholder(player, "%factionsuuid_faction_founded%");
    }

    /**
     * Parse a placeholder and return the result
     */
    private static String parsePlaceholder(Player player, String placeholder) {
        return parsePlaceholder(player, placeholder, null);
    }

    /**
     * Parse a placeholder with a fallback value
     */
    private static String parsePlaceholder(Player player, String placeholder, String fallback) {
        if (!placeholderAPIAvailable) return fallback;

        try {
            String result = PlaceholderAPI.setPlaceholders(player, placeholder);

            // Check if placeholder was resolved (not returned as-is)
            if (result.equals(placeholder)) {
                return fallback;
            }

            // Check for common "empty" values
            if (result == null || result.isEmpty() || result.equalsIgnoreCase("null")) {
                return fallback;
            }

            return result;
        } catch (Exception e) {
            logger.warning("[FactionsRecruit] Error parsing placeholder " + placeholder + ": " + e.getMessage());
            return fallback;
        }
    }

    /**
     * Parse a placeholder as an integer
     */
    private static int parseIntPlaceholder(Player player, String placeholder) {
        String value = parsePlaceholder(player, placeholder, "0");
        try {
            // Remove any non-numeric characters except minus sign
            value = value.replaceAll("[^0-9-]", "");
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Parse a placeholder as a double
     */
    private static double parseDoublePlaceholder(Player player, String placeholder) {
        String value = parsePlaceholder(player, placeholder, "0");
        try {
            // Remove any non-numeric characters except decimal point and minus sign
            value = value.replaceAll("[^0-9.-]", "");
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    /**
     * Format faction value for display (adds commas, K/M/B suffixes)
     */
    public static String formatFactionValue(String value) {
        try {
            // Try to parse as number
            double val = Double.parseDouble(value.replaceAll("[^0-9.]", ""));

            if (val >= 1_000_000_000) {
                return String.format("%.2fB", val / 1_000_000_000);
            } else if (val >= 1_000_000) {
                return String.format("%.2fM", val / 1_000_000);
            } else if (val >= 1_000) {
                return String.format("%.2fK", val / 1_000);
            } else {
                return String.format("%.0f", val);
            }
        } catch (NumberFormatException e) {
            return value; // Return as-is if can't parse
        }
    }
}