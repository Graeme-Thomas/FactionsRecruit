package com.gnut.factionsrecruit.interfaces;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.gnut.factionsrecruit.FactionsRecruit;
import com.gnut.factionsrecruit.model.FactionApplication;
import com.gnut.factionsrecruit.util.VisualUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ManageApplicationGUI {

    private final FactionsRecruit plugin;

    public ManageApplicationGUI(FactionsRecruit plugin) {
        this.plugin = plugin;
    }

    public void openManageApplicationUI(Player player) {
        // Create compact server-themed title to prevent overflow
        String title = VisualUtils.createCompactServerTitle("Faction Manager");
        Inventory gui = Bukkit.createInventory(null, 27, title);

        // Fill borders with WHITE and PINK glass panes
        ItemStack whiteGlassPane = createGuiItem(plugin.getConfigManager().getMaterial("manage-application.materials.border-primary", Material.WHITE_STAINED_GLASS_PANE), " ");
        ItemStack pinkGlassPane = createGuiItem(plugin.getConfigManager().getMaterial("manage-application.materials.border-secondary", Material.PINK_STAINED_GLASS_PANE), " ");

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

        // Lectern (11)
        gui.setItem(11, createGuiItem(plugin.getConfigManager().getMaterial("manage-application.materials.edit-requirements", Material.LECTERN), plugin.getConfigManager().getGuiString("manage-application.edit-requirements", "&6Edit faction requirements")));

        // Banner (13)
        FactionApplication factionApplication = plugin.getDatabaseManager().getFactionApplication(plugin.getPapiIntegrationManager().getFactionInternalId(player));
        if (factionApplication == null) {
            factionApplication = new FactionApplication(
                plugin.getPapiIntegrationManager().getFactionInternalId(player),
                plugin.getPapiIntegrationManager().getFactionName(player),
                player.getUniqueId(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                true,
                System.currentTimeMillis(),
                System.currentTimeMillis()
            );
            // Optionally save the new application to the database immediately
            plugin.getDatabaseManager().saveFactionApplication(factionApplication);
        }
        ItemStack banner = createGuiItem(plugin.getConfigManager().getMaterial("manage-application.materials.banner", Material.YELLOW_BANNER), "§6" + factionApplication.getFactionName());
        ItemMeta bannerMeta = banner.getItemMeta();
        List<String> bannerLore = new ArrayList<>();
        bannerLore.add("§7Status: " + (factionApplication.isAccepting() ? plugin.getConfigManager().getGuiString("manage-application.faction-status.accepting", "&aAccepting Applications") : plugin.getConfigManager().getGuiString("manage-application.faction-status.not-accepting", "&cNot Accepting Applications")));
        bannerMeta.setLore(bannerLore);
        banner.setItemMeta(bannerMeta);
        gui.setItem(13, banner);

        // Enhanced Status Display (15)
        boolean isAccepting = factionApplication.isAccepting();
        String statusTitle = "Application Status";
        String statusText = isAccepting ? "Accepting Applications" : "Not Accepting Applications";
        Material statusMaterial = isAccepting ? Material.EMERALD : Material.REDSTONE;

        ItemStack statusItem = VisualUtils.createStatusItem(statusMaterial, statusTitle, statusText, isAccepting);
        gui.setItem(15, statusItem);

        // Bookshelf (22)
        gui.setItem(22, createGuiItem(plugin.getConfigManager().getMaterial("manage-application.materials.view-pending", Material.BOOKSHELF), plugin.getConfigManager().getGuiString("manage-application.view-pending", "&6View pending applications")));

        player.openInventory(gui);
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
