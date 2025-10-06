package com.gnut.factionsrecruit;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.gnut.factionsrecruit.util.ItemBuilder;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FilterGUI {

    private final FactionsRecruit plugin;

    public FilterGUI(FactionsRecruit plugin) {
        this.plugin = plugin;
    }

    public void openFilterUI(Player player) {
        // Create server-themed central display item
        List<String> instructions = Arrays.asList(
            "Customize your search criteria",
            "to find the perfect candidates"
        );
        List<String> steps = Arrays.asList(
            VisualUtils.Symbols.ARROW_RIGHT + " Select options in each category",
            VisualUtils.Symbols.ARROW_RIGHT + " Apply filters to see results"
        );

        List<String> centralLore = VisualUtils.createServerLoreSection("Instructions", instructions);
        centralLore.addAll(VisualUtils.createServerLoreSection("Steps", steps));

        ItemStack centralItem = VisualUtils.createServerItem(
            Material.HOPPER,
            "Recruitment Filter",
            centralLore,
            true
        );

        SelectionEditorTemplate.SelectionEditorConfig config = new SelectionEditorTemplate.SelectionEditorConfig(
                VisualUtils.createCompactServerTitle("Filter Results"),
                centralItem,
                plugin.getConfigManager()
        );

        config.withOnClose(() -> {
            plugin.getRecruitGUI().openMainMenu(player, plugin.getFilter());
        });

        final SelectionEditorTemplate editor = new SelectionEditorTemplate(player, config, plugin.getConfigManager());

        config.withSelectionMode(SelectionEditorTemplate.Category.TIMEZONE, SelectionEditorTemplate.SelectionMode.CHECKBOX);
        config.withSelectionMode(SelectionEditorTemplate.Category.EXPERIENCE, SelectionEditorTemplate.SelectionMode.CHECKBOX);
        config.withSelectionMode(SelectionEditorTemplate.Category.DAYS, SelectionEditorTemplate.SelectionMode.CHECKBOX);
        config.withSelectionMode(SelectionEditorTemplate.Category.SKILLS, SelectionEditorTemplate.SelectionMode.CHECKBOX);

        // Enhanced Apply Button
        List<String> applyLore = Arrays.asList(
            net.md_5.bungee.api.ChatColor.of(VisualUtils.ColorPalette.TEXT_SECONDARY) + "Apply your selected filters",
            net.md_5.bungee.api.ChatColor.of(VisualUtils.ColorPalette.TEXT_SECONDARY) + "to the recruitment browser",
            "",
            VisualUtils.createActionLore("Click to apply filters", VisualUtils.ColorPalette.SUCCESS)
        );
        ItemStack applyButton = VisualUtils.createServerItem(
            plugin.getConfigManager().getMaterial("filter.materials.apply-button", Material.EMERALD),
            "Apply Filters",
            applyLore,
            true
        );

        // Enhanced Reset Button
        List<String> resetLore = Arrays.asList(
            net.md_5.bungee.api.ChatColor.of(VisualUtils.ColorPalette.TEXT_SECONDARY) + "Clear all current filters",
            net.md_5.bungee.api.ChatColor.of(VisualUtils.ColorPalette.TEXT_SECONDARY) + "and start fresh",
            "",
            VisualUtils.createActionLore("Click to reset filters", VisualUtils.ColorPalette.ERROR)
        );
        ItemStack resetButton = VisualUtils.createServerItem(
            plugin.getConfigManager().getMaterial("filter.materials.reset-button", Material.BARRIER),
            "Reset Filters",
            resetLore,
            false
        );

        config.withActionButton(49, applyButton, () -> {
            Map<SelectionEditorTemplate.Category, Set<String>> selections = editor.getPlayerSelections();
            FactionApplication filter = new FactionApplication(
                    "",
                    "",
                    null,
                    Arrays.asList(selections.get(SelectionEditorTemplate.Category.TIMEZONE).toArray(new String[0])),
                    Arrays.asList(selections.get(SelectionEditorTemplate.Category.EXPERIENCE).toArray(new String[0])),
                    Arrays.asList(selections.get(SelectionEditorTemplate.Category.DAYS).toArray(new String[0])),
                    Arrays.asList(selections.get(SelectionEditorTemplate.Category.SKILLS).toArray(new String[0])),
                    false,
                    0,
                    0
            );
            plugin.setFilter(filter);
            player.sendMessage(plugin.getConfigManager().getPrefix() +
                net.md_5.bungee.api.ChatColor.of(VisualUtils.ColorPalette.SUCCESS) +
                VisualUtils.Symbols.CHECK + " Filters applied successfully!");
            plugin.getRecruitGUI().openMainMenu(player, filter);
        });
        config.withActionButton(48, resetButton, () -> {
            plugin.getFilterManager().clearPlayerFilters(player.getUniqueId());
            plugin.setFilter(null); // Clear the main plugin filter
            openFilterUI(player);
        });

        FactionApplication currentFilter = plugin.getFilter();
        if (currentFilter != null) {
            Map<SelectionEditorTemplate.Category, Set<String>> initialSelections = new HashMap<>();
            initialSelections.put(SelectionEditorTemplate.Category.TIMEZONE, new HashSet<>(currentFilter.getDesiredTimezones()));
            initialSelections.put(SelectionEditorTemplate.Category.EXPERIENCE, new HashSet<>(currentFilter.getExperienceLevels()));
            initialSelections.put(SelectionEditorTemplate.Category.DAYS, new HashSet<>(currentFilter.getRequiredDays()));
            initialSelections.put(SelectionEditorTemplate.Category.SKILLS, new HashSet<>(currentFilter.getDesiredSkills()));
            editor.setPlayerSelections(initialSelections);
        }

        editor.drawGUI();
        player.openInventory(editor.getInventory());
        plugin.getServer().getPluginManager().registerEvents(editor, plugin);
    }

    public void handleFilterClick(Player player, int clickedSlot) {
        // This method is now handled by the SelectionEditorTemplate
    }
}
