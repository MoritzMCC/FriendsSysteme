package de.MoritzMCC.friendsSysteme;

import de.MoritzMCC.commands.FriendRequestCommand;
import de.MoritzMCC.commands.FriendCommand;
import de.MoritzMCC.database.MySQLHandler;
import de.MoritzMCC.listeners.JoinListener;
import org.bukkit.command.CommandExecutor;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class Main extends JavaPlugin {
    private static Plugin instance;
    private static MySQLHandler mysqlHandler;

    @Override
    public void onEnable() {
        instance = this;
        mysqlHandler = new MySQLHandler(connectToDatabase("localhost", 3306, "friendsSystem", "root", "asdf"));

        /* -- onEnable message -- */
        this.getLogger().info("§5FriendsSystem §7has been §aENABLED§7!");

        /* -- register commands -- */
        registerCommand("friend", new FriendCommand());
        registerCommand("friendrequest", new FriendRequestCommand());

        /* -- register listener -- */
        registerListener(new JoinListener());
    }

    private void registerCommand(String command, CommandExecutor executor) {
        this.getCommand(command).setExecutor(executor);
    }

    private void registerListener(Listener listener) {
        this.getServer().getPluginManager().registerEvents(listener, this);
    }

    @Override
    public void onDisable() {
        /* -- onDisable message -- */
        this.getLogger().info("§5FriendsSystem §7has been §cDISABLED§7!");
    }

    private Connection connectToDatabase(String host, int port, String database, String user, String password) {
        try {
            String url = "jdbc:mysql://" + host + ":" + port + "/" + database;
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
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
}
