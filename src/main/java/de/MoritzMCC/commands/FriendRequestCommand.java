package de.MoritzMCC.commands;

import de.MoritzMCC.friendrequests.FriendrequestHandler;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FriendRequestCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage("You must be a player to use this command!");
            return false;
        }
        Player player = (Player) commandSender;
        if (!FriendrequestHandler.getOpenRequests().containsValue(player)) {
            player.sendMessage("You have no open requests!");
            return false;
        }
        if(strings.length !=2){
            player.sendMessage("You must specify a friend request to accept or decline!");
            return false;
        }
        Player target = Bukkit.getPlayer(strings[0]);
        if(!FriendrequestHandler.getOpenRequests().containsKey(target) && FriendrequestHandler.getOpenRequests().get(target) != player){
            player.sendMessage("You have no open requests by that player!");
            return false;
        }
        if (strings[1].equalsIgnoreCase("accept")) {
            FriendrequestHandler.acceptFriendRequest(player, target);
            return false;
        }
        if (strings[1].equalsIgnoreCase("decline")) {
            FriendrequestHandler.removeOpenRequest(player, target);
            return false;
        }
        player.sendMessage("You have to use this command: /friendrequest [playername] [accept|decline]");

        return false;
    }
}
