package com.gnut.factionsrecruit;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import com.gnut.factionsrecruit.GuiManager.InventoryClickHandler;
import com.gnut.factionsrecruit.util.ItemBuilder;

import org.bukkit.ChatColor;

import java.util.function.BiConsumer;
import java.util.ArrayList;
import java.util.List;

public class ConfirmationGUI {

    private final FactionsRecruit plugin;
    private final Player player;
    private final String title;
    private final String message;
    private final Runnable onConfirm;
    private final Runnable onCancel;
    private final Inventory inventory;
    private final PlayerResume resumeToConfirm; // New field

    public ConfirmationGUI(FactionsRecruit plugin, Player player, String title, String message, Runnable onConfirm, Runnable onCancel) {
        this(plugin, player, title, message, null, onConfirm, onCancel); // Call new constructor
    }

    public ConfirmationGUI(FactionsRecruit plugin, Player player, String title, String message, PlayerResume resumeToConfirm, Runnable onConfirm, Runnable onCancel) {
        this.plugin = plugin;
        this.player = player;
        this.title = title;
        this.message = message;
        this.resumeToConfirm = resumeToConfirm; // Initialize new field
        this.onConfirm = onConfirm;
        this.onCancel = onCancel;
        this.inventory = Bukkit.createInventory(null, 27, title);
        setupGUI();
    }

    private void setupGUI() {
        // Fill empty slots with black stained glass panes
        ItemStack filler = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).setName(" ").build();
        for (int i = 0; i < 27; i++) {
            inventory.setItem(i, filler);
        }

        // Confirmation message item (or Player Head if resume is present)
        ItemStack centralItem;
        if (resumeToConfirm != null) {
            // Create a player head with resume details
            centralItem = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta skullMeta = (SkullMeta) centralItem.getItemMeta();
            if (skullMeta != null) {
                skullMeta.setOwningPlayer(player); // Use the player viewing the GUI
                skullMeta.setDisplayName(plugin.getConfigManager().getGuiString("confirmation-gui.new-resume-title", "&6%player_name%'s New Resume").replace("%player_name%", player.getName()));
                List<String> lore = new ArrayList<>();

                // Convert database keys to display names for user-friendly display
                String displayTimezone = plugin.getConfigManager().getDisplayName(resumeToConfirm.getTimezone());
                String displayExperience = plugin.getConfigManager().getDisplayName(resumeToConfirm.getExperience());

                // Convert collections of database keys to display names
                List<String> displayDays = new ArrayList<>();
                for (String dbDay : resumeToConfirm.getAvailableDays()) {
                    displayDays.add(plugin.getConfigManager().getDisplayName(dbDay));
                }

                List<String> displaySkills = new ArrayList<>();
                for (String dbSkill : resumeToConfirm.getSkills()) {
                    displaySkills.add(plugin.getConfigManager().getDisplayName(dbSkill));
                }

                lore.add(plugin.getConfigManager().getGuiString("confirmation-gui.resume-lore.timezone", "&7Timezone: &f%timezone%").replace("%timezone%", displayTimezone));
                lore.add(plugin.getConfigManager().getGuiString("confirmation-gui.resume-lore.experience", "&7Experience: &f%experience%").replace("%experience%", displayExperience));
                lore.add(plugin.getConfigManager().getGuiString("confirmation-gui.resume-lore.available-days", "&7Available Days: &f%days%").replace("%days%", String.join(", ", displayDays)));
                lore.add(plugin.getConfigManager().getGuiString("confirmation-gui.resume-lore.skills", "&7Skills: &f%skills%").replace("%skills%", String.join(", ", displaySkills)));
                lore.add(plugin.getConfigManager().getGuiString("confirmation-gui.resume-lore.looking-for-faction", "&7Looking for Faction: &f%status%").replace("%status%", (resumeToConfirm.isLooking() ? plugin.getConfigManager().getGuiString("confirmation-gui.resume-lore.status-yes", "Yes") : plugin.getConfigManager().getGuiString("confirmation-gui.resume-lore.status-no", "No"))));
                lore.add(plugin.getConfigManager().getGuiString("confirmation-gui.resume-lore.visibility", "&7Visibility: &f%status%").replace("%status%", (resumeToConfirm.isHidden() ? plugin.getConfigManager().getGuiString("confirmation-gui.resume-lore.status-hidden", "Hidden") : plugin.getConfigManager().getGuiString("confirmation-gui.resume-lore.status-visible", "Visible"))));
                skullMeta.setLore(lore);
                centralItem.setItemMeta(skullMeta);
            }
        } else {
            // Fallback to generic message item
            centralItem = new ItemBuilder(Material.PAPER)
                    .setName(plugin.getConfigManager().getGuiString("confirmation-gui.message.name", "&eAre you sure?"))
                    .setLore(replacePlaceholder(plugin.getConfigManager().getGuiStringList("confirmation-gui.message.lore", new ArrayList<>()), "%message%", message))
                    .build();
        }
        inventory.setItem(4, centralItem); // Center top

        // Confirm Button (Green Wool)
        ItemStack confirmButton = new ItemBuilder(Material.GREEN_WOOL)
                .setName(plugin.getConfigManager().getGuiString("confirmation-gui.confirm.name", "&aConfirm"))
                .setLore(plugin.getConfigManager().getGuiStringList("confirmation-gui.confirm.lore", new ArrayList<>()))
                .build();
        inventory.setItem(11, confirmButton); // Left center

        // Cancel Button (Red Wool)
        ItemStack cancelButton = new ItemBuilder(Material.RED_WOOL)
                .setName(plugin.getConfigManager().getGuiString("confirmation-gui.cancel.name", "&cCancel"))
                .setLore(plugin.getConfigManager().getGuiStringList("confirmation-gui.cancel.lore", new ArrayList<>()))
                .build();
        inventory.setItem(15, cancelButton); // Right center
    }

    public void open() {
        player.openInventory(inventory);
        plugin.getGuiManager().addActiveGUI(player.getUniqueId(), (InventoryClickHandler) this::handleClick); // Cast to InventoryClickHandler
    }

    private void handleClick(Player player, int slot, ClickType clickType) {
        if (slot == 11) { // Confirm button
            onConfirm.run();
            plugin.getGuiManager().removeActiveGUI(player.getUniqueId());
            player.closeInventory();
        } else if (slot == 15) { // Cancel button
            onCancel.run();
            plugin.getGuiManager().removeActiveGUI(player.getUniqueId());
            player.closeInventory();
        } else {
            // User clicked on a non-interactive element (decorative border, etc.)
            // Provide subtle feedback without closing the GUI
            player.sendMessage(ChatColor.YELLOW + "Please click the green or red buttons to confirm or cancel.");
        }
    }

    private List<String> replacePlaceholder(List<String> list, String placeholder, String replacement) {
        List<String> newList = new ArrayList<>();
        for (String s : list) {
            newList.add(s.replace(placeholder, replacement));
        }
        return newList;
    }
}
