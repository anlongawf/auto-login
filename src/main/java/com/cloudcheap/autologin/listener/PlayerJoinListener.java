package com.cloudcheap.autologin.listener;

import com.cloudcheap.autologin.AutoLoginPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import fr.xephi.authme.api.v3.AuthMeApi;

public class PlayerJoinListener implements Listener {
    private final AutoLoginPlugin plugin;

    public PlayerJoinListener(AutoLoginPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        java.util.UUID uuid = player.getUniqueId();

        // Check Admin/Staff Bypass (Excluded for security)
        if (player.hasPermission("autologin.bypass")) {
            return; 
        }

        // Read from Concurrent Cache (Instant 0.00ms check)
        Boolean canAutoLogin = plugin.getAutoLoginCache().get(uuid);
        
        if (canAutoLogin != null && canAutoLogin) {
            String currentIP = player.getAddress().getAddress().getHostAddress();
            
            plugin.getScheduler().runTaskAtEntity(player, () -> {
                if (plugin.getServer().getPluginManager().isPluginEnabled("AuthMe")) {
                    AuthMeApi.getInstance().forceLogin(player);
                    player.sendMessage("§a[AutoLogin] Automatically logged in from IP: " + currentIP);
                    
                    if (plugin.getWebhookHelper() != null) {
                        plugin.getWebhookHelper().sendNotification(player.getName(), currentIP, true);
                    }
                }
            });
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Clear cache when player leaves to prevent memory leaks
        plugin.getAutoLoginCache().remove(event.getPlayer().getUniqueId());
    }
}
