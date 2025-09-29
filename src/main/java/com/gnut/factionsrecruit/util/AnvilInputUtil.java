package com.gnut.factionsrecruit.util;

import com.gnut.factionsrecruit.FactionsRecruit;
import com.gnut.factionsrecruit.VisualUtils;
import net.md_5.bungee.api.ChatColor;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Collections;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Utility class for creating themed AnvilGUI text input dialogs
 * Provides consistent styling with server theme (white/pink/red)
 */
public class AnvilInputUtil {

    /**
     * Open a simple text input dialog
     *
     * @param plugin Plugin instance
     * @param player Player to show dialog to
     * @param title Dialog title (will be styled with server theme)
     * @param initialText Initial text in input field
     * @param onComplete Callback when player submits valid input (text)
     * @param onCancel Callback when player closes without submitting
     */
    public static void openTextInput(FactionsRecruit plugin, Player player, String title, String initialText,
                                      Consumer<String> onComplete, Runnable onCancel) {
        openTextInput(plugin, player, title, initialText, null, onComplete, onCancel);
    }

    /**
     * Open a text input dialog with validation
     *
     * @param plugin Plugin instance
     * @param player Player to show dialog to
     * @param title Dialog title (will be styled with server theme)
     * @param initialText Initial text in input field
     * @param validator Function that returns error message if invalid, null if valid
     * @param onComplete Callback when player submits valid input (text)
     * @param onCancel Callback when player closes without submitting
     */
    public static void openTextInput(FactionsRecruit plugin, Player player, String title, String initialText,
                                      java.util.function.Function<String, String> validator,
                                      Consumer<String> onComplete, Runnable onCancel) {

        // Style the title with server theme
        String styledTitle = VisualUtils.SmallCaps.convert(title);
        styledTitle = VisualUtils.createGradient(styledTitle,
                VisualUtils.ColorPalette.SERVER_WHITE,
                VisualUtils.ColorPalette.SERVER_PINK);

        new AnvilGUI.Builder()
                .plugin(plugin)
                .title(styledTitle)
                .text(initialText != null ? initialText : "")
                .itemLeft(createInputIcon())
                .onClick((slot, stateSnapshot) -> {
                    if (slot != AnvilGUI.Slot.OUTPUT) {
                        return Collections.emptyList();
                    }

                    String text = stateSnapshot.getText().trim();

                    // Validate input if validator provided
                    if (validator != null) {
                        String errorMessage = validator.apply(text);
                        if (errorMessage != null) {
                            // Show error and keep dialog open
                            player.sendMessage(ChatColor.of(VisualUtils.ColorPalette.ERROR) +
                                    VisualUtils.Symbols.CROSS + " " + errorMessage);
                            return Arrays.asList(AnvilGUI.ResponseAction.replaceInputText(text));
                        }
                    }

                    // Input is valid, complete
                    onComplete.accept(text);
                    return Arrays.asList(AnvilGUI.ResponseAction.close());
                })
                .onClose(stateSnapshot -> {
                    // Check if player submitted or just closed
                    // If text matches initial, they likely just closed
                    if (stateSnapshot.getText().equals(initialText) ||
                        stateSnapshot.getText().isEmpty()) {
                        if (onCancel != null) {
                            onCancel.run();
                        }
                    }
                })
                .open(player);
    }

    /**
     * Open a Discord username input dialog
     * Validates Discord username format (3-32 characters, alphanumeric + _ and .)
     *
     * @param plugin Plugin instance
     * @param player Player to show dialog to
     * @param currentDiscord Current Discord username (or null)
     * @param onComplete Callback with new Discord username
     * @param onCancel Callback when cancelled
     */
    public static void openDiscordInput(FactionsRecruit plugin, Player player, String currentDiscord,
                                         Consumer<String> onComplete, Runnable onCancel) {
        openTextInput(
                plugin,
                player,
                "Enter Discord Username",
                currentDiscord != null ? currentDiscord : "",
                text -> {
                    // Validate Discord username
                    if (text.isEmpty()) {
                        return "Discord username cannot be empty!";
                    }
                    if (text.length() < 2 || text.length() > 32) {
                        return "Discord username must be 2-32 characters!";
                    }
                    if (!text.matches("[a-zA-Z0-9_.]+")) {
                        return "Discord username can only contain letters, numbers, _ and .";
                    }
                    return null; // Valid
                },
                onComplete,
                onCancel
        );
    }

