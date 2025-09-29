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
 * Player Applications GUI - Manage incoming and outgoing applications/invitations
 * For faction leaders: View incoming applications and outgoing invitations
 * For players: View outgoing applications and incoming invitations
 * Supports pagination for multiple items
 */
public class PlayerApplicationsGUI implements InventoryHolder, Listener {

    private final Inventory inventory;
    private final Player player;
    private final int page;
    private final boolean isInFaction;
    private final List<ApplicationData> applications;
    private static final int ITEMS_PER_PAGE = 28; // 4 rows x 7 columns

    public PlayerApplicationsGUI(Player player, int page, boolean isInFaction) {
        this.player = player;
        this.page = page;
        this.isInFaction = isInFaction;
        this.inventory = Bukkit.createInventory(this, 54, VisualUtils.createCompactServerTitle("Applications"));

        // TODO: Load applications from database
        this.applications = loadApplications();

        initializeItems();
    }

    private List<ApplicationData> loadApplications() {
        // TODO: Query database for applications and invitations
        List<ApplicationData> data = new ArrayList<>();

        if (isInFaction) {
            // Load incoming applications and outgoing invitations
            // SELECT * FROM applications WHERE faction = ? AND state = 'Pending'
            // SELECT * FROM invites WHERE faction = ? AND state = 'Pending'
            data.add(new ApplicationData("Player1", "ExampleFaction", true, false, "Pending"));
            data.add(new ApplicationData("Player2", "ExampleFaction", true, false, "Pending"));
        } else {
            // Load outgoing applications and incoming invitations
            // SELECT * FROM applications WHERE player = ? AND state = 'Pending'
            // SELECT * FROM invites WHERE player = ? AND state = 'Pending'
            data.add(new ApplicationData("TestPlayer", "FactionA", false, false, "Pending"));
            data.add(new ApplicationData("TestPlayer", "FactionB", false, true, "Pending"));
        }

        return data;
    }

    private void initializeItems() {
        // Create border with alternating pink and white glass panes
        createBorder();

        // Fill empty slots with black stained glass
        fillEmptySlots();

        // Add application items
        addApplicationItems();

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

        // Slots for application items
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

    private void addApplicationItems() {
        int startIndex = page * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, applications.size());

        int[] contentSlots = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
        };

        for (int i = startIndex; i < endIndex; i++) {
            ApplicationData app = applications.get(i);
            int slotIndex = i - startIndex;

            if (slotIndex < contentSlots.length) {
                ItemStack item = createApplicationItem(app);
                inventory.setItem(contentSlots[slotIndex], item);
            }
        }
    }

    private ItemStack createApplicationItem(ApplicationData app) {
        // Use player head for applications, banner for invitations
        Material material = app.isInvitation ? Material.WHITE_BANNER : Material.PLAYER_HEAD;

        String title;
        List<String> lore = new ArrayList<>();

        if (isInFaction) {
            // Faction leader view
            if (app.isIncoming) {
                // Incoming application from player
                title = app.playerName;
                lore.add("Application to join your faction");
                lore.add("");
                lore.add("Status: " + app.status);
                lore.add("");
                lore.add("Left-click to accept");
                lore.add("Right-click to reject");
            } else {
                // Outgoing invitation to player
                title = app.playerName;
                lore.add("Invitation sent to player");
                lore.add("");
                lore.add("Status: " + app.status);
                lore.add("");
                lore.add("Click to cancel invitation");
            }
        } else {
            // Player view
            if (app.isIncoming) {
                // Incoming invitation from faction
                title = app.factionName;
                lore.add("Invitation from faction");
                lore.add("");
                lore.add("Status: " + app.status);
                lore.add("");
                lore.add("Left-click to accept");
                lore.add("Right-click to reject");
            } else {
                // Outgoing application to faction
                title = app.factionName;
                lore.add("Your application to join");
                lore.add("");
                lore.add("Status: " + app.status);
                lore.add("");
                lore.add("Click to cancel application");
            }
        }

        return VisualUtils.createServerItem(material, title, lore, false);
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
                    "Click to view previous items"
                ),
                false
            );
            inventory.setItem(45, prevButton);
        }

        // Next page button (slot 53)
        int totalPages = (int) Math.ceil((double) applications.size() / ITEMS_PER_PAGE);
        if (page < totalPages - 1) {
            ItemStack nextButton = VisualUtils.createServerItem(
                Material.ARROW,
                "Next Page",
                Arrays.asList(
                    "Go to page " + (page + 2),
                    "",
                    "Click to view more items"
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
        if (!(event.getInventory().getHolder() instanceof PlayerApplicationsGUI)) {
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
        boolean isLeftClick = event.isLeftClick();
        boolean isRightClick = event.isRightClick();

        // Check if clicked on an application item
        int[] contentSlots = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
        };

        for (int i = 0; i < contentSlots.length; i++) {
            if (slot == contentSlots[i]) {
                int appIndex = (page * ITEMS_PER_PAGE) + i;
                if (appIndex < applications.size()) {
                    handleApplicationClick(clicker, applications.get(appIndex), isLeftClick, isRightClick);
                    return;
                }
            }
        }

        // Navigation buttons
        switch (slot) {
            case 45: // Previous page
                if (page > 0) {
                    new PlayerApplicationsGUI(clicker, page - 1, isInFaction).open();
                }
                break;

            case 49: // Close
                new LandingUI(clicker, isInFaction, false).open();
                break;

            case 53: // Next page
                int totalPages = (int) Math.ceil((double) applications.size() / ITEMS_PER_PAGE);
                if (page < totalPages - 1) {
                    new PlayerApplicationsGUI(clicker, page + 1, isInFaction).open();
                }
                break;
        }
    }

    private void handleApplicationClick(Player clicker, ApplicationData app, boolean isLeftClick, boolean isRightClick) {
        if (app.isIncoming) {
            // Accept or reject incoming item
            if (isLeftClick) {
                // Accept
                clicker.sendMessage(VisualUtils.createServerGradient("Accepted!"));
                // TODO: Update database - set status to 'Accepted'
                // TODO: Add player to faction or accept invitation
            } else if (isRightClick) {
                // Reject
                clicker.sendMessage(VisualUtils.createServerGradient("Rejected!"));
                // TODO: Update database - set status to 'Rejected'
            }
        } else {
            // Cancel outgoing item
            clicker.sendMessage(VisualUtils.createServerGradient("Cancelled!"));
            // TODO: Update database - set status to 'Expired' or delete
        }

        // Refresh the UI
        new PlayerApplicationsGUI(clicker, page, isInFaction).open();
    }

    /**
     * Internal data class for application/invitation information
     */
    private static class ApplicationData {
        String playerName;
        String factionName;
        boolean isIncoming; // True if incoming to the viewer
        boolean isInvitation; // True if invitation, false if application
        String status;

        ApplicationData(String playerName, String factionName, boolean isIncoming, boolean isInvitation, String status) {
            this.playerName = playerName;
            this.factionName = factionName;
            this.isIncoming = isIncoming;
            this.isInvitation = isInvitation;
            this.status = status;
        }
    }
}