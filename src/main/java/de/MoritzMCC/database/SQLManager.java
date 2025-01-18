package de.MoritzMCC.database;

import de.MoritzMCC.friendsSysteme.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class SQLManager {

    public static void addFriend(UUID uuid, String friendUUID) {
        CompletableFuture.runAsync(() -> {
            OfflinePlayer offlinePlayer = getOfflinePlayerByUUID(uuid);
            Player player = offlinePlayer.getPlayer();
            if (sqlInjectionCheckFailedAsync(friendUUID)) {
                player.sendMessage(ChatColor.RED + "Invalid Input a Admin has been asserted");
                return;
            }

            List<String> friends = getUUIDFriendList(uuid).join();

            if (friends.contains(friendUUID)) {
                player.sendMessage(ChatColor.RED + "This player is already your friend.");
                return;
            }

            friends.add(friendUUID);
            String friendListString = String.join(",", friends);

            Main.getMySQLHandler().executeQuery("UPDATE players SET friends = ? WHERE uuid = ?;", friendListString, uuid.toString());
            player.sendMessage(ChatColor.GREEN + "You are now friends with " + getOfflinePlayerByUUID(UUID.fromString(friendUUID)).getName());
        });
    }

    public static void removeFriend(UUID uuid, String friendUUID) {
        CompletableFuture.runAsync(() -> {
            List<String> friends = getUUIDFriendList(uuid).join();
            Player player = getOfflinePlayerByUUID(uuid).getPlayer();
            if (friends.isEmpty()) {
                player.sendMessage(ChatColor.AQUA + "You have no friends yet add them with /friend add [name]");
                return;
            }

            if (!friends.contains(friendUUID)) {
                player.sendMessage(ChatColor.RED + "This player is not your friend.");
                return;
            }

            friends.remove(friendUUID);
            String friendListString = String.join(",", friends);

            Main.getMySQLHandler().executeQueryAsync("UPDATE players SET friends = ? WHERE uuid = ?;", friendListString, uuid.toString()).join();
            player.sendMessage(ChatColor.GREEN + "" + getOfflinePlayerByUUID(UUID.fromString(friendUUID)).getName() + " is not your friend any more.");
        });
    }

    public static CompletableFuture<List<String>> getUUIDFriendList(UUID playerUUID) {
        return CompletableFuture.supplyAsync(() -> {
            String friendsString = (String) Main.getMySQLHandler().getRowAsync("SELECT friends FROM players WHERE uuid = ? LIMIT 1;", playerUUID.toString())
                    .join()
                    .get("friends");

            if (friendsString == null || friendsString.isEmpty()) {
                return new ArrayList<>();
            }

            return Arrays.stream(friendsString.split(","))
                    .collect(Collectors.toList());
        });
    }

    public static CompletableFuture<List<Player>> getOnlineFriendsAsPlayerAsync(UUID playerUUID) {
        return getUUIDFriendList(playerUUID).thenApplyAsync(friends -> {
            List<Player> playerList = new ArrayList<>();
            for (String friend : friends) {
                UUID friendUUID = UUID.fromString(friend);
                Player player = Bukkit.getPlayer(friendUUID);
                if (player != null) {
                    playerList.add(player);
                }
            }
            return playerList;
        });
    }

    public static List<Player> getAllFriendsAsPlayerAsync(UUID playerUUID) {
        List<Player> playerList = new ArrayList<>();
        List<UUID> uuidList = new ArrayList<>();
        getUUIDFriendList(playerUUID).join().forEach(string -> {
            uuidList.add(UUID.fromString(string));
        });

        uuidList.forEach(uuid -> {
            for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
                if (offlinePlayer.getUniqueId().equals(uuid)) {
                    playerList.add(getOfflinePlayerByUUID(uuid).getPlayer());
                    break;
                }
            }
        });
        return playerList;
    }

    private static Boolean sqlInjectionCheckFailedAsync(String input) {
        for (String pattern : DANGEROUS_PATTERNS) {
            if (input.contains(pattern)) {
                return true;
            }
        }
        return false;
    }

    private static UUID getPlayerUUID(String playerName) {
        Player onlinePlayer = Bukkit.getPlayer(playerName);
        if (onlinePlayer != null) {
            return onlinePlayer.getUniqueId();
        }
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
        return offlinePlayer.getUniqueId();
    }

    private static final String[] DANGEROUS_PATTERNS = {
            "'", "\"", ";", "--", "#", "\\*", "\\/", "UNION", "SELECT", "INSERT",
            "UPDATE", "DELETE", "DROP", "AND", "OR", "=", "\\(", "\\)"
    };

    public static OfflinePlayer getOfflinePlayerByUUID(UUID uuid) {
        return Bukkit.getOfflinePlayer(uuid);
    }

}
