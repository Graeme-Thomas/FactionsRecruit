package com.gnut.factionsrecruit.manager;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GuiManager {

    @FunctionalInterface
    public interface InventoryClickHandler {
        void handleClick(Player player, int slot, ClickType clickType);
    }

    private final Map<UUID, InventoryClickHandler> activeGUIs;

    public GuiManager() {
        this.activeGUIs = new HashMap<>();
    }

    public void addActiveGUI(UUID playerUUID, InventoryClickHandler clickHandler) {
        activeGUIs.put(playerUUID, clickHandler);
    }

    public void removeActiveGUI(UUID playerUUID) {
        activeGUIs.remove(playerUUID);
    }

    public InventoryClickHandler getClickHandler(UUID playerUUID) {
        return activeGUIs.get(playerUUID);
    }

    public boolean hasActiveGUI(UUID playerUUID) {
        return activeGUIs.containsKey(playerUUID);
    }
}
