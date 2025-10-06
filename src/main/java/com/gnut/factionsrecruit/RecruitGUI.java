package com.gnut.factionsrecruit;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

public class RecruitGUI {

    private final FactionsRecruit plugin;

    public RecruitGUI(FactionsRecruit plugin) {
        this.plugin = plugin;
    }

    public void openMainMenu(Player player, FactionApplication filter) {
        // Create compact server-themed title to fit within 30 character limit
        String title = VisualUtils.createCompactServerTitle("Recruitment Browser");
        Inventory gui = Bukkit.createInventory(null, 54, title);

        // Fill borders with PINK and WHITE glass panes
        ItemStack pinkGlassPane = createGuiItem(plugin.getConfigManager().getMaterial("main-menu.materials.border-primary", Material.PINK_STAINED_GLASS_PANE), " ");
        ItemStack whiteGlassPane = createGuiItem(plugin.getConfigManager().getMaterial("main-menu.materials.border-secondary", Material.WHITE_STAINED_GLASS_PANE), " ");

        // Top row
        for (int i = 0; i < 9; i++) {
            gui.setItem(i, (i % 2 == 0) ? pinkGlassPane : whiteGlassPane);
        }

        // Bottom row
        for (int i = 45; i < 54; i++) {
            gui.setItem(i, (i % 2 == 0) ? pinkGlassPane : whiteGlassPane);
        }

        // Side columns
        for (int i = 9; i < 45; i += 9) {
            if (i%2==0){
                gui.setItem(i, pinkGlassPane);
                gui.setItem(i + 8, pinkGlassPane);

            }else{
                gui.setItem(i, whiteGlassPane);
                gui.setItem(i + 8, whiteGlassPane);                
            }
        }

        // Player Head (Slot 4) - Enhanced Profile Display
        PlayerResume resume = plugin.getDatabaseManager().getPlayerResume(player.getUniqueId());

        // Create enhanced player profile item
        String profileTitle = player.getName() + "'s Profile";
        List<String> profileLore = new ArrayList<>();

        boolean isVisible = resume != null && resume.isLooking();
        String visibilityStatus = isVisible ? "Visible to Faction Leaders" : "Hidden from Faction Leaders";

        profileLore.add(VisualUtils.createStatusIndicator(visibilityStatus, isVisible));
        profileLore.add("");
        profileLore.add(net.md_5.bungee.api.ChatColor.of(VisualUtils.ColorPalette.INFO) +
                       VisualUtils.Symbols.ARROW_RIGHT + " Click to toggle visibility");

        ItemStack playerHead = VisualUtils.createEnhancedItem(Material.PLAYER_HEAD, profileTitle, profileLore, isVisible);
        SkullMeta playerHeadMeta = (SkullMeta) playerHead.getItemMeta();
        playerHeadMeta.setOwningPlayer(player);
        playerHead.setItemMeta(playerHeadMeta);
        gui.setItem(4, playerHead);

        // Arrow Navigation (Slots 45, 53)
        if (plugin.getCurrentMainMenuDisplayMode() == FactionsRecruit.MainMenuDisplayMode.PLAYER_MODE) {
            int totalPlayers = plugin.getDatabaseManager().getLookingPlayers(filter).size();
            int maxPages = (int) Math.ceil((double) totalPlayers / 34);
            if (plugin.getPlayerListPage() > 1) {
                gui.setItem(45, createGuiItem(plugin.getConfigManager().getMaterial("main-menu.materials.arrow", Material.ARROW), plugin.getConfigManager().getGuiString("main-menu.navigation.previous-page", "&aPrevious Page")));
            } else {
                gui.setItem(45, createGuiItem(plugin.getConfigManager().getMaterial("main-menu.materials.border-secondary", Material.WHITE_STAINED_GLASS_PANE), " "));
            }
            if (plugin.getPlayerListPage() < maxPages) {
                gui.setItem(53, createGuiItem(plugin.getConfigManager().getMaterial("main-menu.materials.arrow", Material.ARROW), plugin.getConfigManager().getGuiString("main-menu.navigation.next-page", "&aNext Page")));
            } else {
                gui.setItem(53, createGuiItem(plugin.getConfigManager().getMaterial("main-menu.materials.border-secondary", Material.WHITE_STAINED_GLASS_PANE), " "));
            }
        } else {
            int totalFactions = plugin.getDatabaseManager().getRecruitingFactions(filter).size();
            int maxPages = (int) Math.ceil((double) totalFactions / 34);
            if (plugin.getFactionListPage() > 1) {
                gui.setItem(45, createGuiItem(plugin.getConfigManager().getMaterial("main-menu.materials.arrow", Material.ARROW), plugin.getConfigManager().getGuiString("main-menu.navigation.previous-page", "&aPrevious Page")));
            } else {
                gui.setItem(45, createGuiItem(plugin.getConfigManager().getMaterial("main-menu.materials.border-secondary", Material.WHITE_STAINED_GLASS_PANE), " "));
            }
            if (plugin.getFactionListPage() < maxPages) {
                gui.setItem(53, createGuiItem(plugin.getConfigManager().getMaterial("main-menu.materials.arrow", Material.ARROW), plugin.getConfigManager().getGuiString("main-menu.navigation.next-page", "&aNext Page")));
            } else {
                gui.setItem(53, createGuiItem(plugin.getConfigManager().getMaterial("main-menu.materials.border-secondary", Material.WHITE_STAINED_GLASS_PANE), " "));
            }
        }

        // Lectern (Slot 46) - Manage Resume
        gui.setItem(46, createGuiItem(plugin.getConfigManager().getMaterial("main-menu.materials.manage-resume", Material.LECTERN), plugin.getConfigManager().getGuiString("main-menu.manage-resume.name", "&6Manage Resume"), plugin.getConfigManager().getGuiStringList("main-menu.manage-resume.lore", Arrays.asList("&7Edit your recruitment profile", "&7and skills information"))));

        // Jukebox (Slot 47) - Manage Applications
        if (plugin.getPapiIntegrationManager().isInFaction(player) && plugin.getPapiIntegrationManager().hasRole(player, "admin")) {
            gui.setItem(47, createGuiItem(plugin.getConfigManager().getMaterial("main-menu.materials.manage-applications", Material.JUKEBOX), plugin.getConfigManager().getGuiString("main-menu.manage-applications.name", "&6Manage Applications"), plugin.getConfigManager().getGuiStringList("main-menu.manage-applications.lore", Arrays.asList("&7Set faction requirements", "&7and view applications"))));
        } else {
            gui.setItem(47, pinkGlassPane);
        }

        // Book (Slot 48) - Help & Commands
        gui.setItem(48, createGuiItem(plugin.getConfigManager().getMaterial("main-menu.materials.help-and-commands", Material.BOOK), plugin.getConfigManager().getGuiString("main-menu.help-and-commands.name", "&6Help & Commands"), plugin.getConfigManager().getGuiStringList("main-menu.help-and-commands.lore", Arrays.asList("&7/recruit - Open browser", "&7/recruit <player> - View player resume", "", "&eClick items to interact!"))));

        // Barrier (Slot 49) - Close
        gui.setItem(49, createGuiItem(plugin.getConfigManager().getMaterial("main-menu.materials.close-button", Material.BARRIER), plugin.getConfigManager().getGuiString("main-menu.close-button.name", "&cClose")));

        // Compass (Slot 50) - Search
        gui.setItem(50, createGuiItem(plugin.getConfigManager().getMaterial("main-menu.materials.search-button", Material.COMPASS), plugin.getConfigManager().getGuiString("main-menu.search-button.name", "&6Search"), plugin.getCurrentMainMenuDisplayMode() == FactionsRecruit.MainMenuDisplayMode.PLAYER_MODE ? plugin.getConfigManager().getGuiString("main-menu.search-button.lore-players", "&7Search for player names") : plugin.getConfigManager().getGuiString("main-menu.search-button.lore-factions", "&7Search for faction names")));

        // Sword/Banner (Slot 51) - Toggle View
        if (plugin.getCurrentMainMenuDisplayMode() == FactionsRecruit.MainMenuDisplayMode.PLAYER_MODE) {
            gui.setItem(51, createGuiItem(plugin.getConfigManager().getMaterial("main-menu.materials.browsing-players", Material.NETHERITE_SWORD), plugin.getConfigManager().getGuiString("main-menu.browsing-players.name", "&6Browsing Players"), plugin.getConfigManager().getGuiString("main-menu.browsing-players.lore", "&7Shows player heads")));
        } else {
            gui.setItem(51, createGuiItem(plugin.getConfigManager().getMaterial("main-menu.materials.browsing-factions", Material.YELLOW_BANNER), plugin.getConfigManager().getGuiString("main-menu.browsing-factions.name", "&6Browsing Factions"), plugin.getConfigManager().getGuiString("main-menu.browsing-factions.lore", "&7Shows faction banners")));
        }

        // Hopper (Slot 52) - Filter Results
        gui.setItem(52, createGuiItem(plugin.getConfigManager().getMaterial("main-menu.materials.filter-results", Material.HOPPER), plugin.getConfigManager().getGuiString("main-menu.filter-results.name", "&6Filter Results"), plugin.getConfigManager().getGuiStringList("main-menu.filter-results.lore", Arrays.asList("&7Filter by timezone, experience,", "&7skills, and availability"))));

        // Populate content display (Slots 10-43)
        if (plugin.getCurrentMainMenuDisplayMode() == FactionsRecruit.MainMenuDisplayMode.PLAYER_MODE) {
            List<PlayerResume> lookingPlayers;
            if (plugin.getSearchResultPlayers() != null) {
                lookingPlayers = plugin.getSearchResultPlayers();
                plugin.clearSearchResults(); // Clear after use
            } else {
                lookingPlayers = plugin.getDatabaseManager().getLookingPlayers(filter);
            }
            int page = plugin.getPlayerListPage();
            int startIndex = (page - 1) * 34;
            int endIndex = Math.min(startIndex + 34, lookingPlayers.size());

            for (int i = startIndex; i < endIndex; i++) {
                PlayerResume playerResume = lookingPlayers.get(i);
                String playerName = Bukkit.getOfflinePlayer(playerResume.getPlayerUUID()).getName();

                // Create enhanced player item using VisualUtils
                ItemStack playerHeadItem = VisualUtils.createPlayerItem(
                    Material.PLAYER_HEAD,
                    playerName,
                    playerResume.getExperience(),
                    playerResume.getTimezone(),
                    String.join(", ", playerResume.getSkills())
                );

                // Set the skull owner
                SkullMeta skullMeta = (SkullMeta) playerHeadItem.getItemMeta();
                skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(playerResume.getPlayerUUID()));

                // Add availability info to existing lore
                List<String> currentLore = skullMeta.getLore();
                if (currentLore != null) {
                    List<String> newLore = new ArrayList<>(currentLore);
                    // Insert availability after timezone line
                    newLore.add(newLore.size() - 2, net.md_5.bungee.api.ChatColor.of(VisualUtils.ColorPalette.TEXT_SECONDARY) +
                        VisualUtils.Symbols.DIAMOND + " Available: " +
                        net.md_5.bungee.api.ChatColor.of(VisualUtils.ColorPalette.TEXT_PRIMARY) +
                        String.join(", ", playerResume.getAvailableDays()));
                    skullMeta.setLore(newLore);
                }

                playerHeadItem.setItemMeta(skullMeta);
                gui.setItem(10 + (i - startIndex), playerHeadItem);
            }
        } else { // FACTION_MODE
            List<FactionApplication> recruitingFactions;
            if (plugin.getSearchResultFactions() != null) {
                recruitingFactions = plugin.getSearchResultFactions();
                plugin.clearSearchResults(); // Clear after use
            } else {
                recruitingFactions = plugin.getDatabaseManager().getRecruitingFactions(filter);
            }
            int page = plugin.getFactionListPage();
            int startIndex = (page - 1) * 34;
            int endIndex = Math.min(startIndex + 34, recruitingFactions.size());

            for (int i = startIndex; i < endIndex; i++) {
                FactionApplication factionApp = recruitingFactions.get(i);
                String leaderName = Bukkit.getOfflinePlayer(factionApp.getLeaderUuid()).getName();

                // Get faction size using PAPI
                int factionSize = plugin.getPapiIntegrationManager().getFactionSize(Bukkit.getOfflinePlayer(factionApp.getLeaderUuid()));
                int maxMembers = plugin.getConfigManager().getFactionMemberLimit();

                // Create enhanced faction item using VisualUtils
                ItemStack factionBannerItem = VisualUtils.createFactionItem(
                    Material.YELLOW_BANNER,
                    factionApp.getFactionName(),
                    leaderName,
                    factionSize,
                    maxMembers
                );

                // Add additional faction-specific lore
                ItemMeta meta = factionBannerItem.getItemMeta();
                List<String> currentLore = meta.getLore();
                if (currentLore != null) {
                    List<String> newLore = new ArrayList<>(currentLore);
                    // Remove the last "Click to view details" line temporarily
                    newLore.remove(newLore.size() - 1);
                    newLore.remove(newLore.size() - 1); // Remove empty line too

                    // Add faction-specific requirements
                    newLore.add(net.md_5.bungee.api.ChatColor.of(VisualUtils.ColorPalette.TEXT_SECONDARY) +
                        VisualUtils.Symbols.HOURGLASS + " Timezones: " +
                        net.md_5.bungee.api.ChatColor.of(VisualUtils.ColorPalette.TEXT_PRIMARY) +
                        String.join(", ", factionApp.getDesiredTimezones()));

                    newLore.add(net.md_5.bungee.api.ChatColor.of(VisualUtils.ColorPalette.TEXT_SECONDARY) +
                        VisualUtils.Symbols.SWORD + " Looking for: " +
                        net.md_5.bungee.api.ChatColor.of(VisualUtils.ColorPalette.TEXT_PRIMARY) +
                        String.join(", ", factionApp.getDesiredSkills()));

                    // Add hidden faction ID for click handling
                    String factionId = factionApp.getFactionId();
                    if (plugin.getConfigManager().isDebugLoggingEnabled()) {
                        plugin.getLogger().info("[DEBUG] Adding faction ID to banner lore: '" + factionId + "' for faction: " + factionApp.getFactionName());
                    }
                    newLore.add(net.md_5.bungee.api.ChatColor.of(VisualUtils.ColorPalette.TEXT_MUTED) + factionId);

                    // Add action line
                    newLore.add("");
                    newLore.add(net.md_5.bungee.api.ChatColor.of(VisualUtils.ColorPalette.INFO) +
                        VisualUtils.Symbols.ARROW_RIGHT + " Click to apply");

                    meta.setLore(newLore);
                }

                factionBannerItem.setItemMeta(meta);
                gui.setItem(10 + (i - startIndex), factionBannerItem);
            }
        }

        player.openInventory(gui);
    }

    public ItemStack createGuiItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            if (lore.length > 0) {
                meta.setLore(Arrays.asList(lore));
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack createGuiItem(Material material, String name, java.util.List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    public void updateGuiItemEnchantment(ItemStack item, boolean enchant) {
        if (item == null) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        if (enchant) {
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        } else {
            meta.removeEnchant(Enchantment.UNBREAKING);
            meta.removeItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        item.setItemMeta(meta);
    }
}
