package com.cloudcheap.autologin.listener;

import com.cloudcheap.autologin.AutoLoginPlugin;
import com.cloudcheap.autologin.database.DatabaseManager;
import fr.xephi.authme.events.LoginEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class AuthMeLoginListener implements Listener {
    private final AutoLoginPlugin plugin;
    private final DatabaseManager db;

    public AuthMeLoginListener(AutoLoginPlugin plugin, DatabaseManager db) {
        this.plugin = plugin;
        this.db = db;
    }

    @EventHandler
    public void onAuthMeLogin(LoginEvent event) {
        Player player = event.getPlayer();
        String uuid = player.getUniqueId().toString();
        String ip = player.getAddress().getAddress().getHostAddress();

        // Update session upon successful manual login
        db.updateSession(uuid, ip);
    }
}
