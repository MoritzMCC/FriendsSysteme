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

    public static void addFriend(Player player, String friendName) {
        CompletableFuture.runAsync(() -> {
            if (sqlInjectionCheckFailedAsync(friendName)) {
                player.sendMessage(ChatColor.RED + "Ungültiger Name. Ein Admin wurde benachrichtigt.");
                return;
            }

            UUID uuid = player.getUniqueId();
            UUID targetId = getPlayerUUID(friendName);

            // Freunde aus der Datenbank laden
            List<String> friends = getFriendListAsync(player).join();

            if (friends.contains(targetId.toString())) {
                player.sendMessage(ChatColor.RED + "Du bist bereits mit diesem Spieler befreundet.");
                return;
            }

            friends.add(targetId.toString());
            String friendListString = String.join(", ", friends);

            Main.getMySQLHandler().executeQueryAsync("UPDATE players SET friends = '" + friendListString + "' WHERE uuid = '" + uuid + "';").join();
            player.sendMessage(ChatColor.GREEN + "Du bist jetzt mit " + friendName + " befreundet!");
        });
    }

    public static void removeFriend(Player player, String friendName) {
        CompletableFuture.runAsync(() -> {
            UUID uuid = player.getUniqueId();
            UUID targetId = getPlayerUUID(friendName);

            List<String> friends = getFriendListAsync(player).join();

            if (friends.isEmpty()) {
                player.sendMessage(ChatColor.AQUA + "Du hast noch keine Freunde. Füge mit /friend add [name] einen hinzu.");
                return;
            }

            if (!friends.contains(targetId.toString())) {
                player.sendMessage(ChatColor.RED + "Du bist nicht mit diesem Spieler befreundet.");
                return;
            }

            friends.remove(targetId.toString());
            String friendListString = String.join(", ", friends);

            Main.getMySQLHandler().executeQueryAsync("UPDATE players SET friends = '" + friendListString + "' WHERE uuid = '" + uuid + "';").join();
            player.sendMessage(ChatColor.GREEN + "Du bist jetzt nicht mehr mit " + friendName + " befreundet.");
        });
    }

    public static CompletableFuture<List<String>> getFriendListAsync(Player player) {
        return CompletableFuture.supplyAsync(() -> {
            String friendsString = Main.getMySQLHandler().getRowAsync("SELECT friends FROM players WHERE uuid = '" + player.getUniqueId() + "' LIMIT 1;")
                    .join()
                    .get("friends")
                    .toString();

            if (friendsString.isEmpty()) {
                return new ArrayList<>();
            }

            return Arrays.stream(friendsString.split(", "))
                    .collect(Collectors.toList());
        });
    }

    public static CompletableFuture<List<Player>> getFriendsAsPlayerAsync(Player mainPlayer) {
        return getFriendListAsync(mainPlayer).thenApplyAsync(friends -> {
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


}

