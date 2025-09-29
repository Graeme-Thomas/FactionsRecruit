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
 * Looking For Factions GUI - Browse players who are seeking factions
 * Faction leaders can view player profiles and send invitations
 * Supports pagination for viewing multiple players
 */
public class LookingForFactionsGUI implements InventoryHolder, Listener {

    private final Inventory inventory;
    private final Player player;
    private final int page;
    private final List<PlayerData> players;
    private final boolean isFactionLeader;
    private static final int ITEMS_PER_PAGE = 28; // 4 rows x 7 columns

    public LookingForFactionsGUI(Player player, int page) {
        this.player = player;
        this.page = page;
        this.inventory = Bukkit.createInventory(this, 54, VisualUtils.createCompactServerTitle("Looking for a Faction"));

        // TODO: Check if player is faction leader
        this.isFactionLeader = checkIfFactionLeader(player);

        // TODO: Load players from database
        this.players = loadLookingPlayers();

        initializeItems();
    }

    private boolean checkIfFactionLeader(Player player) {
        // TODO: Check if player is faction leader through Factions API
        // For now, return true for demonstration
        return true;
    }

    private List<PlayerData> loadLookingPlayers() {
        // TODO: Query database for players looking for factions
        // SELECT * FROM player_resume WHERE islooking = true
        // Exclude self from the list
        List<PlayerData> mockPlayers = new ArrayList<>();

        // This would be replaced with actual database queries
        mockPlayers.add(new PlayerData("Player1", "NA-WEST", 8, 7, 6, 9));
        mockPlayers.add(new PlayerData("Player2", "EU-WEST", 5, 6, 8, 7));
        mockPlayers.add(new PlayerData("Player3", "ASIA", 9, 8, 5, 8));

        return mockPlayers;
    }

    private void initializeItems() {
        // Create border with alternating pink and white glass panes
        createBorder();

        // Fill empty slots with black stained glass
        fillEmptySlots();

        // Add player heads
        addPlayerHeads();

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

        // Slots for player items (will be overwritten if player exists)
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

    private void addPlayerHeads() {
        int startIndex = page * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, players.size());

        int[] contentSlots = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
        };

        for (int i = startIndex; i < endIndex; i++) {
            PlayerData playerData = players.get(i);
            int slotIndex = i - startIndex;

            // Don't show the viewing player in the list
            if (playerData.name.equals(player.getName())) {
                continue;
            }

            if (slotIndex < contentSlots.length) {
                ItemStack playerHead = createPlayerHead(playerData);
                inventory.setItem(contentSlots[slotIndex], playerHead);
            }
        }
    }

    private ItemStack createPlayerHead(PlayerData playerData) {
        List<String> lore = new ArrayList<>(Arrays.asList(
            VisualUtils.Symbols.DIAMOND + " Timezone: " + playerData.timezone,
            "",
            "Skill Levels:",
            "  Factions: " + getSkillBar(playerData.factionsSkill),
            "  Raiding: " + getSkillBar(playerData.raidingSkill),
            "  Building: " + getSkillBar(playerData.buildingSkill),
            "  PvP: " + getSkillBar(playerData.pvpSkill),
            ""
        ));

        // Add action text for faction leaders
        if (isFactionLeader) {
            lore.add("Click to send an invitation");
        } else {
            lore.add("Click to view full profile");
        }

        return VisualUtils.createServerItem(
            Material.PLAYER_HEAD,
            playerData.name,
            lore,
            false
        );
    }

    private String getSkillBar(int skill) {
        // Create a visual skill bar (1-10 scale)
        StringBuilder bar = new StringBuilder();
        for (int i = 1; i <= 10; i++) {
            if (i <= skill) {
                bar.append("\u25AE"); // Filled bar
            } else {
                bar.append("\u25AF"); // Empty bar
            }
        }
        return bar.toString() + " (" + skill + "/10)";
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
                    "Click to view previous players"
                ),
                false
            );
            inventory.setItem(45, prevButton);
        }

        // Next page button (slot 53)
        int totalPages = (int) Math.ceil((double) players.size() / ITEMS_PER_PAGE);
        if (page < totalPages - 1) {
            ItemStack nextButton = VisualUtils.createServerItem(
                Material.ARROW,
                "Next Page",
                Arrays.asList(
                    "Go to page " + (page + 2),
                    "",
                    "Click to view more players"
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
        if (!(event.getInventory().getHolder() instanceof LookingForFactionsGUI)) {
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

        // Check if clicked on a player head
        int[] contentSlots = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
        };

        for (int i = 0; i < contentSlots.length; i++) {
            if (slot == contentSlots[i]) {
                int playerIndex = (page * ITEMS_PER_PAGE) + i;
                if (playerIndex < players.size()) {
                    handlePlayerClick(clicker, players.get(playerIndex));
                    return;
                }
            }
        }

        // Navigation buttons
        switch (slot) {
            case 45: // Previous page
                if (page > 0) {
                    new LookingForFactionsGUI(clicker, page - 1).open();
                }
                break;

            case 49: // Close
                new LandingUI(clicker, false, false).open();
                break;

            case 53: // Next page
                int totalPages = (int) Math.ceil((double) players.size() / ITEMS_PER_PAGE);
                if (page < totalPages - 1) {
                    new LookingForFactionsGUI(clicker, page + 1).open();
                }
                break;
        }
    }

    private void handlePlayerClick(Player clicker, PlayerData targetPlayer) {
        if (isFactionLeader && !targetPlayer.name.equals(clicker.getName())) {
            // Send invitation
            clicker.sendMessage(VisualUtils.createServerGradient("Sending invitation to " + targetPlayer.name));
            clicker.sendMessage(VisualUtils.createServerGradient("Invitation system coming soon!"));

            // TODO: Implement invitation creation
            // - Check if invitation already sent
            // - Create new invitation in database
            // - Notify target player
            // - Send confirmation message
        } else {
            // View profile details
            clicker.sendMessage(VisualUtils.createServerGradient("Viewing profile of " + targetPlayer.name));
            // TODO: Open detailed profile view
        }
    }

    /**
     * Internal data class for player information
     */
    private static class PlayerData {
        String name;
        String timezone;
        int factionsSkill;
        int raidingSkill;
        int buildingSkill;
        int pvpSkill;

        PlayerData(String name, String timezone, int factionsSkill, int raidingSkill, int buildingSkill, int pvpSkill) {
            this.name = name;
            this.timezone = timezone;
            this.factionsSkill = factionsSkill;
            this.raidingSkill = raidingSkill;
            this.buildingSkill = buildingSkill;
            this.pvpSkill = pvpSkill;
        }
    }
}