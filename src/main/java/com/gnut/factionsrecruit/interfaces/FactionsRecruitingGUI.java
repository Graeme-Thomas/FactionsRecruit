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
import java.util.ArrayList;
import java.util.List;

/**
 * Factions Recruiting GUI - Browse factions that are currently recruiting
 * Supports pagination for viewing multiple factions
 */
public class FactionsRecruitingGUI implements InventoryHolder, Listener {

    private final Inventory inventory;
    private final Player player;
    private final int page;
    private final List<FactionData> factions;
    private static final int ITEMS_PER_PAGE = 28; // 4 rows x 7 columns

    public FactionsRecruitingGUI(Player player, int page) {
        this.player = player;
        this.page = page;
        this.inventory = Bukkit.createInventory(this, 54, VisualUtils.createCompactServerTitle("Factions Recruiting"));

        // TODO: Load factions from database
        this.factions = loadRecruitingFactions();

        initializeItems();
    }

    private List<FactionData> loadRecruitingFactions() {
        // TODO: Query database for recruiting factions
        // SELECT * FROM factions_applications WHERE is_recruiting = true
        // For now, return mock data for demonstration
        List<FactionData> mockFactions = new ArrayList<>();

        // This would be replaced with actual database queries
        mockFactions.add(new FactionData("ExampleFaction1", "Leader1", 15, 30));
        mockFactions.add(new FactionData("ExampleFaction2", "Leader2", 8, 20));

        return mockFactions;
    }

    private void initializeItems() {
        // Create border with alternating pink and white glass panes
        createBorder();

        // Fill empty slots with black stained glass
        fillEmptySlots();

        // Add faction banners
        addFactionBanners();

        // Add navigation buttons
        addNavigationButtons();

        // Add close button
        createCloseButton();
    }

    private void createBorder() {
        ItemStack pinkPane = new ItemStack(Material.PINK_STAINED_GLASS_PANE);
        ItemStack whitePane = new ItemStack(Material.WHITE_STAINED_GLASS_PANE);

        // Row 1
        inventory.setItem(0, pinkPane);
        inventory.setItem(1, whitePane);
        inventory.setItem(2, pinkPane);
        inventory.setItem(3, whitePane);
        inventory.setItem(4, pinkPane);
        inventory.setItem(5, whitePane);
        inventory.setItem(6, pinkPane);
        inventory.setItem(7, whitePane);
        inventory.setItem(8, pinkPane);

        // Side borders
        inventory.setItem(9, whitePane);
        inventory.setItem(17, whitePane);
        inventory.setItem(18, pinkPane);
        inventory.setItem(26, pinkPane);
        inventory.setItem(27, whitePane);
        inventory.setItem(35, whitePane);
        inventory.setItem(36, pinkPane);
        inventory.setItem(44, pinkPane);

        // Row 6
        inventory.setItem(45, whitePane);
        inventory.setItem(46, pinkPane);
        inventory.setItem(47, whitePane);
        inventory.setItem(48, pinkPane);
        inventory.setItem(50, pinkPane);
        inventory.setItem(51, whitePane);
        inventory.setItem(52, pinkPane);
        inventory.setItem(53, whitePane);
    }

    private void fillEmptySlots() {
        ItemStack blackPane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);

        // Slots for faction items (will be overwritten if faction exists)
        int[] contentSlots = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
        };

        for (int slot : contentSlots) {
            inventory.setItem(slot, blackPane);
        }
    }

    private void addFactionBanners() {
        int startIndex = page * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, factions.size());

        int[] contentSlots = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
        };

        for (int i = startIndex; i < endIndex; i++) {
            FactionData faction = factions.get(i);
            int slotIndex = i - startIndex;

            if (slotIndex < contentSlots.length) {
                ItemStack factionBanner = createFactionBanner(faction);
                inventory.setItem(contentSlots[slotIndex], factionBanner);
            }
        }
    }

    private ItemStack createFactionBanner(FactionData faction) {
        // TODO: Use faction's custom banner if available
        // For now, use yellow banner as default
        return VisualUtils.createServerItem(
            Material.YELLOW_BANNER,
            faction.name,
            Arrays.asList(
                VisualUtils.Symbols.CROWN + " Leader: " + faction.leader,
                VisualUtils.Symbols.SHIELD + " Members: " + faction.currentMembers + "/" + faction.maxMembers,
                "",
                "Recruiting: " + (faction.maxMembers - faction.currentMembers) + " slots open",
                "",
                "Click to view faction details",
                "and send an application"
            ),
            true
        );
    }

    private void addNavigationButtons() {
        // Previous page button (slot 45)
        if (page > 0) {
            ItemStack prevButton = VisualUtils.createServerItem(
                Material.ARROW,
                "Previous Page",
                Arrays.asList(
                    "Go to page " + page,
                    "",
                    "Click to view previous factions"
                ),
                false
            );
            inventory.setItem(45, prevButton);
        }

        // Next page button (slot 53)
        int totalPages = (int) Math.ceil((double) factions.size() / ITEMS_PER_PAGE);
        if (page < totalPages - 1) {
            ItemStack nextButton = VisualUtils.createServerItem(
                Material.ARROW,
                "Next Page",
                Arrays.asList(
                    "Go to page " + (page + 2),
                    "",
                    "Click to view more factions"
                ),
                false
            );
            inventory.setItem(53, nextButton);
        }
    }

    private void createCloseButton() {
        ItemStack item = VisualUtils.createServerItem(
            Material.BARRIER,
            "Close",
            Arrays.asList(
                "Return to main menu"
            ),
            false
        );
        inventory.setItem(49, item);
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
        if (!(event.getInventory().getHolder() instanceof FactionsRecruitingGUI)) {
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

        // Check if clicked on a faction banner
        int[] contentSlots = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
        };

        for (int i = 0; i < contentSlots.length; i++) {
            if (slot == contentSlots[i]) {
                int factionIndex = (page * ITEMS_PER_PAGE) + i;
                if (factionIndex < factions.size()) {
                    handleFactionClick(clicker, factions.get(factionIndex));
                    return;
                }
            }
        }

        // Navigation buttons
        switch (slot) {
            case 45: // Previous page
                if (page > 0) {
                    new FactionsRecruitingGUI(clicker, page - 1).open();
                }
                break;

            case 49: // Close
                // TODO: Determine if player was in faction context or not
                new LandingUI(clicker, false, false).open();
                break;

            case 53: // Next page
                int totalPages = (int) Math.ceil((double) factions.size() / ITEMS_PER_PAGE);
                if (page < totalPages - 1) {
                    new FactionsRecruitingGUI(clicker, page + 1).open();
                }
                break;
        }
    }

    private void handleFactionClick(Player player, FactionData faction) {
        // TODO: Open faction details screen or send application
        // For now, send a message
        player.sendMessage(VisualUtils.createServerGradient("You clicked on " + faction.name));
        player.sendMessage(VisualUtils.createServerGradient("Application system coming soon!"));

        // TODO: Implement application creation
        // - Check if player already applied
        // - Create new application in database
        // - Send confirmation message
    }

    /**
     * Internal data class for faction information
     */
    private static class FactionData {
        String name;
        String leader;
        int currentMembers;
        int maxMembers;

        FactionData(String name, String leader, int currentMembers, int maxMembers) {
            this.name = name;
            this.leader = leader;
            this.currentMembers = currentMembers;
            this.maxMembers = maxMembers;
        }
    }
}