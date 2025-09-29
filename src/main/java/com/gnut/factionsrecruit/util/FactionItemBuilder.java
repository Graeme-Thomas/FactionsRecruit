package com.gnut.factionsrecruit.util;

import com.gnut.factionsrecruit.VisualUtils;
import com.gnut.factionsrecruit.integration.FactionInfo;
import com.gnut.factionsrecruit.integration.PlaceholderAPIIntegration;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for creating faction items with PlaceholderAPI integration
 * Displays faction information using Kore and FactionsUUID placeholders
 */
public class FactionItemBuilder {

    /**
     * Create a faction banner item with full PAPI integration
     * Shows: leader name, online members, faction age, faction value, FTop position
     *
     * @param factionMember A member of the faction (used to query PAPI)
     * @return ItemStack with faction details
     */
    public static ItemStack createFactionBannerItem(Player factionMember) {
        if (!PlaceholderAPIIntegration.isAvailable() || !PlaceholderAPIIntegration.isInFaction(factionMember)) {
            return createDefaultFactionItem(factionMember.getName());
        }

        FactionInfo factionInfo = PlaceholderAPIIntegration.getFactionInfo(factionMember);
        if (factionInfo == null) {
            return createDefaultFactionItem(factionMember.getName());
        }

        return createFactionBannerItem(factionInfo);
    }

    /**
     * Create a faction banner item from FactionInfo object
     *
     * @param factionInfo Faction information from PAPI
     * @return ItemStack with faction details
     */
    public static ItemStack createFactionBannerItem(FactionInfo factionInfo) {
        List<String> lore = new ArrayList<>();

        // Add server divider
        lore.add(VisualUtils.createServerDivider());

        // Leader name
        lore.add(ChatColor.of(VisualUtils.ColorPalette.TEXT_SECONDARY) +
                VisualUtils.Symbols.CROWN + " ʟᴇᴀᴅᴇʀ: " +
                ChatColor.of(VisualUtils.ColorPalette.SERVER_PINK) + factionInfo.getLeaderName());

        // Online members / total members
        String memberColor = factionInfo.getOnlineMembers() > 0 ?
                VisualUtils.ColorPalette.SUCCESS : VisualUtils.ColorPalette.TEXT_MUTED;
        lore.add(ChatColor.of(VisualUtils.ColorPalette.TEXT_SECONDARY) +
                VisualUtils.Symbols.SHIELD + " ᴍᴇᴍʙᴇʀꜱ: " +
                ChatColor.of(memberColor) + factionInfo.getOnlineMembers() +
                ChatColor.of(VisualUtils.ColorPalette.TEXT_SECONDARY) + "/" +
                ChatColor.of(VisualUtils.ColorPalette.TEXT_PRIMARY) + factionInfo.getTotalMembers());

        // Faction age (founded date or age from Kore)
        lore.add(ChatColor.of(VisualUtils.ColorPalette.TEXT_SECONDARY) +
                VisualUtils.Symbols.HOURGLASS + " ᴀɢᴇ: " +
                ChatColor.of(VisualUtils.ColorPalette.TEXT_PRIMARY) + factionInfo.getFactionAge());

        // Faction value/worth
        String formattedValue = PlaceholderAPIIntegration.formatFactionValue(factionInfo.getFactionValue());
        lore.add(ChatColor.of(VisualUtils.ColorPalette.TEXT_SECONDARY) +
                VisualUtils.Symbols.DIAMOND + " ᴠᴀʟᴜᴇ: " +
                ChatColor.of(VisualUtils.ColorPalette.SERVER_PINK) + "$" + formattedValue);

        // FTop position (if available from Kore)
        if (!factionInfo.getFtopPosition().equals("N/A")) {
            lore.add(ChatColor.of(VisualUtils.ColorPalette.TEXT_SECONDARY) +
                    VisualUtils.Symbols.STAR + " ꜰᴛᴏᴘ: " +
                    ChatColor.of(VisualUtils.ColorPalette.SERVER_RED) + "#" + factionInfo.getFtopPosition());
        }

        // Add spacing
        lore.add("");

        // Power/DTR info
        lore.add(ChatColor.of(VisualUtils.ColorPalette.TEXT_MUTED) +
                "ᴘᴏᴡᴇʀ: " + factionInfo.getPower() + "/" + factionInfo.getMaxPower());

        // Description if available
        if (factionInfo.getFactionDescription() != null && !factionInfo.getFactionDescription().isEmpty()) {
            lore.add("");
            lore.add(ChatColor.of(VisualUtils.ColorPalette.TEXT_MUTED) +
                    factionInfo.getFactionDescription());
        }

        // Add spacing
        lore.add("");

        // Click action
        lore.add(ChatColor.of(VisualUtils.ColorPalette.INFO) +
                VisualUtils.Symbols.ARROW_RIGHT + " ᴄʟɪᴄᴋ ᴛᴏ ᴀᴘᴘʟʏ");

        // Add server divider
        lore.add(VisualUtils.createServerDivider());

        // Create item with server styling
        return VisualUtils.createServerItem(
                Material.YELLOW_BANNER, // Default banner color
                factionInfo.getFactionName(),
                lore,
                true // Enchantment glow
        );
    }

