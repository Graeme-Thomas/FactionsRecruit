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
 * Timezone Selection GUI - Choose player's timezone region
 * Offers 6 main timezone options for matchmaking
 */
public class TimeZoneSelectionGUI implements InventoryHolder, Listener {

    private final Inventory inventory;
    private final Player player;
    private final ApplicationSettingsGUI parentGUI;

    // Timezone options
    private static final String[] TIMEZONES = {
        "NA-WEST",      // North America West
        "NA-EAST",      // North America East
        "EU-WEST",      // Europe West
        "EU-CENTRAL",   // Europe Central
        "ASIA",         // Asia
        "OCEANIA"       // Oceania/Australia
    };

    private static final String[] TIMEZONE_DESCRIPTIONS = {
        "PST/PDT - California, Oregon, Washington",
        "EST/EDT - New York, Florida, Ontario",
        "GMT/BST - UK, Ireland, Portugal",
        "CET/CEST - Germany, France, Italy",
        "JST/CST - Japan, China, Korea",
        "AEST/AEDT - Australia, New Zealand"
    };

    public TimeZoneSelectionGUI(Player player, ApplicationSettingsGUI parentGUI) {
        this.player = player;
        this.parentGUI = parentGUI;
        this.inventory = Bukkit.createInventory(this, 36, VisualUtils.createCompactServerTitle("Timezone"));
        initializeItems();
    }

    private void initializeItems() {
        // Create border with alternating pink and white glass panes
        createBorder();

        // Add timezone buttons
        addTimezoneButtons();

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
        inventory.setItem(12, blackPane);
        inventory.setItem(14, blackPane);
        inventory.setItem(16, blackPane);
        inventory.setItem(19, blackPane);
        inventory.setItem(21, blackPane);
        inventory.setItem(23, blackPane);
        inventory.setItem(25, blackPane);
    }

    private void addTimezoneButtons() {
        // Row 2: NA-WEST (11), NA-EAST (13), EU-WEST (15)
        createTimezoneButton(0, 11);  // NA-WEST
        createTimezoneButton(1, 13);  // NA-EAST
        createTimezoneButton(2, 15);  // EU-WEST

        // Row 3: EU-CENTRAL (20), ASIA (22), OCEANIA (24)
        createTimezoneButton(3, 20);  // EU-CENTRAL
        createTimezoneButton(4, 22);  // ASIA
        createTimezoneButton(5, 24);  // OCEANIA
    }

    private void createTimezoneButton(int index, int slot) {
        String timezone = TIMEZONES[index];
        String description = TIMEZONE_DESCRIPTIONS[index];

        // Use different clock-related materials for variety
        Material material = Material.CLOCK;

        ItemStack item = VisualUtils.createServerItem(
            material,
            timezone,
            Arrays.asList(
                description,
                "",
                "Click to select this timezone"
            ),
            false
        );

        inventory.setItem(slot, item);
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
        if (!(event.getInventory().getHolder() instanceof TimeZoneSelectionGUI)) {
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

        // Map slots to timezone indices
        switch (slot) {
            case 11: // NA-WEST
                selectTimezone(clicker, 0);
                break;
            case 13: // NA-EAST
                selectTimezone(clicker, 1);
                break;
            case 15: // EU-WEST
                selectTimezone(clicker, 2);
                break;
            case 20: // EU-CENTRAL
                selectTimezone(clicker, 3);
                break;
            case 22: // ASIA
                selectTimezone(clicker, 4);
                break;
            case 24: // OCEANIA
                selectTimezone(clicker, 5);
                break;
            case 31: // Close
                parentGUI.open();
                break;
        }
    }

    private void selectTimezone(Player clicker, int index) {
        String timezone = TIMEZONES[index];

        // Update parent GUI with selected timezone
        parentGUI.setTimezone(timezone);

        // Send confirmation message
        clicker.sendMessage(VisualUtils.createServerGradient("Timezone set to " + timezone));

        // Return to parent GUI
        parentGUI.open();
    }
}