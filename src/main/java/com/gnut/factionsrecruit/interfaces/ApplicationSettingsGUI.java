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
 * Application Settings GUI - Configure player recruitment profile
 * Allows players to set timezone, Discord, skills, availability, and previous factions
 */
public class ApplicationSettingsGUI implements InventoryHolder, Listener {

    private final Inventory inventory;
    private final Player player;

    // Profile data
    private String timezone;
    private String discord;
    private int factionsSkill;
    private int raidingSkill;
    private int buildingSkill;
    private int pvpSkill;
    private int availability;
    private List<String> previousFactions;

    public ApplicationSettingsGUI(Player player) {
        this.player = player;
        this.inventory = Bukkit.createInventory(this, 45, VisualUtils.createCompactServerTitle("Profile Settings"));

        // TODO: Load existing profile from database
        loadProfileData();

        initializeItems();
    }

    private void loadProfileData() {
        // TODO: Query database for player's existing profile
        // SELECT * FROM player_resume WHERE player = ?
        this.timezone = "Not Set";
        this.discord = "Not Set";
        this.factionsSkill = 0;
        this.raidingSkill = 0;
        this.buildingSkill = 0;
        this.pvpSkill = 0;
        this.availability = 0;
        this.previousFactions = new ArrayList<>();
    }

    private void initializeItems() {
        // Create border with alternating pink and white glass panes
        createBorder();

        // Fill empty slots with black stained glass
        fillEmptySlots();

        // Add profile configuration buttons
        createReadyIndicator();
        createTimezoneButton();
        createDiscordButton();
        createFactionsButton();
        createRaidingButton();
        createBuildingButton();
        createPvPButton();
        createAvailabilityButton();
        createPreviousFactionsButton();
        createClearAllButton();
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
        inventory.setItem(9, whitePane);
        inventory.setItem(17, whitePane);
        inventory.setItem(18, pinkPane);
        inventory.setItem(26, pinkPane);
        inventory.setItem(27, pinkPane);
        inventory.setItem(35, pinkPane);

        // Row 5: WHITE, PINK, WHITE, PINK, [CLOSE], PINK, WHITE, PINK, WHITE
        inventory.setItem(36, whitePane);
        inventory.setItem(37, pinkPane);
        inventory.setItem(38, whitePane);
        inventory.setItem(39, pinkPane);
        // Slot 40 is close
        inventory.setItem(41, pinkPane);
        inventory.setItem(42, whitePane);
        inventory.setItem(43, pinkPane);
        inventory.setItem(44, whitePane);
    }

    private void fillEmptySlots() {
        ItemStack blackPane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);

        // Empty slots in content area
        int[] emptySlots = {10, 12, 14, 16, 19, 20, 22, 24, 25, 28, 30, 32, 34};