    /**
     * Create a compact faction item for listings (less detail)
     *
     * @param factionMember A member of the faction
     * @return Compact faction item
     */
    public static ItemStack createCompactFactionItem(Player factionMember) {
        if (!PlaceholderAPIIntegration.isAvailable() || !PlaceholderAPIIntegration.isInFaction(factionMember)) {
            return createDefaultFactionItem(factionMember.getName());
        }

        String factionName = PlaceholderAPIIntegration.getFactionName(factionMember);
        String leaderName = PlaceholderAPIIntegration.getFactionLeader(factionMember);
        int onlineMembers = PlaceholderAPIIntegration.getOnlineMembers(factionMember);
        int totalMembers = PlaceholderAPIIntegration.getTotalMembers(factionMember);
        String ftopPosition = PlaceholderAPIIntegration.getFtopPosition(factionMember);

        List<String> lore = new ArrayList<>();
        lore.add(VisualUtils.createServerDivider());

        lore.add(ChatColor.of(VisualUtils.ColorPalette.TEXT_SECONDARY) +
                VisualUtils.Symbols.CROWN + " " + leaderName);

        String memberColor = onlineMembers > 0 ?
                VisualUtils.ColorPalette.SUCCESS : VisualUtils.ColorPalette.TEXT_MUTED;
        lore.add(ChatColor.of(memberColor) + String.valueOf(onlineMembers) +
                ChatColor.of(VisualUtils.ColorPalette.TEXT_SECONDARY) + "/" +
                ChatColor.of(VisualUtils.ColorPalette.TEXT_PRIMARY) + String.valueOf(totalMembers));

        if (!ftopPosition.equals("N/A")) {
            lore.add(ChatColor.of(VisualUtils.ColorPalette.SERVER_RED) + "#" + ftopPosition);
        }

        lore.add("");
        lore.add(ChatColor.of(VisualUtils.ColorPalette.INFO) +
                VisualUtils.Symbols.ARROW_RIGHT + " ᴄʟɪᴄᴋ");
        lore.add(VisualUtils.createServerDivider());

        return VisualUtils.createServerItem(
                Material.YELLOW_BANNER,
                factionName,
                lore,
                true
        );
    }

    /**
     * Check if a player is a faction leader (for showing invitation options)
     *
     * @param player Player to check
     * @return true if player is a faction leader
     */
    public static boolean isFactionLeader(Player player) {
        return PlaceholderAPIIntegration.isAvailable() &&
               PlaceholderAPIIntegration.isFactionLeader(player);
    }

    /**
     * Check if a player is in a faction
     *
     * @param player Player to check
     * @return true if player is in a faction
     */
    public static boolean isInFaction(Player player) {
        return PlaceholderAPIIntegration.isAvailable() &&
               PlaceholderAPIIntegration.isInFaction(player);
    }

    /**
     * Create a default faction item when PAPI is unavailable
     *
     * @param playerName Name to display
     * @return Default faction item
     */
    private static ItemStack createDefaultFactionItem(String playerName) {
        List<String> lore = new ArrayList<>();
        lore.add(VisualUtils.createServerDivider());
        lore.add(ChatColor.of(VisualUtils.ColorPalette.ERROR) +
                "PlaceholderAPI not available");
        lore.add(ChatColor.of(VisualUtils.ColorPalette.TEXT_MUTED) +
                "Install PlaceholderAPI and");
        lore.add(ChatColor.of(VisualUtils.ColorPalette.TEXT_MUTED) +
                "FactionsUUID expansion");
        lore.add(VisualUtils.createServerDivider());

        return VisualUtils.createServerItem(
                Material.BARRIER,
                "Unknown Faction",
                lore,
                false
        );
    }

    /**
     * Get faction value formatted for display
     *
     * @param player Faction member
     * @return Formatted faction value string (e.g., "$1.5M")
     */
    public static String getFormattedFactionValue(Player player) {
        if (!PlaceholderAPIIntegration.isAvailable()) {
            return "$0";
        }

        String value = PlaceholderAPIIntegration.getFactionValue(player);
        return "$" + PlaceholderAPIIntegration.formatFactionValue(value);
    }

    /**
     * Get faction FTop position
     *
     * @param player Faction member
     * @return FTop position (e.g., "#3") or "N/A"
     */
    public static String getFtopPosition(Player player) {
        if (!PlaceholderAPIIntegration.isAvailable()) {
            return "N/A";
        }

        String position = PlaceholderAPIIntegration.getFtopPosition(player);
        return position.equals("N/A") ? "N/A" : "#" + position;
    }
}