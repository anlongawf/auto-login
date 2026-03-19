package com.cloudcheap.autologin.listener;

import com.cloudcheap.autologin.AutoLoginPlugin;
import com.cloudcheap.autologin.database.DatabaseManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import java.sql.SQLException;
import java.util.UUID;

public class AsyncPlayerPreLoginListener implements Listener {
    private final AutoLoginPlugin plugin;
    private final DatabaseManager db;

    public AsyncPlayerPreLoginListener(AutoLoginPlugin plugin, DatabaseManager db) {
        this.plugin = plugin;
        this.db = db;
    }

    @EventHandler
    public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        UUID uuid = event.getUniqueId();
        String currentIP = event.getAddress().getHostAddress();

        try {
            String savedIP = db.getSavedIP(uuid.toString());
            if (savedIP != null && savedIP.equals(currentIP)) {
                // Qualified for AutoLogin -> Store in fast memory Cache
                plugin.getAutoLoginCache().put(uuid, true);
            } else {
                plugin.getAutoLoginCache().put(uuid, false);
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Error querying DB during pre-login for " + event.getName() + ": " + e.getMessage());
        }
    }
}
