package com.cloudcheap.autologin.database;

import com.cloudcheap.autologin.AutoLoginPlugin;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseManager {
    private final AutoLoginPlugin plugin;
    private Connection connection;

    public DatabaseManager(AutoLoginPlugin plugin) {
        this.plugin = plugin;
    }

    public void connect() throws SQLException {
        String type = plugin.getConfig().getString("database.type", "sqlite").toLowerCase();
        
        if (type.equals("mysql")) {
            String host = plugin.getConfig().getString("database.mysql.host", "localhost");
            int port = plugin.getConfig().getInt("database.mysql.port", 3306);
            String database = plugin.getConfig().getString("database.mysql.database", "minecraft");
            String username = plugin.getConfig().getString("database.mysql.username", "root");
            String password = plugin.getConfig().getString("database.mysql.password", "");
            
            String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false";
            connection = DriverManager.getConnection(url, username, password);
        } else {
            // SQLite
            java.io.File file = new java.io.File(plugin.getDataFolder(), "sessions.db");
            // Đảm bảo thư mục dữ liệu tồn tại
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }
            String url = "jdbc:sqlite:" + file.getAbsolutePath();
            connection = DriverManager.getConnection(url);
        }
        
        createTables();
    }

    private void createTables() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS autologin_sessions (" +
                     "uuid VARCHAR(36) PRIMARY KEY, " +
                     "ip VARCHAR(45) NOT NULL, " +
                     "last_login BIGINT NOT NULL)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.execute();
        }
    }

    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateSession(String uuid, String ip) {
        plugin.getScheduler().runTaskAsync(() -> {
            // Tối ưu hóa SQLite Concurrency (Khóa kết nối để ghi tuần tự)
            synchronized (connection) {
                try {
                    if (hasSession(uuid)) {
                        String update = "UPDATE autologin_sessions SET ip = ?, last_login = ? WHERE uuid = ?";
                        try (PreparedStatement stmt = connection.prepareStatement(update)) {
                            stmt.setString(1, ip);
                            stmt.setLong(2, System.currentTimeMillis());
                            stmt.setString(3, uuid);
                            stmt.executeUpdate();
                        }
                    } else {
                        String insert = "INSERT INTO autologin_sessions (uuid, ip, last_login) VALUES (?, ?, ?)";
                        try (PreparedStatement stmt = connection.prepareStatement(insert)) {
                            stmt.setString(1, uuid);
                            stmt.setString(2, ip);
                            stmt.setLong(3, System.currentTimeMillis());
                            stmt.executeUpdate();
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public boolean hasSession(String uuid) throws SQLException {
        String sql = "SELECT 1 FROM autologin_sessions WHERE uuid = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, uuid);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public String getSavedIP(String uuid) throws SQLException {
        String sql = "SELECT ip FROM autologin_sessions WHERE uuid = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, uuid);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getString("ip");
            }
        }
        return null;
    }
}
