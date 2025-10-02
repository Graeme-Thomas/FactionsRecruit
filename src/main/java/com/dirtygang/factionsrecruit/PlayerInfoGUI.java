package com.dirtygang.factionsrecruit;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PlayerInfoGUI {

    private final FactionsRecruit plugin;

    public PlayerInfoGUI(FactionsRecruit plugin) {
        this.plugin = plugin;
    }

    public void openPlayerInfoDisplay(Player viewer, Player targetPlayer) {
        // Create compact server-themed title to fit within 30 character limit
        // For max 16-char username + " Profile" = 24 chars + decorators = under 30
        String title = VisualUtils.createCompactServerTitle(targetPlayer.getName() + " Profile");
        Inventory gui = Bukkit.createInventory(null, 27, title);

        // Create modern border elements with enhanced styling
        ItemStack whiteGlassPane = VisualUtils.createEnhancedItem(
            plugin.getConfigManager().getMaterial("player-info.materials.border-primary", Material.WHITE_STAINED_GLASS_PANE),
            " ",
            null,
            false
        );
        ItemStack pinkGlassPane = VisualUtils.createEnhancedItem(
            plugin.getConfigManager().getMaterial("player-info.materials.border-secondary", Material.PINK_STAINED_GLASS_PANE),
            " ",
            null,
            false
        );

        // Top row
        for (int i = 0; i < 9; i++) {
            gui.setItem(i, (i % 2 == 0) ? whiteGlassPane : pinkGlassPane);
        }

        // Bottom row
        for (int i = 18; i < 27; i++) {
            gui.setItem(i, (i % 2 == 0) ? whiteGlassPane : pinkGlassPane);
        }

        // Side columns
        gui.setItem(9, pinkGlassPane);
        gui.setItem(17, pinkGlassPane);

        // Enhanced Player Head (Slot 4)
        PlayerResume resume = plugin.getDatabaseManager().getPlayerResume(targetPlayer.getUniqueId());
        List<String> profileLore = new ArrayList<>();

        if (resume != null) {
            // Personal Information Section - convert database keys to display names
            List<String> personalInfo = Arrays.asList(
                net.md_5.bungee.api.ChatColor.of(VisualUtils.ColorPalette.TEXT_PRIMARY) + plugin.getConfigManager().getDisplayName(resume.getTimezone()),
                net.md_5.bungee.api.ChatColor.of(VisualUtils.ColorPalette.TEXT_PRIMARY) + plugin.getConfigManager().getDisplayName(resume.getExperience())
            );
            profileLore.addAll(VisualUtils.createServerLoreSection("Personal Info", personalInfo));

            // Availability Section - convert database keys to display names
            List<String> displayDays = new ArrayList<>();
            for (String dbDay : resume.getAvailableDays()) {
                displayDays.add(plugin.getConfigManager().getDisplayName(dbDay));
            }
            List<String> availability = Arrays.asList(
                net.md_5.bungee.api.ChatColor.of(VisualUtils.ColorPalette.TEXT_PRIMARY) + String.join(", ", displayDays)
            );
            profileLore.addAll(VisualUtils.createServerLoreSection("Availability", availability));

            // Skills Section - convert database keys to display names
            List<String> displaySkills = new ArrayList<>();
            for (String dbSkill : resume.getSkills()) {
                displaySkills.add(plugin.getConfigManager().getDisplayName(dbSkill));
            }
            List<String> skills = Arrays.asList(
                net.md_5.bungee.api.ChatColor.of(VisualUtils.ColorPalette.TEXT_PRIMARY) + String.join(", ", displaySkills)
            );
            profileLore.addAll(VisualUtils.createServerLoreSection("Skills", skills));

            // Status Section
            boolean isLooking = resume.isLooking();
            profileLore.add(VisualUtils.createServerStatusIndicator("Looking for Faction", isLooking));
        } else {
            profileLore.add(VisualUtils.createDivider(VisualUtils.ColorPalette.WARNING));
            profileLore.add(net.md_5.bungee.api.ChatColor.of(VisualUtils.ColorPalette.WARNING) + VisualUtils.Symbols.CROSS + " No resume found for this player");
            profileLore.add(VisualUtils.createDivider(VisualUtils.ColorPalette.WARNING));
        }

        ItemStack playerHead = VisualUtils.createServerItem(Material.PLAYER_HEAD, targetPlayer.getName() + "'s Profile", profileLore, false);
        SkullMeta skullMeta = (SkullMeta) playerHead.getItemMeta();
        skullMeta.setOwningPlayer(targetPlayer);
        playerHead.setItemMeta(skullMeta);
        gui.setItem(4, playerHead);

        // Statistics Display (Slots 10-16)
        createStatisticItem(gui, 10, Material.DIAMOND_SWORD, "Combat Stats", targetPlayer, "combat");
        createStatisticItem(gui, 11, Material.CLOCK, "Activity Stats", targetPlayer, "activity");
        createStatisticItem(gui, 12, Material.DIAMOND_PICKAXE, "Building Stats", targetPlayer, "building");
        createStatisticItem(gui, 13, Material.IRON_SWORD, "Grinding Stats", targetPlayer, "grinding");
        createStatisticItem(gui, 14, Material.GOLD_INGOT, "Economy Stats", targetPlayer, "economy");
        createStatisticItem(gui, 15, Material.NETHER_STAR, "Server Status", targetPlayer, "status");
        createStatisticItem(gui, 16, Material.ENCHANTED_BOOK, "Experience Stats", targetPlayer, "experience");

        // Enhanced Invite Button (Slot 22) - Only for faction owners
        if (plugin.getPapiIntegrationManager().isInFaction(viewer) && plugin.getPapiIntegrationManager().hasRole(viewer, "admin")) {
            List<String> inviteLore = Arrays.asList(
                net.md_5.bungee.api.ChatColor.of(VisualUtils.ColorPalette.TEXT_SECONDARY) + "Send an invitation to " + targetPlayer.getName(),
                net.md_5.bungee.api.ChatColor.of(VisualUtils.ColorPalette.TEXT_SECONDARY) + "to join your faction",
                "",
                VisualUtils.createActionLore("Click to invite", VisualUtils.ColorPalette.SUCCESS)
            );

            String inviteName = "Invite to Faction";
            ItemStack inviteButton = VisualUtils.createServerItem(
                plugin.getConfigManager().getMaterial("player-info.materials.invite-button", Material.LECTERN),
                inviteName,
                inviteLore,
                true
            );
            gui.setItem(22, inviteButton);
        }

        viewer.openInventory(gui);
    }

    private ItemStack createGuiItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            if (lore.length > 0) {
                meta.setLore(java.util.Arrays.asList(lore));
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    private void createStatisticItem(Inventory gui, int slot, Material material, String statName, Player targetPlayer, String statType) {
        List<String> statLore = new ArrayList<>();

        // Get the appropriate statistic value based on type
        List<String> statValues = getStatisticValues(targetPlayer, statType);

        for (String statValue : statValues) {
            statLore.add(net.md_5.bungee.api.ChatColor.of(VisualUtils.ColorPalette.TEXT_SECONDARY) +
                        VisualUtils.Symbols.DIAMOND + " " + statValue);
        }

        ItemStack statItem = VisualUtils.createServerItem(material, statName, statLore, false);
        gui.setItem(slot, statItem);
    }

    private List<String> getStatisticValues(Player targetPlayer, String statType) {
        PAPIIntegrationManager papiManager = plugin.getPapiIntegrationManager();
        List<String> values = new ArrayList<>();

        switch (statType) {
            case "combat":
                // Combat stats using Statz plugin placeholders (clearer and more reliable)
                String kills = papiManager.getPlaceholderValue(targetPlayer, "%statz_players_killed%");
                String deaths = papiManager.getPlaceholderValue(targetPlayer, "%statz_deaths%");
                String damageTaken = papiManager.getPlaceholderValue(targetPlayer, "%statz_damage_taken%");
                String kothWins = papiManager.getPlaceholderValue(targetPlayer, "%kore_koth_wins%");

                // Fallback to PlayerStats if Statz not available
                if (kills == null) {
                    kills = papiManager.getPlaceholderValue(targetPlayer, "%playerstats_me,player_kills%");
                }
                if (deaths == null) {
                    deaths = papiManager.getPlaceholderValue(targetPlayer, "%playerstats_me,deaths%");
                }
                if (damageTaken == null) {
                    damageTaken = papiManager.getPlaceholderValue(targetPlayer, "%playerstats_me,damage_taken%");
                }

                // Further fallback to basic statistics
                if (kills == null) {
                    kills = papiManager.getPlaceholderValue(targetPlayer, "%statistic_player_kills%");
                }
                if (deaths == null) {
                    deaths = papiManager.getPlaceholderValue(targetPlayer, "%statistic_deaths%");
                }

                // Always show kills/deaths even if 0
                if (kills != null && deaths != null) {
                    try {
                        int killCount = Integer.parseInt(kills);
                        int deathCount = Integer.parseInt(deaths);
                        if (deathCount > 0) {
                            double kdRatio = (double) killCount / deathCount;
                            values.add(String.format("KDR: %.1f", kdRatio));
                        } else {
                            values.add("KDR: ∞");
                        }
                        values.add(String.format("Kills: %d", killCount));
                        values.add(String.format("Deaths: %d", deathCount));

                        if (damageTaken != null && !damageTaken.equals("0")) {
                            values.add("Damage Taken: " + formatNumber(damageTaken));
                        }
                    } catch (NumberFormatException e) {
                        values.add("Kills: " + (kills != null ? kills : "0"));
                        values.add("Deaths: " + (deaths != null ? deaths : "0"));
                    }
                } else {
                    values.add("Kills: 0");
                    values.add("Deaths: 0");
                }

                if (kothWins != null && !kothWins.equals("0")) {
                    values.add("KOTH Wins: " + kothWins);
                }
                break;

            case "activity":
                // Activity stats using Statz plugin placeholders (clearer formatting)
                String playtime = papiManager.getPlaceholderValue(targetPlayer, "%statz_time_formated_dhm%");
                if (playtime == null || playtime.equals("0")) {
                    playtime = papiManager.getPlaceholderValue(targetPlayer, "%statz_time_played%");
                }

                // Fallback to PlayerStats
                if (playtime == null || playtime.equals("0")) {
                    playtime = papiManager.getPlaceholderValue(targetPlayer, "%playerstats_me,play_one_minute%");
                }
                if (playtime == null || playtime.equals("0")) {
                    playtime = papiManager.getPlaceholderValue(targetPlayer, "%statistic_play_one_minute%");
                }

                if (playtime != null && !playtime.equals("0")) {
                    // Check if it's already formatted (contains letters) or needs formatting
                    if (playtime.matches(".*[a-zA-Z].*")) {
                        values.add("Playtime: " + playtime);
                    } else {
                        values.add("Playtime: " + formatPlaytime(playtime, true));
                    }
                } else {
                    values.add("Playtime: 0m");
                }

                String sessions = papiManager.getPlaceholderValue(targetPlayer, "%statz_joins%");
                if (sessions == null) {
                    sessions = papiManager.getPlaceholderValue(targetPlayer, "%playerstats_me,leave_game%");
                }
                if (sessions == null) {
                    sessions = papiManager.getPlaceholderValue(targetPlayer, "%statistic_leave_game%");
                }

                if (sessions != null && !sessions.equals("0")) {
                    values.add("Sessions: " + formatNumber(sessions));
                } else {
                    values.add("Sessions: 0");
                }
                break;

            case "building":
                // Building stats using Statz plugin placeholders (more reliable tracking)
                String mined = papiManager.getPlaceholderValue(targetPlayer, "%statz_blocks_broken%");
                if (mined == null) {
                    mined = papiManager.getPlaceholderValue(targetPlayer, "%playerstats_me,mine_block%");
                }
                if (mined == null) {
                    mined = papiManager.getPlaceholderValue(targetPlayer, "%statistic_mine_block%");
                }

                if (mined != null && !mined.equals("0")) {
                    values.add("Blocks Mined: " + formatNumber(mined));
                } else {
                    values.add("Blocks Mined: 0");
                }

                // Statz provides reliable block placement tracking
                String placed = papiManager.getPlaceholderValue(targetPlayer, "%statz_blocks_placed%");
                if (placed == null) {
                    placed = papiManager.getPlaceholderValue(targetPlayer, "%playerstats_me,place_block%");
                }

                if (placed != null && !placed.equals("0")) {
                    values.add("Blocks Placed: " + formatNumber(placed));
                } else {
                    values.add("Blocks Placed: 0");
                }

                break;

            case "grinding":
                // Grinding stats using Statz plugin placeholders (more comprehensive tracking)
                String mobKills = papiManager.getPlaceholderValue(targetPlayer, "%statz_mobs_killed%");
                if (mobKills == null) {
                    mobKills = papiManager.getPlaceholderValue(targetPlayer, "%playerstats_me,mob_kills%");
                }
                if (mobKills == null) {
                    mobKills = papiManager.getPlaceholderValue(targetPlayer, "%statistic_mob_kills%");
                }

                if (mobKills != null && !mobKills.equals("0")) {
                    values.add("Mobs Killed: " + formatNumber(mobKills));
                } else {
                    values.add("Mobs Killed: 0");
                }

                // Farm mob kills using Statz specific mob tracking
                String cowKills = papiManager.getPlaceholderValue(targetPlayer, "%statz_mobs_killed_cow%");
                String pigKills = papiManager.getPlaceholderValue(targetPlayer, "%statz_mobs_killed_pig%");
                String chickenKills = papiManager.getPlaceholderValue(targetPlayer, "%statz_mobs_killed_chicken%");

                // Fallback to PlayerStats if Statz not available
                if (cowKills == null) {
                    cowKills = papiManager.getPlaceholderValue(targetPlayer, "%playerstats_me,kill_entity:cow%");
                }
                if (pigKills == null) {
                    pigKills = papiManager.getPlaceholderValue(targetPlayer, "%playerstats_me,kill_entity:pig%");
                }
                if (chickenKills == null) {
                    chickenKills = papiManager.getPlaceholderValue(targetPlayer, "%playerstats_me,kill_entity:chicken%");
                }

                int farmTotal = 0;
                try {
                    if (cowKills != null && !cowKills.equals("0")) farmTotal += Integer.parseInt(cowKills);
                    if (pigKills != null && !pigKills.equals("0")) farmTotal += Integer.parseInt(pigKills);
                    if (chickenKills != null && !chickenKills.equals("0")) farmTotal += Integer.parseInt(chickenKills);
                } catch (NumberFormatException e) {
                    // Use 0 if parsing fails
                }

                if (farmTotal > 0) {
                    values.add("Farm Mobs: " + formatNumber(String.valueOf(farmTotal)));
                } else {
                    values.add("Farm Mobs: 0");
                }
                break;

            case "economy":
                // Economy stats with balance
                String balance = papiManager.getPlaceholderValue(targetPlayer, "%vault_eco_balance%");
                if (balance == null) {
                    balance = papiManager.getPlaceholderValue(targetPlayer, "%economy_balance%");
                }

                if (balance != null && !balance.equals("0")) {
                    values.add("Balance: $" + formatNumber(balance));
                } else {
                    values.add("No economy data");
                }
                break;

            case "status":
                // Server status with rank
                String rank = papiManager.getPlaceholderValue(targetPlayer, "%vault_rank%");
                if (rank == null) {
                    rank = papiManager.getPlaceholderValue(targetPlayer, "%luckperms_primary_group_name%");
                }

                if (rank != null && !rank.isEmpty()) {
                    values.add("Rank: " + rank);
                } else {
                    values.add("No rank data");
                }
                break;

            case "experience":
                // Experience stats with level and XP gained using Statz
                String level = papiManager.getPlaceholderValue(targetPlayer, "%player_level%");
                if (level != null && !level.equals("0")) {
                    values.add("Level: " + level);
                } else {
                    values.add("Level: " + targetPlayer.getLevel());
                }

                // Use Statz XP gained tracking (more accurate than damage dealt)
                String xpGained = papiManager.getPlaceholderValue(targetPlayer, "%statz_xp_gained%");
                if (xpGained == null || xpGained.equals("0")) {
                    xpGained = papiManager.getPlaceholderValue(targetPlayer, "%player_total_experience%");
                }

                if (xpGained != null && !xpGained.equals("0")) {
                    values.add("XP Gained: " + formatNumber(xpGained));
                } else {
                    // Show current XP progress as fallback
                    int currentXP = targetPlayer.getTotalExperience();
                    if (currentXP > 0) {
                        values.add("Current XP: " + formatNumber(String.valueOf(currentXP)));
                    } else {
                        values.add("XP Gained: 0");
                    }
                }
                break;

            default:
                values.add("Unknown stat");
                break;
        }

        return values.isEmpty() ? Arrays.asList("No data") : values;
    }

    private String formatPlaytime(String playtimeString, boolean isPlayOneMinute) {
        try {
            if (isPlayOneMinute) {
                // play_one_minute is actually in ticks (despite the name)
                long ticks = Long.parseLong(playtimeString);
                long seconds = ticks / 20;
                long hours = seconds / 3600;
                long minutes = (seconds % 3600) / 60;

                if (hours > 0) {
                    return hours + "h " + minutes + "m";
                } else {
                    return minutes + "m";
                }
            } else {
                // Other playtime formats might be in minutes
                long minutes = Long.parseLong(playtimeString);
                long hours = minutes / 60;
                long remainingMinutes = minutes % 60;

                if (hours > 0) {
                    return hours + "h " + remainingMinutes + "m";
                } else {
                    return minutes + "m";
                }
            }
        } catch (NumberFormatException e) {
            // If it's not a number, return as-is (might already be formatted)
            return playtimeString != null ? playtimeString : "0m";
        }
    }


    private String formatNumber(String numberString) {
        try {
            long number = Long.parseLong(numberString);
            if (number >= 1_000_000) {
                return String.format("%.1fM", number / 1_000_000.0);
            } else if (number >= 1_000) {
                return String.format("%.1fK", number / 1_000.0);
            } else {
                return String.valueOf(number);
            }
        } catch (NumberFormatException e) {
            return numberString;
        }
    }
}
