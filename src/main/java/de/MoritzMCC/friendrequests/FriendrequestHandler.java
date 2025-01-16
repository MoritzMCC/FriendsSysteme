package de.MoritzMCC.friendrequests;

import de.MoritzMCC.database.SQLManager;
import de.MoritzMCC.friendsSysteme.Main;
import net.md_5.bungee.api.chat.ClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import net.md_5.bungee.api.chat.TextComponent;

import java.awt.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class FriendrequestHandler {

    private static final HashMap<String, Player>openRequests = new HashMap<>(); //who ask , who get asked

    public static void removeOpenRequest(Player player, Player requestedFriend) {
        String request = player.getUniqueId().toString() + "." + requestedFriend.getUniqueId().toString();
        openRequests.remove(player);
    }
    public static void acceptFriendRequest(Player player, Player requestedFriend) {
      removeOpenRequest(player, requestedFriend);
        SQLManager.addFriend(player, requestedFriend.getName());
        SQLManager.addFriend(requestedFriend, player.getName());

    }

    public static void sendFriendRequest(Player player, Player requestedFriend) {
        TextComponent message = new TextComponent(ChatColor.DARK_AQUA + player.getName() + " wants to be your friend");
        TextComponent accept = new TextComponent(ChatColor.GREEN + "[accept] ");
        TextComponent decline = new TextComponent(ChatColor.GREEN + "[decline]");

        accept.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/friendrequest " + requestedFriend.getName() + " accept"));
        decline.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/friendrequest " + requestedFriend.getName() + " decline"));

        message.addExtra(accept);
        message.addExtra(decline);
        player.spigot().sendMessage(message);
    }

    public static HashMap<String, Player> getOpenRequests() {
        return openRequests;
    }

    public static void createFriendRequest(Player player, Player requestedFriend) {
        openRequests.put(player.getUniqueId().toString()+"."+ requestedFriend.getUniqueId().toString(), requestedFriend);
        if(Bukkit.getOnlinePlayers().contains(requestedFriend)) {
            sendFriendRequest(player, requestedFriend);
        }
    }

    public static Player getPlayerWhoSendRequest(Player targetPlayer){
        for (String request : openRequests.keySet()) {
           String s[] = request.split(". ");
           if(Objects.equals(s[1], targetPlayer.getUniqueId().toString())) {
               return Bukkit.getPlayer(s[0]);
           }
        }
        return null;
    }
}
