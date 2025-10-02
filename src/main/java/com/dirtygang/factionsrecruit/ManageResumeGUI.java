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

public class ManageResumeGUI {

    private final FactionsRecruit plugin;

    public ManageResumeGUI(FactionsRecruit plugin) {
        this.plugin = plugin;
    }

    public void openManageResumeUI(Player player) {
        plugin.setFilter(null);

        // Create compact server-themed title to fit within 30 character limit
        String title = VisualUtils.createCompactServerTitle("Profile Manager");
        Inventory gui = Bukkit.createInventory(null, 27, title);

        // Fill borders with WHITE and PINK glass panes
        ItemStack whiteGlassPane = createGuiItem(plugin.getConfigManager().getMaterial("manage-resume.materials.border-primary", Material.WHITE_STAINED_GLASS_PANE), " ");
        ItemStack pinkGlassPane = createGuiItem(plugin.getConfigManager().getMaterial("manage-resume.materials.border-secondary", Material.PINK_STAINED_GLASS_PANE), " ");

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

        // Enhanced Edit Resume Button (11)
        List<String> editLore = Arrays.asList(
            net.md_5.bungee.api.ChatColor.of(VisualUtils.ColorPalette.TEXT_SECONDARY) + "Modify your profile information",
            net.md_5.bungee.api.ChatColor.of(VisualUtils.ColorPalette.TEXT_SECONDARY) + "and availability settings",
            "",
            VisualUtils.createActionLore("Click to edit", VisualUtils.ColorPalette.INFO)
        );
        ItemStack editButton = VisualUtils.createServerItem(
            plugin.getConfigManager().getMaterial("manage-resume.materials.edit-resume", Material.LECTERN),
            "Edit Resume",
            editLore,
            true
        );
        gui.setItem(11, editButton);

        // Enhanced Player Head (13)
        PlayerResume resume = plugin.getDatabaseManager().getPlayerResume(player.getUniqueId());
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
            profileLore.add(VisualUtils.createServerStatusIndicator("Looking for Faction: " + (isLooking ? "Yes" : "No"), isLooking));
        } else {
            profileLore.add(VisualUtils.createServerDivider());
            profileLore.add(net.md_5.bungee.api.ChatColor.of(VisualUtils.ColorPalette.WARNING) + VisualUtils.Symbols.CROSS + " No resume found - Click Edit to create one!");
            profileLore.add(VisualUtils.createServerDivider());
        }

        ItemStack playerHead = VisualUtils.createServerItem(Material.PLAYER_HEAD, player.getName() + "'s Profile", profileLore, false);
        SkullMeta skullMeta = (SkullMeta) playerHead.getItemMeta();
        skullMeta.setOwningPlayer(player);
        playerHead.setItemMeta(skullMeta);
        gui.setItem(13, playerHead);

        // Enhanced Visibility Status (15)
        boolean isVisible = resume != null && resume.isLooking();
        String statusTitle = "Resume Visibility";
        String statusText = isVisible ? "Visible to Faction Leaders" : "Hidden from Faction Leaders";
        Material statusMaterial = isVisible ? Material.EMERALD : Material.REDSTONE;

        List<String> statusLore = Arrays.asList(
            net.md_5.bungee.api.ChatColor.of(VisualUtils.ColorPalette.TEXT_SECONDARY) + "Current Status: " +
                net.md_5.bungee.api.ChatColor.of(isVisible ? VisualUtils.ColorPalette.SUCCESS : VisualUtils.ColorPalette.ERROR) + statusText,
            "",
            VisualUtils.createActionLore("Click to toggle", VisualUtils.ColorPalette.INFO)
        );

        ItemStack statusItem = VisualUtils.createServerItem(statusMaterial, statusTitle, statusLore, isVisible);
        gui.setItem(15, statusItem);

        // Enhanced View Pending Button (22)
        List<String> pendingLore = Arrays.asList(
            net.md_5.bungee.api.ChatColor.of(VisualUtils.ColorPalette.TEXT_SECONDARY) + "Check your faction applications",
            net.md_5.bungee.api.ChatColor.of(VisualUtils.ColorPalette.TEXT_SECONDARY) + "and pending invitations",
            "",
            VisualUtils.createActionLore("Click to view", VisualUtils.ColorPalette.INFO)
        );
        ItemStack pendingButton = VisualUtils.createServerItem(
            plugin.getConfigManager().getMaterial("manage-resume.materials.view-pending", Material.BOOKSHELF),
            "View Pending",
            pendingLore,
            false
        );
        gui.setItem(22, pendingButton);

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