    /**
     * Open a faction name input dialog
     * Validates faction name (1-20 characters, alphanumeric)
     *
     * @param plugin Plugin instance
     * @param player Player to show dialog to
     * @param onComplete Callback with faction name
     * @param onCancel Callback when cancelled
     */
    public static void openFactionNameInput(FactionsRecruit plugin, Player player,
                                             Consumer<String> onComplete, Runnable onCancel) {
        openTextInput(
                plugin,
                player,
                "Enter Faction Name",
                "",
                text -> {
                    // Validate faction name
                    if (text.isEmpty()) {
                        return "Faction name cannot be empty!";
                    }
                    if (text.length() > 20) {
                        return "Faction name must be 20 characters or less!";
                    }
                    if (!text.matches("[a-zA-Z0-9_]+")) {
                        return "Faction name can only contain letters, numbers, and _";
                    }
                    return null; // Valid
                },
                onComplete,
                onCancel
        );
    }

    /**
     * Open a numeric input dialog
     *
     * @param plugin Plugin instance
     * @param player Player to show dialog to
     * @param title Dialog title
     * @param min Minimum value (inclusive)
     * @param max Maximum value (inclusive)
     * @param onComplete Callback with parsed integer
     * @param onCancel Callback when cancelled
     */
    public static void openNumericInput(FactionsRecruit plugin, Player player, String title,
                                         int min, int max,
                                         Consumer<Integer> onComplete, Runnable onCancel) {
        openTextInput(
                plugin,
                player,
                title,
                String.valueOf(min),
                text -> {
                    // Validate numeric input
                    try {
                        int value = Integer.parseInt(text);
                        if (value < min || value > max) {
                            return "Value must be between " + min + " and " + max + "!";
                        }
                        return null; // Valid
                    } catch (NumberFormatException e) {
                        return "Please enter a valid number!";
                    }
                },
                text -> onComplete.accept(Integer.parseInt(text)),
                onCancel
        );
    }

    /**
     * Open a confirmation dialog (yes/no)
     *
     * @param plugin Plugin instance
     * @param player Player to show dialog to
     * @param title Dialog title
     * @param message Message to confirm
     * @param onConfirm Callback when player confirms
     * @param onCancel Callback when player cancels
     */
    public static void openConfirmation(FactionsRecruit plugin, Player player, String title, String message,
                                         Runnable onConfirm, Runnable onCancel) {
        // Style the title with server theme
        String styledTitle = VisualUtils.SmallCaps.convert(title);
        styledTitle = VisualUtils.createGradient(styledTitle,
                VisualUtils.ColorPalette.SERVER_WHITE,
                VisualUtils.ColorPalette.SERVER_PINK);

        new AnvilGUI.Builder()
                .plugin(plugin)
                .title(styledTitle)
                .text("Type 'yes' to confirm")
                .itemLeft(createConfirmIcon())
                .onClick((slot, stateSnapshot) -> {
                    if (slot != AnvilGUI.Slot.OUTPUT) {
                        return Collections.emptyList();
                    }

                    String text = stateSnapshot.getText().trim().toLowerCase();

                    if (text.equals("yes") || text.equals("y") || text.equals("confirm")) {
                        onConfirm.run();
                        return Arrays.asList(AnvilGUI.ResponseAction.close());
                    } else {
                        player.sendMessage(ChatColor.of(VisualUtils.ColorPalette.WARNING) +
                                "Type 'yes' to confirm or close to cancel");
                        return Arrays.asList(AnvilGUI.ResponseAction.replaceInputText(""));
                    }
                })
                .onClose(stateSnapshot -> {
                    if (!stateSnapshot.getText().equalsIgnoreCase("yes")) {
                        if (onCancel != null) {
                            onCancel.run();
                        }
                    }
                })
                .open(player);

        // Send message to player explaining the dialog
        player.sendMessage(ChatColor.of(VisualUtils.ColorPalette.INFO) + message);
    }

