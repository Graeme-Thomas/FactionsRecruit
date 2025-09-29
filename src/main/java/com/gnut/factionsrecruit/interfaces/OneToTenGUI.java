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
 * One to Ten GUI - Reusable skill selector with 1-10 rating scale
 * Uses terracotta gradient from red to green for visual appeal
 */
public class OneToTenGUI implements InventoryHolder, Listener {

    private final Inventory inventory;
    private final Player player;
    private final ApplicationSettingsGUI parentGUI;
    private final String skillName;
    private final ApplicationSettingsGUI.SkillType skillType;

    // Terracotta gradient from red to green
    private static final Material[] TERRACOTTA_GRADIENT = {
        Material.RED_TERRACOTTA,          // 1 - Red
        Material.ORANGE_TERRACOTTA,       // 2 - Orange
        Material.YELLOW_TERRACOTTA,       // 3 - Yellow
        Material.LIME_TERRACOTTA,         // 4 - Lime
        Material.LIGHT_BLUE_TERRACOTTA,   // 5 - Light Blue
        Material.CYAN_TERRACOTTA,         // 6 - Cyan
        Material.LIME_TERRACOTTA,         // 7 - Lime (repeat)
        Material.GREEN_TERRACOTTA,        // 8 - Green
        Material.GREEN_TERRACOTTA,        // 9 - Green
        Material.GREEN_TERRACOTTA         // 10 - Green
    };

    public OneToTenGUI(Player player, ApplicationSettingsGUI parentGUI, String skillName, ApplicationSettingsGUI.SkillType skillType) {
        this.player = player;
        this.parentGUI = parentGUI;
        this.skillName = skillName;
        this.skillType = skillType;
        this.inventory = Bukkit.createInventory(this, 36, VisualUtils.createCompactServerTitle(skillName + " Experience"));
        initializeItems();
    }

    private void initializeItems() {
        // Create border with alternating pink and white glass panes
        createBorder();

        // Add skill level buttons (1-10)
        addSkillButtons();

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

    private void addSkillButtons() {
        // Row 2: Levels 1-5 (slots 11-15)
        for (int i = 1; i <= 5; i++) {
            createSkillButton(i, 10 + i);
        }

        // Row 3: Levels 6-10 (slots 20-24)
        for (int i = 6; i <= 10; i++) {
            createSkillButton(i, 14 + i);
        }
    }

    private void createSkillButton(int level, int slot) {
        Material terracotta = TERRACOTTA_GRADIENT[level - 1];

        String levelName = getLevelName(level);
        String description = getLevelDescription(level);

        ItemStack item = VisualUtils.createServerItem(
            terracotta,
            levelName,
            Arrays.asList(
                "Skill Level: " + level + "/10",
                "",
                description,
                "",
                "Click to select this level"
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

    private String getLevelDescription(int level) {
        if (level <= 2) {
            return "Beginner - Just starting out";
        } else if (level <= 4) {
            return "Novice - Learning the basics";
        } else if (level <= 6) {
            return "Intermediate - Comfortable";
        } else if (level <= 8) {
            return "Advanced - Very skilled";
        } else {
            return "Expert - Mastery level";
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
        if (!(event.getInventory().getHolder() instanceof OneToTenGUI)) {
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

        // Check skill level buttons (11-15 for 1-5, 20-24 for 6-10)
        if (slot >= 11 && slot <= 15) {
            int level = slot - 10;
            selectLevel(clicker, level);
        } else if (slot >= 20 && slot <= 24) {
            int level = slot - 14;
            selectLevel(clicker, level);
        } else if (slot == 31) {
            // Close button - return to parent
            parentGUI.open();
        }
    }

    private void selectLevel(Player clicker, int level) {
        // Update parent GUI with selected skill level
        parentGUI.setSkill(skillType, level);

        // Send confirmation message
        clicker.sendMessage(VisualUtils.createServerGradient(skillName + " skill set to " + level + "/10"));

        // Return to parent GUI
        parentGUI.open();
    }
}