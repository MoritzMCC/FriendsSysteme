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
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage("You must be a player to use this command!");
            return false;
        }

        Player player = (Player) commandSender;
        UUID senderUUID = player.getUniqueId();

        if (args.length != 2) {
            player.sendMessage("You must specify a friend request to accept or decline!");
            return false;
        }

        String action = args[0];
        String targetPlayerName = args[1];
        UUID targetUUID = null;

        if (!targetPlayerName.equalsIgnoreCase("all")) {
            Player targetPlayer = Bukkit.getPlayer(targetPlayerName);
            if (targetPlayer != null) {
                targetUUID = targetPlayer.getUniqueId();
            } else {
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(targetPlayerName);
                if (offlinePlayer != null && offlinePlayer.hasPlayedBefore()) {
                    targetUUID = offlinePlayer.getUniqueId();
                }
            }

            if (targetUUID == null) {
                player.sendMessage("The specified player has never joined the server or is not online.");
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
        }

        if (action.equalsIgnoreCase("accept")) {
            if (targetPlayerName.equalsIgnoreCase("all")) {
                FriendrequestHandler.acceptAllFriendRequests(senderUUID);
                player.sendMessage("You have accepted all friend requests.");
                return true;
            }
            FriendrequestHandler.acceptFriendRequest(senderUUID, targetUUID);
            player.sendMessage("You have accepted the friend request from " + targetPlayerName);
            return true;
        }

        if (action.equalsIgnoreCase("decline")) {
            if (targetPlayerName.equalsIgnoreCase("all")) {
                FriendrequestHandler.declineAllFriendRequests(senderUUID);
                player.sendMessage("You have declined all friend requests.");
                return true;
            }
            FriendrequestHandler.removeOpenRequest(senderUUID, targetUUID);
            player.sendMessage("You have declined the friend request from " + targetPlayerName);
            return true;
        }

        player.sendMessage("You have to use this command: /friendrequest [accept|decline] [playername|all]");
        return false;
    }
}