        for (int slot : emptySlots) {
            inventory.setItem(slot, blackPane);
        }
    }

    private void createReadyIndicator() {
        // Slot 4: Ready indicator (Lever or Redstone Torch)
        boolean isReady = isProfileComplete();
        Material material = isReady ? Material.REDSTONE_TORCH : Material.LEVER;

        String status = isReady ? "Ready to Submit" : "Incomplete Profile";

        ItemStack item = VisualUtils.createServerItem(
            material,
            status,
            Arrays.asList(
                isReady ? "Your profile is complete!" : "Fill in all fields to activate",
                "",
                "Required fields:",
                (timezone.equals("Not Set") ? "" : "") + " Timezone",
                (factionsSkill == 0 ? "" : "") + " Factions Experience",
                (raidingSkill == 0 ? "" : "") + " Raiding Experience",
                (buildingSkill == 0 ? "" : "") + " Building Experience",
                (pvpSkill == 0 ? "" : "") + " PvP Experience",
                (availability == 0 ? "" : "") + " Availability"
            ),
            isReady
        );
        inventory.setItem(4, item);
    }

    private boolean isProfileComplete() {
        return !timezone.equals("Not Set") &&
               factionsSkill > 0 &&
               raidingSkill > 0 &&
               buildingSkill > 0 &&
               pvpSkill > 0 &&
               availability > 0;
    }

    private void createTimezoneButton() {
        // Slot 11: Clock - Timezone selector
        ItemStack item = VisualUtils.createServerItem(
            Material.CLOCK,
            "Timezone",
            Arrays.asList(
                "Current: " + timezone,
                "",
                "Click to select your timezone"
            ),
            !timezone.equals("Not Set")
        );
        inventory.setItem(11, item);
    }

    private void createDiscordButton() {
        // Slot 13: Anvil - Discord input
        ItemStack item = VisualUtils.createServerItem(
            Material.ANVIL,
            "Discord Username",
            Arrays.asList(
                "Current: " + discord,
                "",
                "Click to set your Discord username",
                "(Will open chat input)"
            ),
            !discord.equals("Not Set")
        );
        inventory.setItem(13, item);
    }

    private void createFactionsButton() {
        // Slot 15: Factions skill selector
        String skillLevel = factionsSkill == 0 ? "Not Set" : factionsSkill + "/10";
        ItemStack item = VisualUtils.createServerItem(
            Material.GRASS_BLOCK,
            "Factions Experience",
            Arrays.asList(
                "Current Level: " + skillLevel,
                "",
                "How experienced are you with",
                "the Factions plugin?",
                "",
                "Click to rate your skill (1-10)"
            ),
            factionsSkill > 0
        );
        inventory.setItem(15, item);
    }

    private void createRaidingButton() {
        // Slot 21: Raiding skill selector
        String skillLevel = raidingSkill == 0 ? "Not Set" : raidingSkill + "/10";
        ItemStack item = VisualUtils.createServerItem(
            Material.TNT,
            "Raiding Experience",
            Arrays.asList(
                "Current Level: " + skillLevel,
                "",
                "How skilled are you at raiding",
                "enemy bases?",
                "",
                "Click to rate your skill (1-10)"
            ),
            raidingSkill > 0
        );
        inventory.setItem(21, item);
    }

    private void createBuildingButton() {
        // Slot 23: Building skill selector
        String skillLevel = buildingSkill == 0 ? "Not Set" : buildingSkill + "/10";
        ItemStack item = VisualUtils.createServerItem(
            Material.BRICKS,
            "Building Experience",
            Arrays.asList(
                "Current Level: " + skillLevel,
                "",
                "How skilled are you at building",
                "bases and structures?",
                "",
                "Click to rate your skill (1-10)"
            ),
            buildingSkill > 0
        );
        inventory.setItem(23, item);
    }

    private void createPvPButton() {
        // Slot 29: PvP skill selector
        String skillLevel = pvpSkill == 0 ? "Not Set" : pvpSkill + "/10";
        ItemStack item = VisualUtils.createServerItem(
            Material.DIAMOND_SWORD,
            "PvP Experience",
            Arrays.asList(
                "Current Level: " + skillLevel,
                "",
                "How skilled are you at",
                "player combat?",
                "",
                "Click to rate your skill (1-10)"
            ),
            pvpSkill > 0
        );
        inventory.setItem(29, item);
    }

    private void createAvailabilityButton() {
        // Slot 31: Availability selector
        String availText = availability == 0 ? "Not Set" : getAvailabilityText(availability);
        ItemStack item = VisualUtils.createServerItem(
            Material.FILLED_MAP,
            "Availability",
            Arrays.asList(
                "Current: " + availText,
                "",
                "How many hours per week",
                "can you play?",
                "",
                "Click to select availability"
            ),
            availability > 0
        );
        inventory.setItem(31, item);
    }

    private String getAvailabilityText(int level) {
        switch (level) {
            case 1: return "Less than 10 hours";
            case 2: return "10-20 hours";
            case 3: return "20-30 hours";
            case 4: return "30-40 hours";
            case 5: return "40-50 hours";
            case 6: return "50-60 hours";
            case 7: return "60-70 hours";
            case 8: return "70-80 hours";
            case 9: return "80-90 hours";
            case 10: return "More than 100 hours";
            default: return "Not Set";
        }
    }

    private void createPreviousFactionsButton() {
        // Slot 33: Previous factions input
        String factionsText = previousFactions.isEmpty() ? "None" : String.join(", ", previousFactions);
        ItemStack item = VisualUtils.createServerItem(
            Material.BOOK,
            "Previous Factions",
            Arrays.asList(
                "Current: " + factionsText,
                "",
                "Left-click to add a faction",
                "Right-click to clear list",
                "",
                "Maximum 10 factions"
            ),
            !previousFactions.isEmpty()
        );
        inventory.setItem(33, item);
    }

    private void createClearAllButton() {
        // Slot 44: Clear all selections
        ItemStack item = VisualUtils.createServerItem(
            Material.GRAY_DYE,
            "Clear All",
            Arrays.asList(
                "Reset all profile settings",
                "",
                "Click to clear everything"
            ),
            false
        );
        inventory.setItem(44, item);
    }

    private void createCloseButton() {
        // Slot 40: Close button
        ItemStack item = VisualUtils.createServerItem(
            Material.BARRIER,
            "Close",
            Arrays.asList(
                "Return to main menu",
                "",
                isProfileComplete() ? "Your profile will be saved" : "Profile incomplete - changes saved"
            ),
            false
        );
        inventory.setItem(40, item);
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
        if (!(event.getInventory().getHolder() instanceof ApplicationSettingsGUI)) {
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
        boolean isRightClick = event.isRightClick();

        switch (slot) {
            case 11: // Timezone
                new TimeZoneSelectionGUI(clicker, this).open();
                break;

            case 13: // Discord
                clicker.closeInventory();
                clicker.sendMessage(VisualUtils.createServerGradient("Type your Discord username in chat:"));
                // TODO: Implement chat listener for Discord input
                break;

            case 15: // Factions skill
                new OneToTenGUI(clicker, this, "Factions", SkillType.FACTIONS).open();
                break;

            case 21: // Raiding skill
                new OneToTenGUI(clicker, this, "Raiding", SkillType.RAIDING).open();
                break;

            case 23: // Building skill
                new OneToTenGUI(clicker, this, "Building", SkillType.BUILDING).open();
                break;

            case 29: // PvP skill
                new OneToTenGUI(clicker, this, "PvP", SkillType.PVP).open();
                break;

            case 31: // Availability
                new AvailabilitySelectionGUI(clicker, this).open();
                break;

            case 33: // Previous factions
                if (isRightClick) {
                    // Clear list
                    previousFactions.clear();
                    player.sendMessage(VisualUtils.createServerGradient("Previous factions list cleared"));
                    refresh();
                } else {
                    // Add faction
                    clicker.closeInventory();
                    clicker.sendMessage(VisualUtils.createServerGradient("Type faction name to add (max 20 characters):"));
                    // TODO: Implement chat listener for faction input
                }
                break;

            case 40: // Close
                saveProfile();
                new LandingUI(clicker, false, false).open();
                break;

            case 44: // Clear all
                clearAll();
                refresh();
                player.sendMessage(VisualUtils.createServerGradient("Profile cleared"));
                break;
        }
    }

    private void clearAll() {
        this.timezone = "Not Set";
        this.discord = "Not Set";
        this.factionsSkill = 0;
        this.raidingSkill = 0;
        this.buildingSkill = 0;
        this.pvpSkill = 0;
        this.availability = 0;
        this.previousFactions.clear();
    }

    private void saveProfile() {
        // TODO: Save profile to database
        // UPDATE player_resume SET timezone=?, discord=?, factions_experience=?, ...
        player.sendMessage(VisualUtils.createServerGradient("Profile saved!"));
    }

    public void refresh() {
        // Refresh all items to show updated values
        initializeItems();
    }

    // Setters for callback from selection GUIs
    public void setTimezone(String timezone) {
        this.timezone = timezone;
        refresh();
    }

    public void setSkill(SkillType type, int value) {
        switch (type) {
            case FACTIONS:
                this.factionsSkill = value;
                break;
            case RAIDING:
                this.raidingSkill = value;
                break;
            case BUILDING:
                this.buildingSkill = value;
                break;
            case PVP:
                this.pvpSkill = value;
                break;
        }
        refresh();
    }

    public void setAvailability(int availability) {
        this.availability = availability;
        refresh();
    }

    /**
     * Enum for skill types
     */
    public enum SkillType {
        FACTIONS, RAIDING, BUILDING, PVP
    }
}