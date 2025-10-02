package com.dirtygang.factionsrecruit;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import java.util.List;
import java.util.ArrayList;

public class FactionApplicationReviewGUI {

    private final FactionsRecruit plugin;
    private final RecruitGUI recruitGUI;

    public FactionApplicationReviewGUI(FactionsRecruit plugin, RecruitGUI recruitGUI) {
        this.plugin = plugin;
        this.recruitGUI = recruitGUI;
    }

    public void openFactionApplicationReviewUI(Player player) {
        // Create compact server-themed title to fit within 30 character limit
        String title = VisualUtils.createCompactServerTitle("Applications");
        Inventory gui = Bukkit.createInventory(null, 54, title);

        // Fill borders with PINK and WHITE glass panes
        ItemStack pinkGlassPane = recruitGUI.createGuiItem(Material.PINK_STAINED_GLASS_PANE, " ");
        ItemStack whiteGlassPane = recruitGUI.createGuiItem(Material.WHITE_STAINED_GLASS_PANE, " ");

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
        // Faction Banner (Slot 4)
        ItemStack factionBanner = recruitGUI.createGuiItem(Material.YELLOW_BANNER, plugin.getConfigManager().getGuiString("faction-application-review.faction-banner-name", "&6%faction_name%").replace("%faction_name%", plugin.getPapiIntegrationManager().getFactionName(player)));
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
            meta.setDisplayName(plugin.getConfigManager().getGuiString("faction-application-review.player-head-name", "&b%player_name%").replace("%player_name%", Bukkit.getOfflinePlayer(application.getPlayerUuid()).getName()));
            List<String> lore = new ArrayList<>();
            lore.add(plugin.getConfigManager().getGuiString("faction-application-review.lore.status", "&7Status: &f%status%").replace("%status%", application.getStatus()));
            lore.add(plugin.getConfigManager().getGuiString("faction-application-review.lore.applied", "&7Applied: &f%date%").replace("%date%", new java.util.Date(application.getApplicationDate()).toString()));
            lore.add(plugin.getConfigManager().getGuiString("faction-application-review.lore.expires", "&7Expires: &f%date%").replace("%date%", new java.util.Date(application.getExpiresAt()).toString()));
            meta.setLore(lore);
            playerHeadItem.setItemMeta(meta);
            gui.setItem(slot++, playerHeadItem);
        }

        player.openInventory(gui);
    }
}
