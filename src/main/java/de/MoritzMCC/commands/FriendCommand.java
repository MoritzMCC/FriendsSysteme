package de.MoritzMCC.commands;

import de.MoritzMCC.database.SQLManager;
import de.MoritzMCC.friendrequests.FriendrequestHandler;
import de.MoritzMCC.friendsSysteme.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class FriendCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if(!(commandSender instanceof Player)){
            commandSender.sendMessage(ChatColor.RED + "You must be a player to use this command!");
            return true;
        }
        Player player = (Player) commandSender;
        if(strings.length == 0){
            player.sendMessage("/friend list");
            player.sendMessage("/friend add [player name]");
            player.sendMessage("/friend remove [player name]");
            return false;
        }
        if(strings[0].equalsIgnoreCase("list")){
            player.sendMessage(ChatColor.GOLD + "Friends:");
            List<Player> friends = SQLManager.getAllFriendsAsPlayerAsync(player.getUniqueId());

            if(friends.isEmpty() || friends == null){
                player.sendMessage(ChatColor.AQUA + "You have no friends yet, invite some with /friend add [name]");
                return false;
            }
            for(Player friend : friends){
                player.sendMessage(ChatColor.GOLD + friend.getName());
            }
            player.sendMessage(ChatColor.GOLD + " ---------------");
            return false;
        }

        Player targetPlayer = Bukkit.getPlayer(strings[1]);
        UUID targetID = null;
        if (targetPlayer != null) {
            targetID = targetPlayer.getUniqueId();
        } else {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(strings[1]);
            if (offlinePlayer != null && offlinePlayer.hasPlayedBefore()) {
                targetID = offlinePlayer.getUniqueId();
            }
        }
        if (targetID == null){
            player.sendMessage(ChatColor.RED + "The Player you searching for has never joined this server!");
            return false;
        }

        if(strings[0].equalsIgnoreCase("add")){
            if (strings.length != 2){
                player.sendMessage(ChatColor.RED + "You must specify a player name!");
                return false;
            }

            if (SQLManager.getUUIDFriendList(player.getUniqueId()).join().contains(targetID.toString()) ){
                player.sendMessage(ChatColor.RED + "You already have a friend with that name!");
                return false;
            }

            FriendrequestHandler.createFriendRequest(player.getUniqueId(), targetID);
            player.sendMessage(ChatColor.DARK_GREEN + "You invited " + strings[1] + " to be your friend!");
            return false;

        }
        if(strings[0].equalsIgnoreCase("remove")){
            if (strings.length != 2){
                player.sendMessage(ChatColor.RED + "You must specify a player name!");
                return false;
            }
           SQLManager.removeFriend(player.getUniqueId() , targetID.toString());
            SQLManager.removeFriend(targetID, player.getUniqueId().toString());
        }
        return false;
    }
}
