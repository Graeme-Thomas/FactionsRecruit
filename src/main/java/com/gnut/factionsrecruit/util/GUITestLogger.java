package com.gnut.factionsrecruit.util;

import org.bukkit.entity.Player;

import com.gnut.factionsrecruit.FactionsRecruit;
import com.gnut.factionsrecruit.manager.ConfigManager;

import java.util.logging.Logger;

/**
 * Debug logging utility for GUI interactions and title matching
 * Helps verify GUI functionality during testing and troubleshooting
 */
public class GUITestLogger {

    private final Logger logger;
    private final ConfigManager configManager;
    private boolean debugEnabled;

    public GUITestLogger(FactionsRecruit plugin) {
        this.logger = plugin.getLogger();
        this.configManager = plugin.getConfigManager();
        this.debugEnabled = configManager.isDebugLoggingEnabled();
    }

    /**
     * Updates debug logging state from configuration
     */
    public void updateDebugState() {
        this.debugEnabled = configManager.isDebugLoggingEnabled();
    }

    /**
     * Logs title matching attempts for GUI detection
     */
    public void logTitleMatching(String inventoryTitle, String stripped, String normalized, boolean matched, String expectedTitle) {
        if (!debugEnabled) return;

        logger.info(String.format(
            "[GUI-DEBUG] Title Matching: '%s' -> stripped:'%s' -> normalized:'%s' | Expected:'%s' | Result:%s",
            inventoryTitle, stripped, normalized, expectedTitle, matched ? "MATCHED" : "NO MATCH"
        ));
    }

    /**
     * Logs GUI click events for debugging interaction issues
     */
    public void logClickEvent(Player player, int slot, String guiType, String itemName) {
        if (!debugEnabled) return;

        logger.info(String.format(
            "[GUI-DEBUG] Click Event: %s clicked slot %d in '%s' (item: %s)",
            player.getName(), slot, guiType, itemName != null ? itemName : "null"
        ));
    }

    /**
     * Logs GUI opening events
     */
    public void logGUIOpen(Player player, String guiType, String title) {
        if (!debugEnabled) return;

        logger.info(String.format(
            "[GUI-DEBUG] GUI Opened: %s opened '%s' with title '%s'",
            player.getName(), guiType, title
        ));
    }

    /**
     * Logs GUI closing events
     */
    public void logGUIClose(Player player, String guiType) {
        if (!debugEnabled) return;

        logger.info(String.format(
            "[GUI-DEBUG] GUI Closed: %s closed '%s'",
            player.getName(), guiType
        ));
    }

    /**
     * Logs title normalization process step-by-step
     */
    public void logTitleNormalization(String original, String stripped, String normalized) {
        if (!debugEnabled) return;

        logger.info(String.format(
            "[GUI-DEBUG] Title Normalization: '%s' -> stripped:'%s' -> normalized:'%s'",
            original, stripped, normalized
        ));
    }

    /**
     * Logs small caps conversion for debugging
     */
    public void logSmallCapsConversion(String input, String output) {
        if (!debugEnabled) return;

        logger.info(String.format(
            "[GUI-DEBUG] Small Caps Conversion: '%s' -> '%s'",
            input, output
        ));
    }

    /**
     * Logs color stripping process
     */
    public void logColorStripping(String input, String output) {
        if (!debugEnabled) return;

        logger.info(String.format(
            "[GUI-DEBUG] Color Stripping: '%s' -> '%s'",
            input, output
        ));
    }

    /**
     * Logs when a GUI interaction is cancelled due to matching rules
     */
    public void logInteractionCancelled(Player player, String reason, String inventoryTitle) {
        if (!debugEnabled) return;

        logger.info(String.format(
            "[GUI-DEBUG] Interaction Cancelled: %s - %s (inventory: '%s')",
            player.getName(), reason, inventoryTitle
        ));
    }

    /**
     * Logs performance metrics for title matching
     */
    public void logPerformanceMetric(String operation, long durationNanos) {
        if (!debugEnabled) return;

        double durationMs = durationNanos / 1_000_000.0;
        if (durationMs > 5.0) { // Only log if operation takes more than 5ms
            logger.warning(String.format(
                "[GUI-DEBUG] Performance Warning: %s took %.2f ms",
                operation, durationMs
            ));
        }
    }

    /**
     * Logs the complete GUI state for debugging
     */
    public void logGUIState(Player player, String inventoryTitle, int totalSlots, int clickedSlot) {
        if (!debugEnabled) return;

        logger.info(String.format(
            "[GUI-DEBUG] GUI State: Player=%s | Title='%s' | Slots=%d | ClickedSlot=%d",
            player.getName(), inventoryTitle, totalSlots, clickedSlot
        ));
    }

    /**
     * Logs error conditions in GUI handling
     */
    public void logError(String operation, Exception e) {
        logger.severe(String.format(
            "[GUI-ERROR] Error in %s: %s",
            operation, e.getMessage()
        ));
        if (debugEnabled) {
            e.printStackTrace();
        }
    }

    /**
     * Logs validation failures for GUI operations
     */
    public void logValidationFailure(String validation, String expected, String actual) {
        if (!debugEnabled) return;

        logger.warning(String.format(
            "[GUI-DEBUG] Validation Failed: %s | Expected:'%s' | Actual:'%s'",
            validation, expected, actual
        ));
    }

    /**
     * Logs successful GUI operations for positive confirmation
     */
    public void logSuccess(String operation, String details) {
        if (!debugEnabled) return;

        logger.info(String.format(
            "[GUI-DEBUG] Success: %s - %s",
            operation, details
        ));
    }

    /**
     * Creates a formatted separator for log readability
     */
    public void logSeparator(String section) {
        if (!debugEnabled) return;

        logger.info(String.format(
            "[GUI-DEBUG] ==================== %s ====================",
            section
        ));
    }

    /**
     * Logs the current testing mode status
     */
    public void logTestingMode(boolean enabled) {
        logger.info(String.format(
            "[GUI-DEBUG] Testing Mode: %s",
            enabled ? "ENABLED" : "DISABLED"
        ));
    }
}