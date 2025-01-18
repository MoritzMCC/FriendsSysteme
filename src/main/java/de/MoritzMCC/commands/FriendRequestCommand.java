package de.MoritzMCC.commands;

import de.MoritzMCC.friendrequests.FriendrequestHandler;
import de.MoritzMCC.friendsSysteme.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class FriendRequestCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage("You must be a player to use this command!");
            return false;
        }

        Player player = (Player) commandSender;
        if (strings.length != 2) {
            player.sendMessage("You must specify a friend request to accept or decline!");
            return false;
        }

        String targetPlayerName = strings[1];
        UUID senderUUID = player.getUniqueId();
        Player targetPlayer = Bukkit.getPlayer(targetPlayerName);

        if (targetPlayer == null){
            targetPlayer = Main.getInstance().getServer().getOfflinePlayer(targetPlayerName).getPlayer();
        }

        if (targetPlayer == null) {
            player.sendMessage("The specified player has never joined the server.");
            return false;
        }

        UUID targetUUID = targetPlayer.getUniqueId();
        if (targetUUID == null) {
            player.sendMessage("The specified player is not online.");
            return false;
        }

        if (!FriendrequestHandler.getOpenRequests().containsValue(senderUUID)) {
            player.sendMessage("You have no open requests!");
            return false;
        }

        String request = targetUUID + "." + senderUUID;

        if (!FriendrequestHandler.getOpenRequests().containsKey(request)) {
            player.sendMessage("You have no open requests by that player!");
            return false;
        }

        if (strings[0].equalsIgnoreCase("accept")) {
            if (strings[1] == "all"){
                FriendrequestHandler.acceptAllFriendRequest(senderUUID);
                return true;
            }

            FriendrequestHandler.acceptFriendRequest(senderUUID, targetUUID);
            return true;
        }

        if (strings[0].equalsIgnoreCase("decline")) {
            if (strings[1] == "all"){
               FriendrequestHandler.declineAllFriendRequest(senderUUID);
            }

            FriendrequestHandler.removeOpenRequest(senderUUID, targetUUID);
            return true;
        }

        player.sendMessage("You have to use this command: /friendrequest [playername] [accept|decline]");
        return false;
    }
}
