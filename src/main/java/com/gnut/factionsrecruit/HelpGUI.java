package com.gnut.factionsrecruit;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class HelpGUI {

    private final FactionsRecruit plugin;

    public HelpGUI(FactionsRecruit plugin) {
        this.plugin = plugin;
    }

    public void openHelpUI(Player player) {
        // Create compact server-themed title to fit within 30 character limit
        String title = VisualUtils.createCompactServerTitle("Help & Commands");
        Inventory gui = Bukkit.createInventory(null, 27, title);

        // Fill borders with WHITE and PINK glass panes
        ItemStack whiteGlassPane = createGuiItem(plugin.getConfigManager().getMaterial("help.materials.border-primary", Material.WHITE_STAINED_GLASS_PANE), " ");
        ItemStack pinkGlassPane = createGuiItem(plugin.getConfigManager().getMaterial("help.materials.border-secondary", Material.PINK_STAINED_GLASS_PANE), " ");

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

        // Help Book
        ItemStack helpBook = createGuiItem(plugin.getConfigManager().getMaterial("help.materials.help-book", Material.WRITTEN_BOOK), plugin.getConfigManager().getGuiString("help.title", "&6Help & Commands"),
                plugin.getConfigManager().getGuiStringList("help.lore", Arrays.asList("&7/recruit - Open browser", "&7/recruit <player> - View player resume", "", "&eClick items to interact!")));
        gui.setItem(13, helpBook);

        // Back Button
        gui.setItem(22, createGuiItem(plugin.getConfigManager().getMaterial("help.materials.back-button", Material.ARROW),  plugin.getConfigManager().getGuiString("help.back-button", "&aGo Back")));

        player.openInventory(gui);
    }

    private ItemStack createGuiItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        if (item.getItemMeta() != null) {
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(name);
            if (lore.length > 0) {
                meta.setLore(Arrays.asList(lore));
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createGuiItem(Material material, String name, java.util.List<String> lore) {
        ItemStack item = new ItemStack(material);
        if (item.getItemMeta() != null) {
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(name);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
}
