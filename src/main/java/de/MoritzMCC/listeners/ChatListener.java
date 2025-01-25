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
        SQLManager sqlManager = Main.getSqlManager();

        List<String> friendsUUIDList = sqlManager.getUUIDFriendList(player.getUniqueId()).join();
        friendsUUIDList.forEach(uuid ->{
          boolean isFriend = false;

          for(Player p : event.getRecipients()){
              if(friendsUUIDList.contains(p.getUniqueId().toString())){
                  isFriend = true;
                  break;
              }
          }
          if(isFriend){
              event.setFormat(ChatColor.GOLD + "["+ player.getName() +"] " + ChatColor.RESET + ": " + message);
          }
        });

    }
}
