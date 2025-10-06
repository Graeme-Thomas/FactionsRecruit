package com.gnut.factionsrecruit.interfaces;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import com.gnut.factionsrecruit.FactionsRecruit;
import com.gnut.factionsrecruit.model.PlayerApplication;
import com.gnut.factionsrecruit.util.VisualUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PendingApplicationsGUI {

    private final FactionsRecruit plugin;

    public PendingApplicationsGUI(FactionsRecruit plugin) {
        this.plugin = plugin;
    }

    public void openPendingApplicationsUI(Player player) {
        if (plugin.getPapiIntegrationManager().isInFaction(player) && plugin.getPapiIntegrationManager().hasRole(player, "admin")) {
            // Faction Owner Mode - Create compact server-themed title
            String title = VisualUtils.createCompactServerTitle("Pending Applications");
            Inventory gui = Bukkit.createInventory(null, 54, title);

            // Fill borders with PINK and WHITE glass panes
            ItemStack pinkGlassPane = createGuiItem(plugin.getConfigManager().getMaterial("pending-applications.materials.border-primary", Material.PINK_STAINED_GLASS_PANE), " ");
            ItemStack whiteGlassPane = createGuiItem(plugin.getConfigManager().getMaterial("pending-applications.materials.border-secondary", Material.WHITE_STAINED_GLASS_PANE), " ");

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

            } else {
                gui.setItem(i, whiteGlassPane);
                gui.setItem(i + 8, whiteGlassPane);                
            }
        }

            // Faction Banner (Slot 4)
            ItemStack factionBanner = createGuiItem(plugin.getConfigManager().getMaterial("pending-applications.materials.faction-banner", Material.YELLOW_BANNER), "§6" + plugin.getPapiIntegrationManager().getFactionName(player));
            gui.setItem(4, factionBanner);

            // Populate content display (Slots 10-43)
            List<PlayerApplication> pendingApplications = plugin.getDatabaseManager().getPlayerApplicationsByFaction(plugin.getPapiIntegrationManager().getFactionInternalId(player));
            int slot = 10;
            for (PlayerApplication application : pendingApplications) {
                ItemStack playerHeadItem = new ItemStack(Material.PLAYER_HEAD);
                SkullMeta skullMeta = (SkullMeta) playerHeadItem.getItemMeta();
                skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(application.getPlayerUuid()));
                playerHeadItem.setItemMeta(skullMeta);

                ItemMeta meta = playerHeadItem.getItemMeta();
                meta.setDisplayName(plugin.getConfigManager().getGuiString("main-menu.player-item.name", "&b%player_name%").replace("%player_name%", Bukkit.getOfflinePlayer(application.getPlayerUuid()).getName()));
                List<String> lore = new ArrayList<>();
                lore.add(plugin.getConfigManager().getGuiString("pending-applications.application-item.status", "&7Status: &f%status%").replace("%status%", application.getStatus()));
                lore.add(plugin.getConfigManager().getGuiString("pending-applications.application-item.applied", "&7Applied: &f%date%").replace("%date%", new java.util.Date(application.getApplicationDate()).toString()));
                lore.add(plugin.getConfigManager().getGuiString("pending-applications.application-item.expires", "&7Expires: &f%date%").replace("%date%", new java.util.Date(application.getExpiresAt()).toString()));
                lore.add("§8" + application.getId()); // Store application ID in lore
                meta.setLore(lore);
                playerHeadItem.setItemMeta(meta);
                gui.setItem(slot++, playerHeadItem);
            }

            player.openInventory(gui);
        } else {
            // Player Mode - Create compact server-themed title
            String title = VisualUtils.createCompactServerTitle("Your Applications");
            Inventory gui = Bukkit.createInventory(null, 27, title);

            // Fill borders with WHITE and PINK glass panes
            ItemStack whiteGlassPane = createGuiItem(plugin.getConfigManager().getMaterial("pending-applications.materials.border-secondary", Material.WHITE_STAINED_GLASS_PANE), " ");
            ItemStack pinkGlassPane = createGuiItem(plugin.getConfigManager().getMaterial("pending-applications.materials.border-primary", Material.PINK_STAINED_GLASS_PANE), " ");

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

            // Application Slots (12-14)
            List<PlayerApplication> playerApplications = plugin.getDatabaseManager().getPlayerApplicationsByPlayer(player.getUniqueId());
            int[] appSlots = {12, 13, 14};
            for (int i = 0; i < plugin.getConfigManager().getApplicationSlotsPerPlayer(); i++) {
                if (i < playerApplications.size()) {
                    PlayerApplication application = playerApplications.get(i);
                    ItemStack factionBanner = createGuiItem(plugin.getConfigManager().getMaterial("pending-applications.materials.faction-banner", Material.YELLOW_BANNER), plugin.getConfigManager().getGuiString("main-menu.faction-item.name", "&b%faction_name%").replace("%faction_name%", plugin.getPapiIntegrationManager().getFactionName(Bukkit.getOfflinePlayer(application.getPlayerUuid()))));
                    ItemMeta meta = factionBanner.getItemMeta();
                    List<String> lore = new ArrayList<>();
                    lore.add(plugin.getConfigManager().getGuiString("pending-applications.application-item.status", "&7Status: &f%status%").replace("%status%", application.getStatus()));
                    lore.add(plugin.getConfigManager().getGuiString("pending-applications.application-item.expires", "&7Expires: &f%date%").replace("%date%", new java.util.Date(application.getExpiresAt()).toString()));
                    lore.add(plugin.getConfigManager().getGuiString("pending-applications.application-item.applied", "&7Applied: &f%date%").replace("%date%", new java.util.Date(application.getApplicationDate()).toString()));
                    lore.add("§8" + application.getId()); // Store application ID in lore
                    meta.setLore(lore);
                    factionBanner.setItemMeta(meta);
                    gui.setItem(appSlots[i], factionBanner);
                } else {
                    gui.setItem(appSlots[i], createGuiItem(plugin.getConfigManager().getMaterial("pending-applications.materials.empty-slot", Material.BLACK_STAINED_GLASS_PANE), plugin.getConfigManager().getGuiString("pending-applications.empty-slot", "&7Empty Slot")));
                }
            }

            // Back Button (22)
            gui.setItem(22, createGuiItem(plugin.getConfigManager().getMaterial("pending-applications.materials.back-button", Material.BARRIER), plugin.getConfigManager().getGuiString("pending-applications.back-button", "&cBack")));

            player.openInventory(gui);
        }
    }

    private ItemStack createGuiItem(Material material, String name, String... lore) {
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
}
