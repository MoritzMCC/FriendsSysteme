package de.MoritzMCC.listeners;

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

import java.util.*;

public class JoinListener implements Listener {
    SQLManager sqlManager = new SQLManager();

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        List<UUID> friendListUuids = sqlManager.getFriendListOfUuid(player.getUniqueId());

        if (friendListUuids == null) {
            player.sendMessage(ChatColor.AQUA + "You have no friends yet, invite some with /friend add [name]");
            return;
        }

        List<OfflinePlayer> friendList = new ArrayList<>();
        friendListUuids.forEach((uuid) -> {
            friendList.add(Main.getInstance().getServer().getOfflinePlayer(uuid));
        });

        friendListUuids.forEach((uuid) -> {
            Player friendPlayer = Bukkit.getPlayer(uuid);
            if (friendPlayer != null && friendPlayer.isOnline()) {
                player.sendMessage(ChatColor.AQUA + "Your friend " + friendPlayer.getName() + " is online now");
            }
        });

        if (FriendrequestHandler.getOpenRequests().containsValue(player.getUniqueId())) {
            Player requester = FriendrequestHandler.getPlayerWhoSendRequest(player);
            if (requester != null) {
                FriendrequestHandler.sendFriendRequest(player, requester);
            }
        }
    }
}