    /**
     * Open a multi-line text input dialog (uses multiple sequential inputs)
     * Useful for collecting descriptions, messages, etc.
     *
     * @param plugin Plugin instance
     * @param player Player to show dialog to
     * @param title Dialog title
     * @param lines Number of lines to collect
     * @param onComplete Callback with collected lines
     * @param onCancel Callback when cancelled
     */
    public static void openMultilineInput(FactionsRecruit plugin, Player player, String title, int lines,
                                           Consumer<String[]> onComplete, Runnable onCancel) {
        String[] collectedLines = new String[lines];
        final int[] currentLine = {0};

        BiConsumer<Integer, String> collectLine = new BiConsumer<Integer, String>() {
            @Override
            public void accept(Integer lineIndex, String text) {
                collectedLines[lineIndex] = text;
                currentLine[0]++;

                if (currentLine[0] < lines) {
                    // Open next line input
                    openTextInput(
                            plugin,
                            player,
                            title + " (Line " + (currentLine[0] + 1) + "/" + lines + ")",
                            "",
                            nextText -> this.accept(currentLine[0], nextText),
                            onCancel
                    );
                } else {
                    // All lines collected
                    onComplete.accept(collectedLines);
                }
            }
        };

        // Start with first line
        openTextInput(
                plugin,
                player,
                title + " (Line 1/" + lines + ")",
                "",
                text -> collectLine.accept(0, text),
                onCancel
        );
    }

    /**
     * Create a themed input icon for left slot
     */
    private static ItemStack createInputIcon() {
        return VisualUtils.createServerItem(
                Material.NAME_TAG,
                "Enter Text",
                Arrays.asList(
                        ChatColor.of(VisualUtils.ColorPalette.TEXT_SECONDARY) +
                                "Type your input above",
                        ChatColor.of(VisualUtils.ColorPalette.INFO) +
                                VisualUtils.Symbols.ARROW_RIGHT + " Click output to submit"
                ),
                false
        );
    }

    /**
     * Create a themed confirmation icon for left slot
     */
    private static ItemStack createConfirmIcon() {
        return VisualUtils.createServerItem(
                Material.LIME_WOOL,
                "Confirmation",
                Arrays.asList(
                        ChatColor.of(VisualUtils.ColorPalette.SUCCESS) +
                                "Type 'yes' to confirm",
                        ChatColor.of(VisualUtils.ColorPalette.ERROR) +
                                "Close to cancel"
                ),
                false
        );
    }

    /**
     * Create a list input dialog (add items one by one)
     * Player enters items sequentially until they enter "done"
     *
     * @param plugin Plugin instance
     * @param player Player to show dialog to
     * @param title Dialog title
     * @param maxItems Maximum number of items
     * @param onComplete Callback with collected items
     * @param onCancel Callback when cancelled
     */
    public static void openListInput(FactionsRecruit plugin, Player player, String title, int maxItems,
                                      Consumer<java.util.List<String>> onComplete, Runnable onCancel) {
        java.util.List<String> items = new java.util.ArrayList<>();

        Consumer<String> addItem = new Consumer<String>() {
            @Override
            public void accept(String item) {
                if (item.equalsIgnoreCase("done")) {
                    // Player finished entering items
                    if (items.isEmpty()) {
                        player.sendMessage(ChatColor.of(VisualUtils.ColorPalette.WARNING) +
                                "No items entered!");
                        if (onCancel != null) {
                            onCancel.run();
                        }
                    } else {
                        onComplete.accept(items);
                    }
                    return;
                }

                if (!item.trim().isEmpty()) {
                    items.add(item.trim());
                    player.sendMessage(ChatColor.of(VisualUtils.ColorPalette.SUCCESS) +
                            VisualUtils.Symbols.CHECK + " Added: " + item);
                }

                if (items.size() >= maxItems) {
                    // Max items reached
                    player.sendMessage(ChatColor.of(VisualUtils.ColorPalette.INFO) +
                            "Maximum items reached (" + maxItems + ")");
                    onComplete.accept(items);
                    return;
                }

                // Open dialog for next item
                openTextInput(
                        plugin,
                        player,
                        title + " (" + items.size() + "/" + maxItems + ")",
                        "",
                        this,
                        onCancel
                );
            }
        };

        // Show initial instructions
        player.sendMessage("");
        player.sendMessage(ChatColor.of(VisualUtils.ColorPalette.INFO) +
                VisualUtils.Symbols.ARROW_RIGHT + " Enter items one at a time");
        player.sendMessage(ChatColor.of(VisualUtils.ColorPalette.INFO) +
                VisualUtils.Symbols.ARROW_RIGHT + " Type 'done' when finished");
        player.sendMessage(ChatColor.of(VisualUtils.ColorPalette.TEXT_MUTED) +
                "Maximum " + maxItems + " items");
        player.sendMessage("");

        // Start with first item
        openTextInput(
                plugin,
                player,
                title + " (0/" + maxItems + ")",
                "",
                addItem,
                onCancel
        );
    }
}