package de.MoritzMCC.listeners;

import de.MoritzMCC.database.MySQLHandler;
import de.MoritzMCC.database.SQLManager;
import de.MoritzMCC.friendrequests.FriendrequestHandler;
import de.MoritzMCC.friendsSysteme.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class JoinListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        MySQLHandler mySQLHandler = Main.getMySQLHandler();

        if (!mySQLHandler.isUUIDPresentAsync(uuid).join()) { // First join
            mySQLHandler.insertPlayerAsync(uuid);
            player.sendMessage(ChatColor.GREEN + "You have joined the server!");
            return;
        }

        List<UUID> friendListUuids = new ArrayList<>();

        SQLManager.getAllFriendsAsPlayerAsync(player.getUniqueId()).forEach(player1 -> {
            if (player1 != null) {
                friendListUuids.add(player1.getUniqueId());
            }
        });

        if (friendListUuids.isEmpty()) {
            player.sendMessage(ChatColor.AQUA + "You have no friends yet, invite some with /friend add [name]");
        } else {
            friendListUuids.forEach(uuidP -> {
                Player friendPlayer = Bukkit.getPlayer(uuidP);
                if (friendPlayer != null && friendPlayer.isOnline()) {
                    player.sendMessage(ChatColor.DARK_GRAY + "Your friend " + friendPlayer.getName() + " is now online");
                    friendPlayer.sendMessage(ChatColor.DARK_GRAY + "Your friend " + player.getName() + " is now online");
                }
            });
        }

        if (FriendrequestHandler.getOpenRequests().containsValue(uuid)) {
            OfflinePlayer requester = FriendrequestHandler.getPlayerWhoSendRequest(uuid);
            if (requester != null) {
                player.sendMessage(ChatColor.AQUA + requester.getName() + " has sent you a friend request.");
                FriendrequestHandler.sendFriendRequest(requester.getUniqueId(), uuid);
            }
        }
    }
}
