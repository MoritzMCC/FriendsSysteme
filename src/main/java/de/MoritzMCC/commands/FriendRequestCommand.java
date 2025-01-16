package de.MoritzMCC.commands;

import de.MoritzMCC.friendrequests.FriendrequestHandler;
import de.MoritzMCC.friendsSysteme.Main;
import org.bukkit.Bukkit;
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

        String targetPlayerName = strings[0];
        OfflinePlayer offlinePlayer = Main.getInstance().getServer().getOfflinePlayer(targetPlayerName);
        if (offlinePlayer == null) {
            player.sendMessage("The specified player has never joined the server.");
            return false;
        }

        UUID offlineID = offlinePlayer.getUniqueId();
        if (offlineID == null) {
            player.sendMessage("The specified player is not online.");
            return false;
        }

        if (!FriendrequestHandler.getOpenRequests().containsValue(player.getUniqueId())) {
            player.sendMessage("You have no open requests!");
            return false;
        }

        String request = offlineID + "." + player.getUniqueId().toString();
        player.sendMessage(request);

        if (!FriendrequestHandler.getOpenRequests().containsKey(request)) {
            player.sendMessage("You have no open requests by that player!");
            player.sendMessage(Bukkit.getPlayer(UUID.fromString(request)).getName()); //entf
            return false;
        }

        if (strings[1].equalsIgnoreCase("accept")) {
            FriendrequestHandler.acceptFriendRequest(player.getUniqueId(),offlineID);
            return true;
        }

        if (strings[1].equalsIgnoreCase("decline")) {
            FriendrequestHandler.removeOpenRequest(player.getUniqueId(), offlineID);
            return true;
        }

        player.sendMessage("You have to use this command: /friendrequest [playername] [accept|decline]");
        return false;
    }
}
