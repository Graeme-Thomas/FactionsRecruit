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
 * Landing UI - Main menu for the Factions Recruitment system
 * Provides navigation to all major features
 */
public class LandingUI implements InventoryHolder, Listener {

    private final Inventory inventory;
    private final Player player;
    private final boolean isInFaction;
    private boolean isLookingForFaction;

    public LandingUI(Player player, boolean isInFaction, boolean isLookingForFaction) {
        this.player = player;
        this.isInFaction = isInFaction;
        this.isLookingForFaction = isLookingForFaction;
        this.inventory = Bukkit.createInventory(this, 54, VisualUtils.createCompactServerTitle("Recruitment"));
        initializeItems();
    }

    private void initializeItems() {
        // Create border with alternating pink and white glass panes
        createBorder();

        // Fill empty slots with black stained glass
        fillEmptySlots();

        // Create main menu items
        createFactionListingsButton();
        createPlayerListingsButton();
        createStatusButton();
        createToggleListingButton();
        createCreateListingButton();
        createCloseButton();
    }

    private void createBorder() {
        ItemStack pinkPane = new ItemStack(Material.PINK_STAINED_GLASS_PANE);
        ItemStack whitePane = new ItemStack(Material.WHITE_STAINED_GLASS_PANE);

        // Row 1: PINK, WHITE, PINK, WHITE, PINK, WHITE, PINK, WHITE, PINK
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
        inventory.setItem(9, whitePane);   // Row 2 left
        inventory.setItem(17, whitePane);  // Row 2 right
        inventory.setItem(18, pinkPane);   // Row 3 left
        inventory.setItem(26, pinkPane);   // Row 3 right
        inventory.setItem(27, whitePane);  // Row 4 left
        inventory.setItem(35, whitePane);  // Row 4 right
        inventory.setItem(36, pinkPane);   // Row 5 left
        inventory.setItem(44, pinkPane);   // Row 5 right

        // Row 6: WHITE, PINK, WHITE, PINK, [CLOSE], PINK, WHITE, PINK, WHITE
        inventory.setItem(45, whitePane);
        inventory.setItem(46, pinkPane);
        inventory.setItem(47, whitePane);
        inventory.setItem(48, pinkPane);
        // Slot 49 is close button
        inventory.setItem(50, pinkPane);
        inventory.setItem(51, whitePane);
        inventory.setItem(52, pinkPane);
        inventory.setItem(53, whitePane);
    }

    private void fillEmptySlots() {
        ItemStack blackPane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);

        // Row 2 (slots 10-16)
        for (int i = 10; i <= 16; i++) {
            inventory.setItem(i, blackPane);
        }

        // Row 3 (slots 19-20, 22, 24-25)
        inventory.setItem(19, blackPane);
        inventory.setItem(20, blackPane);
        inventory.setItem(22, blackPane);
        inventory.setItem(24, blackPane);
        inventory.setItem(25, blackPane);

        // Row 4 (slots 28, 30, 32, 34)
        inventory.setItem(28, blackPane);
        inventory.setItem(30, blackPane);
        inventory.setItem(32, blackPane);
        inventory.setItem(34, blackPane);

        // Row 5 (slots 37-43)
        for (int i = 37; i <= 43; i++) {
            inventory.setItem(i, blackPane);
        }
    }

    private void createFactionListingsButton() {
        // Slot 21: Player Head - Browse factions recruiting
        ItemStack item = VisualUtils.createServerItem(
            Material.PLAYER_HEAD,
            "Faction Listings",
            Arrays.asList(
                "Browse factions that are",
                "currently recruiting members",
                "",
                "Click to view all recruiting factions"
            ),
            false
        );
        inventory.setItem(21, item);
    }

    private void createPlayerListingsButton() {
        // Slot 23: Banner - Browse players looking for factions
        ItemStack item = VisualUtils.createServerItem(
            Material.WHITE_BANNER,
            "Player Listings",
            Arrays.asList(
                "Browse players who are",
                "looking for a faction",
                "",
                "Click to view all available players"
            ),
            false
        );
        inventory.setItem(23, item);
    }

    private void createStatusButton() {
        // Slot 29: Jukebox - View applications and invitations
        String title = isInFaction ? "Check Applications" : "View Status";
        String description1 = isInFaction ? "View incoming applications" : "View your applications";
        String description2 = isInFaction ? "and outgoing invitations" : "and incoming invitations";

        ItemStack item = VisualUtils.createServerItem(
            Material.JUKEBOX,
            title,
            Arrays.asList(
                description1,
                description2,
                "",
                "Click to manage applications"
            ),
            false
        );
        inventory.setItem(29, item);
    }

    private void createToggleListingButton() {
        // Slot 31: Spyglass (on) or Grey Dye (off)
        Material material = isLookingForFaction ? Material.SPYGLASS : Material.GRAY_DYE;
        String status = isLookingForFaction ? "Active" : "Inactive";

        ItemStack item = VisualUtils.createServerItem(
            material,
            "Toggle Listing",
            Arrays.asList(
                "Current Status: " + status,
                "",
                isLookingForFaction ? "You are visible to recruiters" : "You are hidden from recruiters",
                "",
                "Click to toggle your visibility"
            ),
            isLookingForFaction
        );
        inventory.setItem(31, item);
    }

    private void createCreateListingButton() {
        // Slot 33: Lectern - Create/edit your recruitment profile
        ItemStack item = VisualUtils.createServerItem(
            Material.LECTERN,
            "Create Listing",
            Arrays.asList(
                "Create or edit your",
                "recruitment profile",
                "",
                "Click to configure your profile"
            ),
            false
        );
        inventory.setItem(33, item);
    }

    private void createCloseButton() {
        // Slot 49: Barrier - Close the UI
        ItemStack item = VisualUtils.createServerItem(
            Material.BARRIER,
            "Close",
            Arrays.asList(
                "Click to close this menu"
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
        if (!(event.getInventory().getHolder() instanceof LandingUI)) {
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

        switch (slot) {
            case 21: // Faction Listings
                new FactionsRecruitingGUI(clicker, 0).open();
                break;

            case 23: // Player Listings
                new LookingForFactionsGUI(clicker, 0).open();
                break;

            case 29: // Status/Applications
                new PlayerApplicationsGUI(clicker, 0, isInFaction).open();
                break;

            case 31: // Toggle Listing
                toggleListing();
                break;

            case 33: // Create Listing
                new ApplicationSettingsGUI(clicker).open();
                break;

            case 49: // Close
                clicker.closeInventory();
                break;
        }
    }

    private void toggleListing() {
        // Toggle the listing status
        isLookingForFaction = !isLookingForFaction;

        // TODO: Update database with new status
        // DatabaseManager.setPlayerLooking(player.getUniqueId(), isLookingForFaction);

        // Refresh the button
        createToggleListingButton();

        // Send feedback to player
        String message = isLookingForFaction ?
            "Your listing is now active! Faction leaders can see your profile." :
            "Your listing is now inactive. You are hidden from recruiters.";
        player.sendMessage(VisualUtils.createServerGradient(message));
    }
}