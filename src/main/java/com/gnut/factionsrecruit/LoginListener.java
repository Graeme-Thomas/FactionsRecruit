package com.gnut.factionsrecruit;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.gnut.factionsrecruit.model.LoginNotification;

public class LoginListener implements Listener {

    private final FactionsRecruit plugin;

    public LoginListener(FactionsRecruit plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        LoginNotification notification = plugin.getDatabaseManager().getLoginNotification(player.getUniqueId());

        if (notification != null) {
            if (notification.hasExpiredApplications()) {
                player.sendMessage( plugin.getConfigManager().getExpiredApplicationsMessage());
            }
            if (notification.hasNewInvitations()) {
                player.sendMessage( plugin.getConfigManager().getNewInvitationsMessage());
            }
            if (notification.hasAvailableSlots()) {
                int availableSlots = plugin.getDatabaseManager().getAvailableApplicationSlots(player.getUniqueId(), plugin.getConfigManager().getApplicationSlotsPerPlayer());
                if (availableSlots > 0) {
                    player.sendMessage( plugin.getConfigManager().getAvailableSlotsMessage().replace("%count%", String.valueOf(availableSlots)));
                }
            }
            if (notification.hasNewApplications()) {
                player.sendMessage( "You have new applications to your faction!");
            }
            if (notification.hasAcceptedApplications()) {
                player.sendMessage( "One of your applications has been accepted!");
            }
            if (notification.hasRejectedApplications()) {
                player.sendMessage( "One of your applications has been rejected.");
            }

            // Reset notifications
            notification.setHasExpiredApplications(false);
            notification.setHasNewInvitations(false);
            notification.setHasAvailableSlots(false);
            notification.setHasNewApplications(false);
            notification.setHasAcceptedApplications(false);
            notification.setHasRejectedApplications(false);
            notification.setLastChecked(System.currentTimeMillis());
            plugin.getDatabaseManager().saveLoginNotification(notification);
        }
    }
}