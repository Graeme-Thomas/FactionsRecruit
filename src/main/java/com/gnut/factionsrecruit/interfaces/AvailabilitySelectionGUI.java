package com.gnut.factionsrecruit.interfaces;

import com.gnut.factionsrecruit.VisualUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

/**
 * Availability Selection GUI - Select hours available per week
 * Uses 1-10 scale where each level represents a range of hours
 */
public class AvailabilitySelectionGUI implements InventoryHolder, Listener {

    private final Inventory inventory;
    private final Player player;
    private final ApplicationSettingsGUI parentGUI;

    // Hour ranges for each level
    private static final String[] HOUR_RANGES = {
        "Less than 10 hours",      // 1
        "10-20 hours",             // 2
        "20-30 hours",             // 3
        "30-40 hours",             // 4
        "40-50 hours",             // 5
        "50-60 hours",             // 6
        "60-70 hours",             // 7
        "70-80 hours",             // 8
        "80-90 hours",             // 9
        "More than 100 hours"      // 10
    };

    // Terracotta gradient from red to green (same as OneToTenGUI)
    private static final Material[] TERRACOTTA_GRADIENT = {
        Material.RED_TERRACOTTA,
        Material.ORANGE_TERRACOTTA,
        Material.YELLOW_TERRACOTTA,
        Material.LIME_TERRACOTTA,
        Material.LIGHT_BLUE_TERRACOTTA,
        Material.CYAN_TERRACOTTA,
        Material.LIME_TERRACOTTA,
        Material.GREEN_TERRACOTTA,
        Material.GREEN_TERRACOTTA,
        Material.GREEN_TERRACOTTA
    };

    public AvailabilitySelectionGUI(Player player, ApplicationSettingsGUI parentGUI) {
        this.player = player;
        this.parentGUI = parentGUI;
        this.inventory = Bukkit.createInventory(this, 36, VisualUtils.createCompactServerTitle("Hours per week"));
        initializeItems();
    }

    private void initializeItems() {
        // Create border with alternating pink and white glass panes
        createBorder();

        // Add availability buttons (1-10)
        addAvailabilityButtons();

        // Add close button
        createCloseButton();
    }

    private void createBorder() {
        ItemStack pinkPane = new ItemStack(Material.PINK_STAINED_GLASS_PANE);
        ItemStack whitePane = new ItemStack(Material.WHITE_STAINED_GLASS_PANE);

        // Row 1: PINK, WHITE, PINK, WHITE, [empty], WHITE, PINK, WHITE, PINK
        inventory.setItem(0, pinkPane);
        inventory.setItem(1, whitePane);
        inventory.setItem(2, pinkPane);
        inventory.setItem(3, whitePane);
        // Slot 4 empty
        inventory.setItem(5, whitePane);
        inventory.setItem(6, pinkPane);
        inventory.setItem(7, whitePane);
        inventory.setItem(8, pinkPane);

        // Side borders
        inventory.setItem(9, whitePane);
        inventory.setItem(17, whitePane);
        inventory.setItem(18, pinkPane);
        inventory.setItem(26, pinkPane);

        // Row 4: PINK, WHITE, PINK, WHITE, [CLOSE], WHITE, PINK, WHITE, PINK
        inventory.setItem(27, pinkPane);
        inventory.setItem(28, whitePane);
        inventory.setItem(29, pinkPane);
        inventory.setItem(30, whitePane);
        // Slot 31 is close
        inventory.setItem(32, whitePane);
        inventory.setItem(33, pinkPane);
        inventory.setItem(34, whitePane);
        inventory.setItem(35, pinkPane);

        // Empty center slots
        ItemStack blackPane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        inventory.setItem(4, blackPane);
        inventory.setItem(10, blackPane);
        inventory.setItem(16, blackPane);
        inventory.setItem(19, blackPane);
        inventory.setItem(25, blackPane);
    }

    private void addAvailabilityButtons() {
        // Row 2: Levels 1-5 (slots 11-15)
        for (int i = 1; i <= 5; i++) {
            createAvailabilityButton(i, 10 + i);
        }

        // Row 3: Levels 6-10 (slots 20-24)
        for (int i = 6; i <= 10; i++) {
            createAvailabilityButton(i, 14 + i);
        }
    }

    private void createAvailabilityButton(int level, int slot) {
        Material terracotta = TERRACOTTA_GRADIENT[level - 1];
        String hourRange = HOUR_RANGES[level - 1];
        String levelName = getLevelName(level);

        ItemStack item = VisualUtils.createServerItem(
            terracotta,
            levelName,
            Arrays.asList(
                hourRange,
                "",
                getCommitmentLevel(level),
                "",
                "Click to select"
            ),
            false
        );

        inventory.setItem(slot, item);
    }

    private String getLevelName(int level) {
        switch (level) {
            case 1: return "One";
            case 2: return "Two";
            case 3: return "Three";
            case 4: return "Four";
            case 5: return "Five";
            case 6: return "Six";
            case 7: return "Seven";
            case 8: return "Eight";
            case 9: return "Nine";
            case 10: return "Ten";
            default: return String.valueOf(level);
        }
    }

    private String getCommitmentLevel(int level) {
        if (level <= 2) {
            return "Casual player";
        } else if (level <= 4) {
            return "Regular player";
        } else if (level <= 6) {
            return "Active player";
        } else if (level <= 8) {
            return "Dedicated player";
        } else {
            return "Hardcore player";
        }
    }

    private void createCloseButton() {
        ItemStack item = VisualUtils.createServerItem(
            Material.BARRIER,
            "Close",
            Arrays.asList(
                "Go back without selecting"
            ),
            false
        );
        inventory.setItem(31, item);
    }

    public void open() {
        player.openInventory(inventory);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof AvailabilitySelectionGUI)) {
            return;
        }

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player clicker = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }

        int slot = event.getSlot();

        // Check availability level buttons (11-15 for 1-5, 20-24 for 6-10)
        if (slot >= 11 && slot <= 15) {
            int level = slot - 10;
            selectAvailability(clicker, level);
        } else if (slot >= 20 && slot <= 24) {
            int level = slot - 14;
            selectAvailability(clicker, level);
        } else if (slot == 31) {
            // Close button - return to parent
            parentGUI.open();
        }
    }

    private void selectAvailability(Player clicker, int level) {
        String hourRange = HOUR_RANGES[level - 1];

        // Update parent GUI with selected availability
        parentGUI.setAvailability(level);

        // Send confirmation message
        clicker.sendMessage(VisualUtils.createServerGradient("Availability set to " + hourRange));

        // Return to parent GUI
        parentGUI.open();
    }
}