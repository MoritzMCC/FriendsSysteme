package de.MoritzMCC.friendsSysteme;

import de.MoritzMCC.commands.FriendRequestCommand;
import de.MoritzMCC.commands.FriendCommand;
import de.MoritzMCC.database.MySQLHandler;
import de.MoritzMCC.database.SQLManager;
import de.MoritzMCC.friendrequests.FriendrequestHandler;
import de.MoritzMCC.listeners.ChatListener;
import de.MoritzMCC.listeners.JoinListener;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class Main extends JavaPlugin {
    private static Plugin instance;
    private static MySQLHandler mysqlHandler;
    private static SQLManager sqlManager;

    @Override
    public void onEnable() {
        instance = this;
        mysqlHandler = new MySQLHandler(connectToDatabase());
        sqlManager = new SQLManager();

        this.getLogger().info("§5FriendsSystem §7has been §aENABLED§7!");

        registerCommand("friend", new FriendCommand());
        registerCommand("friendrequest", new FriendRequestCommand());

        registerListener(new JoinListener());
        registerListener(new ChatListener());
    }

    private void registerCommand(String command, CommandExecutor executor) {
        this.getCommand(command).setExecutor(executor);
    }

    private void registerListener(Listener listener) {
        this.getServer().getPluginManager().registerEvents(listener, this);
    }

    @Override
    public void onDisable() {
        this.getLogger().info("§5FriendsSystem §7has been §cDISABLED§7!");
        mysqlHandler.close();
    }

    private Connection connectToDatabase() {
        final String CONFIG_FILE = "config.properties";
         Properties properties = new Properties();

         try (FileInputStream fis = new FileInputStream(CONFIG_FILE)) {
                properties.load(fis);
            } catch (IOException e) {
                e.printStackTrace();
            }

        try {
            String dbUrl = properties.getProperty("db.url");
            String user = properties.getProperty("db.user");
            String password = properties.getProperty("db.password");
            return DriverManager.getConnection(dbUrl, user, password);
        } catch (SQLException e) {
            getLogger().severe("Could not connect to database!");
            e.printStackTrace();
            return null;
        }
    }


    public static Plugin getInstance() {
        return instance;
    }

    public static MySQLHandler getMySQLHandler() {
        return mysqlHandler;
    }

    public static SQLManager getSqlManager() {
        return sqlManager;
    }
}
