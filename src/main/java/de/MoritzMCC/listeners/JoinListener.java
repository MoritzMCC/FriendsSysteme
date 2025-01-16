package de.MoritzMCC.listeners;

import de.MoritzMCC.database.SQLManager;
import de.MoritzMCC.friendrequests.FriendrequestHandler;
import de.MoritzMCC.friendsSysteme.Main;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.*;

public class JoinListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        List<Player> friends = SQLManager.getOnlineFriendsAsPlayer(player);
        if(!friends.isEmpty()) {
            player.sendMessage(ChatColor.AQUA + "You have no friends yet, invite some with /friend add [name]");
            return;
        }
        for(Player friend : friends) {
           friend.sendMessage(ChatColor.AQUA + "Your friend"+ player.getName() +"is online now");
        }
        if(FriendrequestHandler.getOpenRequests().containsValue(player)){

            FriendrequestHandler.sendFriendRequest(Objects.requireNonNull(FriendrequestHandler.getPlayerWhoSendRequest(player)), player);
        }
    }
}
