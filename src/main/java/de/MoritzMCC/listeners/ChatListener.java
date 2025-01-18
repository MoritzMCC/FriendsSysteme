package de.MoritzMCC.listeners;

import de.MoritzMCC.database.SQLManager;
import de.MoritzMCC.friendsSysteme.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.List;

public class ChatListener implements Listener {

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();


        List<String> friendsUUIDList = SQLManager.getUUIDFriendList(player.getUniqueId()).join();
        friendsUUIDList.forEach(uuid ->{
            for(Player p : event.getRecipients()) {
                if (p.getUniqueId().toString().equals(uuid)) {
                    event.setFormat(ChatColor.GOLD + "[" + player.getName() + "]:" + ChatColor.RESET + message);
                    break;
                }
            }
        });

    }
}
