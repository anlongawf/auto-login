package com.cloudcheap.autologin;

import com.cloudcheap.autologin.database.DatabaseManager;
import com.cloudcheap.autologin.listener.AsyncPlayerPreLoginListener;
import com.cloudcheap.autologin.listener.AuthMeLoginListener;
import com.cloudcheap.autologin.listener.PlayerJoinListener;
import com.cloudcheap.autologin.scheduler.FoliaScheduler;
import com.cloudcheap.autologin.scheduler.PaperScheduler;
import com.cloudcheap.autologin.scheduler.TaskScheduler;
import com.cloudcheap.autologin.webhook.DiscordWebhookHelper;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class AutoLoginPlugin extends JavaPlugin {

    private TaskScheduler scheduler;
    private DatabaseManager databaseManager;
    private DiscordWebhookHelper webhookHelper;
    
    // Cache để tối ưu hóa tốc độ đăng nhập (Gold Standard)
    private final Map<UUID, Boolean> autoLoginCache = new ConcurrentHashMap<>();

    @Override
    public void onEnable() {
        // 1. Initialize compatible Scheduler
        if (isFolia()) {
            scheduler = new FoliaScheduler(this);
            getLogger().info("Folia environment detected! Activating FoliaScheduler.");
        } else {
            scheduler = new PaperScheduler(this);
            getLogger().info("Paper/Bukkit environment detected! Activating PaperScheduler.");
        }

        // 2. Save default config.yml
        saveDefaultConfig();

        // 3. Initialize Discord Webhook
        if (getConfig().getBoolean("discord.enabled", false)) {
            String webhookUrl = getConfig().getString("discord.webhook-url", "");
            if (!webhookUrl.isEmpty() && webhookUrl.startsWith("http")) {
                webhookHelper = new DiscordWebhookHelper(webhookUrl);
                getLogger().info("Discord Webhook notifications enabled.");
            }
        }

        // 4. Connect Database
        databaseManager = new DatabaseManager(this);
        try {
            databaseManager.connect();
            getLogger().info("Database connected successfully!");
        } catch (SQLException e) {
            getLogger().severe("Could not connect to database: " + e.getMessage());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // 5. Register Listeners
        // AsyncPreLogin xử lý query trước khi vào thế giới
        getServer().getPluginManager().registerEvents(new AsyncPlayerPreLoginListener(this, databaseManager), this);
        // PlayerJoin xử lý force login tức thời
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        
        if (getServer().getPluginManager().isPluginEnabled("AuthMe")) {
             getServer().getPluginManager().registerEvents(new AuthMeLoginListener(this, databaseManager), this);
             getLogger().info("Successfully connected to AuthMe API.");
        } else {
             getLogger().warning("AuthMe not found! AutoLogin requires AuthMe Reloaded for full functionality.");
        }

        getLogger().info("AutoLogin loaded successfully!");
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.disconnect();
        }
        autoLoginCache.clear();
        getLogger().info("AutoLogin disabled!");
    }

    public TaskScheduler getScheduler() {
        return scheduler;
    }

    public DiscordWebhookHelper getWebhookHelper() {
        return webhookHelper;
    }

    public Map<UUID, Boolean> getAutoLoginCache() {
        return autoLoginCache;
    }

    private boolean isFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionScheduler");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
