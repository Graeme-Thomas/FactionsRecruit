package com.gnut.factionsrecruit.interfaces;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.gnut.factionsrecruit.manager.ConfigManager;
import com.gnut.factionsrecruit.util.VisualUtils;

import java.util.*;
import java.util.function.Function;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class SelectionEditorTemplate implements InventoryHolder, Listener {

    private final Inventory inventory;
    private final SelectionEditorConfig config;
    private final Player player; // The player currently viewing this specific instance of the GUI
    private final ConfigManager configManager;

    // Stores the currently selected options for this player
    private final Map<Category, Set<String>> playerSelections = new EnumMap<>(Category.class);

    public SelectionEditorTemplate(Player player, SelectionEditorConfig config, ConfigManager configManager) {
        this.player = player;
        this.config = config;
        // Create server-themed title (title should already be enhanced by caller)
        this.inventory = Bukkit.createInventory(this, 54, config.getTitle());
        this.configManager = configManager;
        // Selections and drawing will be handled externally after instantiation
        for (Category category : Category.values()) {
            playerSelections.put(category, new HashSet<>());
        }
    }

    public Map<Category, Set<String>> getPlayerSelections() {
        return playerSelections;
    }

    public void setPlayerSelections(Map<Category, Set<String>> initialSelections) {
        for (Map.Entry<Category, Set<String>> entry : initialSelections.entrySet()) {
            if (entry.getValue() != null) {
                playerSelections.get(entry.getKey()).addAll(entry.getValue());
            }
        }
    }

    public void drawGUI() {
        // Fill border with decorative glass panes
        fillBorder();

        // Central display item
        inventory.setItem(4, config.getCentralDisplayItem());

        // Section headers
        inventory.setItem(9, createSectionHeader(Category.TIMEZONE));
        inventory.setItem(18, createSectionHeader(Category.EXPERIENCE));
        inventory.setItem(27, createSectionHeader(Category.DAYS));
        inventory.setItem(36, createSectionHeader(Category.SKILLS));
        
        // Section spacers with server styling
        Material spacingMaterial = configManager.getMaterial("selection-editor.materials.spacing", Material.BLACK_STAINED_GLASS_PANE);
        ItemStack spacingStack = VisualUtils.createServerItem(spacingMaterial, " ", null, false);
        inventory.setItem(10,spacingStack);
        inventory.setItem(19,spacingStack);
        inventory.setItem(28,spacingStack);
        inventory.setItem(37,spacingStack);
        // Populate content sections
        populateCategory(Category.TIMEZONE, 11, 17, config.getTimezoneOptions());
        populateCategory(Category.EXPERIENCE, 20, 26, config.getExperienceOptions());
        populateCategory(Category.DAYS, 29, 35, config.getDayOptions());
        populateCategory(Category.SKILLS, 38, 44, config.getSkillOptions());

        // Action buttons
        for (Map.Entry<Integer, SelectionEditorConfig.Pair<ItemStack, Runnable>> entry : config.getActionButtons().entrySet()) {
            inventory.setItem(entry.getKey(), entry.getValue().getFirst());
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().equals(inventory)) {
            HandlerList.unregisterAll(this);
            if (config.getOnClose() != null) {
                config.getOnClose().run();
            }
        }
    }

    private void fillBorder() {
        Material primaryMaterial = configManager.getMaterial("selection-editor.materials.border-primary", Material.PINK_STAINED_GLASS_PANE);
        Material secondaryMaterial = configManager.getMaterial("selection-editor.materials.border-secondary", Material.WHITE_STAINED_GLASS_PANE);

        // Create server-themed border items using VisualUtils
        ItemStack primaryBorder = VisualUtils.createServerItem(primaryMaterial, " ", null, false);
        ItemStack secondaryBorder = VisualUtils.createServerItem(secondaryMaterial, " ", null, false);

        for (int i = 0; i < 9; i++) { // Top row
            inventory.setItem(i, (i % 2 == 0) ? primaryBorder : secondaryBorder);
        }
        for (int i = 45; i < 54; i++) { // Bottom row
            inventory.setItem(i, (i % 2 == 0) ? primaryBorder : secondaryBorder);
        }
        for (int i = 9; i < 45; i += 9) { // Left border
            inventory.setItem(i, (i / 9 % 2 == 0) ? primaryBorder : secondaryBorder);
        }
        for (int i = 17; i < 54; i += 9) { // Right border
            inventory.setItem(i, (i / 9 % 2 == 0) ? primaryBorder : secondaryBorder);
        }
    }

    private ItemStack createBorderItem(Material material, String name) {
        return VisualUtils.createServerItem(material, name, null, false);
    }

    private ItemStack createSectionHeader(Category category) {
        Material headerMaterial = configManager.getMaterial("selection-editor.category-icons." + category.name().toLowerCase(), Material.BOOK);
        String categoryName = category.getDisplayName(configManager);

        // Create server-themed section header with small caps and proper styling
        List<String> instructions = Arrays.asList(
            "Select your " + categoryName.toLowerCase() + " preferences"
        );
        List<String> headerLore = VisualUtils.createServerLoreSection("Instructions", instructions);

        return VisualUtils.createServerItem(headerMaterial, categoryName, headerLore, true);
    }

    private void populateCategory(Category category, int startSlot, int endSlot, List<String> options) {
        SelectionMode mode = config.getSelectionModes().getOrDefault(category, SelectionMode.CHECKBOX);
        Set<String> currentSelections = playerSelections.get(category);

        int currentOptionIndex = 0;
        for (int slot = startSlot; slot <= endSlot; slot++) {
            if (currentOptionIndex < options.size()) {
                String displayName = options.get(currentOptionIndex);
                boolean isSelected = currentSelections.contains(displayName);
                inventory.setItem(slot, createSelectionItem(displayName, isSelected, mode));
                currentOptionIndex++;
            } else {
                inventory.setItem(slot, null); // Clear unused slots
            }
        }
    }

    private ItemStack createSelectionItem(String displayName, boolean isSelected, SelectionMode mode) {
        Material material = isSelected ?
            configManager.getMaterial("selection-editor.materials.item-selected", Material.GREEN_CANDLE) :
            configManager.getMaterial("selection-editor.materials.item-unselected", Material.GRAY_CANDLE);

        // Create enhanced lore with proper styling
        List<String> lore = new ArrayList<>();

        // Status indicator
        String statusColor = isSelected ? VisualUtils.ColorPalette.SUCCESS : VisualUtils.ColorPalette.TEXT_MUTED;
        String statusSymbol = isSelected ? VisualUtils.Symbols.CHECK : VisualUtils.Symbols.BULLET;
        lore.add(net.md_5.bungee.api.ChatColor.of(statusColor) + statusSymbol + " " +
                (isSelected ? "Selected" : "Available"));

        lore.add("");

        // Action instructions
        String actionText = isSelected ? "Click to deselect" : "Click to select";
        lore.add(VisualUtils.createActionLore(actionText, VisualUtils.ColorPalette.INFO));

        // Selection mode indicator
        String modeSymbol = (mode == SelectionMode.RADIO) ? VisualUtils.Symbols.CROWN : VisualUtils.Symbols.DIAMOND;
        String modeText = (mode == SelectionMode.RADIO) ? "Single choice" : "Multiple choice";
        lore.add(net.md_5.bungee.api.ChatColor.of(VisualUtils.ColorPalette.TEXT_SECONDARY) +
                modeSymbol + " " + modeText);

        return VisualUtils.createServerItem(material, displayName, lore, isSelected);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        if (!(event.getWhoClicked() instanceof Player)) return;

        event.setCancelled(true); // Prevent players from taking items

        int clickedSlot = event.getRawSlot();
        Player clicker = (Player) event.getWhoClicked();

        // Handle action buttons
        if (config.getActionButtons().containsKey(clickedSlot)) {
            config.getActionButtons().get(clickedSlot).getSecond().run();
            return;
        }

        // Handle content selection
        Category clickedCategory = getCategoryFromSlot(clickedSlot);
        if (clickedCategory != null) {
            String option = getOptionFromSlot(clickedSlot, clickedCategory);
            if (option != null && !option.trim().isEmpty()) {
                toggleSelection(clickedCategory, option);
                // Redraw only the affected category to update visuals
                int startSlot = getCategoryStartSlot(clickedCategory);
                int endSlot = getCategoryEndSlot(clickedCategory);
                populateCategory(clickedCategory, startSlot, endSlot, getCategoryOptions(clickedCategory));
            }
        }
    }

    private Category getCategoryFromSlot(int slot) {
        if (slot >= 11 && slot <= 17) return Category.TIMEZONE;
        if (slot >= 20 && slot <= 26) return Category.EXPERIENCE;
        if (slot >= 29 && slot <= 35) return Category.DAYS;
        if (slot >= 38 && slot <= 44) return Category.SKILLS;
        return null;
    }

    private int getCategoryStartSlot(Category category) {
        switch (category) {
            case TIMEZONE: return 11;
            case EXPERIENCE: return 20;
            case DAYS: return 29;
            case SKILLS: return 38;
            default: return -1;
        }
    }

    private int getCategoryEndSlot(Category category) {
        switch (category) {
            case TIMEZONE: return 17;
            case EXPERIENCE: return 26;
            case DAYS: return 35;
            case SKILLS: return 44;
            default: return -1;
        }
    }

    private List<String> getCategoryOptions(Category category) {
        switch (category) {
            case TIMEZONE: return config.getTimezoneOptions();
            case EXPERIENCE: return config.getExperienceOptions();
            case DAYS: return config.getDayOptions();
            case SKILLS: return config.getSkillOptions();
            default: return Collections.emptyList();
        }
    }

    private String getOptionFromSlot(int slot, Category category) {
        // Always use index-based lookup as it's more reliable than trying to parse
        // complex gradient-colored display names back to original option values
        List<String> options = getCategoryOptions(category);
        int startSlot = getCategoryStartSlot(category);
        int index = slot - startSlot;

        if (index >= 0 && index < options.size()) {
            return options.get(index);
        }

        // Fallback: try to extract from display name if index fails
        ItemStack item = inventory.getItem(slot);
        if (item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            String displayName = item.getItemMeta().getDisplayName();

            // Strip all color codes including hex colors (§x§r§g§b format and ChatColor format)
            String strippedName = displayName
                .replaceAll("§[0-9a-fk-or]", "")  // Remove standard color codes
                .replaceAll("§x(?:§[0-9a-f]){6}", ""); // Remove hex color codes

            // Try to match against available options (case-insensitive)
            for (String option : options) {
                if (option.equalsIgnoreCase(strippedName.trim())) {
                    return option;
                }
            }
        }

        return null;
    }


    private void toggleSelection(Category category, String option) {
        Set<String> selections = playerSelections.get(category);
        SelectionMode mode = config.getSelectionModes().getOrDefault(category, SelectionMode.CHECKBOX);

        if (mode == SelectionMode.RADIO) {
            selections.clear(); // Deselect all others
            selections.add(option); // Select the new one
        } else { // CHECKBOX
            if (selections.contains(option)) {
                selections.remove(option);
            } else {
                selections.add(option);
            }
        }
    }

    private boolean validateSelections(Player clicker) {
        boolean allValid = true;
        for (Map.Entry<Category, Function<Map<Category, Set<String>>, ValidationResult>> entry : config.getValidationRules().entrySet()) {
            Category category = entry.getKey();
            Function<Map<Category, Set<String>>, ValidationResult> validator = entry.getValue();
            ValidationResult result = validator.apply(playerSelections);

            if (!result.isValid()) {
                clicker.sendMessage(configManager.getGuiString("selection-editor.messages.validation-error", "&cValidationError (%category%): %error%").replace("%category%", category.getDisplayName()).replace("%error%", result.getErrorMessage()));
                allValid = false;
            }
        }
        return allValid;
    }

    // --- Enums and Config Classes ---

    public enum SelectionMode {
        RADIO,
        CHECKBOX
    }

    public enum Category {
        TIMEZONE("selection-editor.categories.timezone", "Timezone"),
        EXPERIENCE("selection-editor.categories.experience", "Experience"),
        DAYS("selection-editor.categories.days", "Days"),
        SKILLS("selection-editor.categories.skills", "Skills");

        private final String configPath;
        private final String defaultName;

        Category(String configPath, String defaultName) {
            this.configPath = configPath;
            this.defaultName = defaultName;
        }

        public String getDisplayName(ConfigManager configManager) {
            return configManager.getGuiString(configPath, defaultName);
        }

        public String getDisplayName() {
            return defaultName; // Fallback for contexts without config manager
        }
    }

    public static class SelectionEditorConfig {
        private String title;
        private ItemStack centralDisplayItem;
        private Map<Category, SelectionMode> selectionModes;
        private Map<Integer, Pair<ItemStack, Runnable>> actionButtons; // Slot -> (ItemStack, Runnable)
        private Map<Category, Function<Map<Category, Set<String>>, ValidationResult>> validationRules;
        private ConfigManager configManager;
        private Runnable onClose; // New field

        public SelectionEditorConfig(String title, ItemStack centralDisplayItem, ConfigManager configManager) {
            this.title = title;
            this.centralDisplayItem = centralDisplayItem;
            this.selectionModes = new EnumMap<>(Category.class);
            this.actionButtons = new HashMap<>();
            this.validationRules = new EnumMap<>(Category.class);
            this.configManager = configManager;
        }

        public String getTitle() { return title; }
        public ItemStack getCentralDisplayItem() { return centralDisplayItem; }
        public Map<Category, SelectionMode> getSelectionModes() { return selectionModes; }
        public Map<Integer, Pair<ItemStack, Runnable>> getActionButtons() { return actionButtons; }
        public Map<Category, Function<Map<Category, Set<String>>, ValidationResult>> getValidationRules() { return validationRules; }
        public Runnable getOnClose() { return onClose; }

        public SelectionEditorConfig withSelectionMode(Category category, SelectionMode mode) {
            this.selectionModes.put(category, mode);
            return this;
        }

        public SelectionEditorConfig withActionButton(int slot, ItemStack button, Runnable action) {
            this.actionButtons.put(slot, new Pair<>(button, action));
            return this;
        }

        public SelectionEditorConfig withValidationRule(Category category, Function<Map<Category, Set<String>>, ValidationResult> rule) {
            this.validationRules.put(category, rule);
            return this;
        }

        public SelectionEditorConfig withOnClose(Runnable onClose) {
            this.onClose = onClose;
            return this;
        }

        // Options for each category (loaded from config.yml)
        public List<String> getTimezoneOptions() { return configManager.getGuiStringList("selection-editor.options.timezones", Arrays.asList("NA-WEST", "NA-EAST", "EU-WEST", "EU-CENTRAL", "ASIA", "OCEANIA")); }
        public List<String> getExperienceOptions() { return configManager.getGuiStringList("selection-editor.options.experiences", Arrays.asList("Under 6 months", "More than 1 year", "1-2 YEARS", "2-3 years", "3-4 years", "4-5 years", "5+ years")); }
        public List<String> getDayOptions() { return configManager.getGuiStringList("selection-editor.options.days", Arrays.asList("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")); }
        public List<String> getSkillOptions() { return configManager.getGuiStringList("selection-editor.options.skills", Arrays.asList("Cannoning", "PVP", "Base Defense", "Base Design", "Redstone", "Farming", "Fishing")); }

        public SelectionEditorConfig withTimezoneOptions(List<String> options) { return this; } // No-op, loaded from config
        public SelectionEditorConfig withExperienceOptions(List<String> options) { return this; } // No-op, loaded from config
        public SelectionEditorConfig withDayOptions(List<String> options) { return this; } // No-op, loaded from config
        public SelectionEditorConfig withSkillOptions(List<String> options) { return this; } // No-op, loaded from config

        public static class Pair<F, S> {
            private F first;
            private S second;

            public Pair(F first, S second) {
                this.first = first;
                this.second = second;
            }

            public F getFirst() {
                return first;
            }

            public S getSecond() {
                return second;
            }
        }
    }

    public static class ValidationResult {
        private final boolean valid;
        private final String errorMessage;

        public ValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }

        public static ValidationResult valid() {
            return new ValidationResult(true, null);
        }

        public static ValidationResult invalid(String errorMessage) {
            return new ValidationResult(false, errorMessage);
        }

        public boolean isValid() {
            return valid;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }

    // Custom BiConsumer for callbacks
    @FunctionalInterface
    public interface BiConsumer<T, U> {
        void accept(T t, U u);
    }
}