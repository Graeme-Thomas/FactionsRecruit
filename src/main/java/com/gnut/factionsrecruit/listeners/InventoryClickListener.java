package com.gnut.factionsrecruit.listeners;

import com.gnut.factionsrecruit.FactionsRecruit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.InventoryHolder;

/**
 * Handles inventory click events for GUI interactions
 *
 * This listener manages all GUI interactions including:
 * - Click handling for all recruitment GUIs
 * - Navigation between different GUI screens
 * - Action execution based on clicked items
 */
public class InventoryClickListener implements Listener {

    private final FactionsRecruit plugin;

    public InventoryClickListener(FactionsRecruit plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        // Check if the click is in a custom inventory
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        InventoryHolder holder = event.getInventory().getHolder();

        // Check if this is one of our custom GUIs
        // TODO: Implement GUI holder checks once GUI classes are implemented
        // For now, we'll detect based on inventory title or holder type

        String inventoryTitle = event.getView().getTitle();

        // Check if this is a FactionsRecruit GUI (titles will have our server styling)
        if (isRecruitmentGUI(inventoryTitle)) {
            // Cancel the event to prevent item manipulation
            event.setCancelled(true);

            // TODO: Route clicks to appropriate GUI handlers
            // This will be implemented once GUI classes are ready
            // Example:
            // if (holder instanceof LandingUI) {
            //     ((LandingUI) holder).handleClick(event);
            // } else if (holder instanceof FactionsRecruitingGUI) {
            //     ((FactionsRecruitingGUI) holder).handleClick(event);
            // }

            plugin.getLogger().fine("GUI click detected for player: " + player.getName() +
                    " in inventory: " + inventoryTitle);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent event) {
        // Handle cleanup when GUIs are closed
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getPlayer();
        String inventoryTitle = event.getView().getTitle();

        // Check if this is a FactionsRecruit GUI
        if (isRecruitmentGUI(inventoryTitle)) {
            // TODO: Cleanup any temporary data or cached GUI state
            plugin.getLogger().fine("GUI closed for player: " + player.getName());
        }
    }

    /**
     * Checks if an inventory is a recruitment GUI based on its title
     *
     * @param title The inventory title
     * @return true if this is a recruitment GUI
     */
    private boolean isRecruitmentGUI(String title) {
        // Check for our custom GUI titles
        // These will use VisualUtils styling which includes special characters
        // We can detect them by checking for our decorative elements or specific keywords

        return title != null && (
            title.contains("►") || // Server arrow symbols
            title.contains("◄") ||
            title.contains("ʀᴇᴄʀᴜɪᴛ") || // Small caps "RECRUIT"
            title.contains("ꜰᴀᴄᴛɪᴏɴꜱ") || // Small caps "FACTIONS"
            title.contains("ᴀᴘᴘʟɪᴄᴀᴛɪᴏɴꜱ") || // Small caps "APPLICATIONS"
            title.contains("ꜱᴇᴛᴛɪɴɢꜱ") // Small caps "SETTINGS"
        );
    }
}