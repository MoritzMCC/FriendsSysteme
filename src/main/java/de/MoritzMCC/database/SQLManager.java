package de.MoritzMCC.database;

import de.MoritzMCC.friendsSysteme.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class SQLManager {

    public static void addFriend(Player player, String friendName){
        UUID uuid = player.getUniqueId();
        player.getUniqueId(); UUID targetId = Bukkit.getPlayer(friendName) != null ? Bukkit.getPlayer(friendName).getUniqueId() : Bukkit.getOfflinePlayer(friendName).getUniqueId();
        List<String> friends = getFriendList(player);
        if (friends.contains(targetId.toString())) {
            player.sendMessage(ChatColor.RED + "You are already friends with this player.");
            return;
        }
        friends.add(targetId.toString());
        String friendListString = String.join(", ", friends);
        Main.getMySQLHandler().executeQuery("UPDATE players SET friends = '" + friendListString + "' WHERE uuid = '" + uuid + "';");
        player.sendMessage(ChatColor.GREEN + "You are now friends with " + friendName + "!");
    }
    public static void removeFriend(Player player, String friendName){
        UUID uuid = player.getUniqueId();
        UUID targetId = Bukkit.getPlayer(friendName) != null ? Bukkit.getPlayer(friendName).getUniqueId() : Bukkit.getOfflinePlayer(friendName).getUniqueId();
        List<String> friends = getFriendList(player);
        if (friends.isEmpty()){
            player.sendMessage(ChatColor.AQUA + "You have no friends yet, invite some with /friend add [name]");
            return;
        }
        if(!friends.contains(targetId.toString())){
            player.sendMessage(ChatColor.RED + "You are not friends with this player");
            return;
        }
        friends.remove(targetId.toString());
        String friendListString = String.join(", ", friends);
        Main.getMySQLHandler().executeQuery("UPDATE players SET friends = '" + friendListString + "' WHERE uuid = '" + uuid + "';");
        player.sendMessage(ChatColor.GREEN + "You are no longer friends with " + friendName + ".");
    }

    public static List<String> getFriendList(Player player) {
        String friendsString = Main.getMySQLHandler().getRow("SELECT friends FROM players WHERE uuid = '" + player.getUniqueId() + "' LIMIT 1;").get("friends").toString();
        if (friendsString.isEmpty()) {
            return Arrays.asList();
        }
        return Arrays.stream(friendsString.split(", ")).collect(Collectors.toList());
    }

    public static List<Player> getFriendsAsPlayer(Player mainPlayer ) {
        List<Player> playerList = new ArrayList<>();
        playerList.addAll(getOnlineFriendsAsPlayer(mainPlayer));
        playerList.addAll(getOfflineFriendsAsPlayer(mainPlayer));
        return playerList;
    }

    public static List<Player> getOnlineFriendsAsPlayer(Player mainPlayer) {
        List<Player> playerList = new ArrayList<>();
        for (String friend : getFriendList(mainPlayer)) {
            UUID friendUUID = UUID.fromString(friend);
            Player onlinePlayer = Bukkit.getPlayer(friendUUID);
            if (onlinePlayer != null && onlinePlayer.isOnline()) {
                playerList.add(onlinePlayer);
            }
        }
        return playerList;
    }

    public static List<Player> getOfflineFriendsAsPlayer(Player mainPlayer) {
        List<Player> playerList = new ArrayList<>();
        for (String friend : getFriendList(mainPlayer)) {
            UUID friendUUID = UUID.fromString(friend);
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(friendUUID);
            if (offlinePlayer.hasPlayedBefore()) {
                Player player = offlinePlayer.getPlayer();
                if (player != null) { playerList.add(player);
                }
            }
        }
        return playerList;
    }
}
