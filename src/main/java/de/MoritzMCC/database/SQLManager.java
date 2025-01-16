package de.MoritzMCC.database;

import de.MoritzMCC.friendrequests.FriendrequestHandler;
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
                player.sendMessage(ChatColor.RED + "Ungültiger Name. Ein Admin wurde benachrichtigt.");
                return;
            }
            UUID targetId = convertStringToUUID(friendUUID);

            List<String> friends = getUUIDFriendList(uuid).join();

            if (friends.contains(targetId.toString())) {
                player.sendMessage(ChatColor.RED + "Du bist bereits mit diesem Spieler befreundet.");
                return;
            }

            friends.add(targetId.toString());
            String friendListString = String.join(", ", friends);

            Main.getMySQLHandler().executeQueryAsync("UPDATE players SET friends = ? WHERE uuid = ?;", friendListString, uuid.toString()).join();
            player.sendMessage(ChatColor.GREEN + "Du bist jetzt mit " + getOfflinePlayerByUUID(targetId) + " befreundet!");
        });
    }

    public static void removeFriend(UUID uuid, String friendUUID) {
        CompletableFuture.runAsync(() -> {

            UUID targetId = convertStringToUUID(friendUUID);

            List<String> friends = getUUIDFriendList(uuid).join();
            Player player = getOfflinePlayerByUUID(uuid).getPlayer();
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

            Main.getMySQLHandler().executeQueryAsync("UPDATE players SET friends = ? WHERE uuid = ?;", friendListString, uuid.toString()).join();
            player.sendMessage(ChatColor.GREEN + "Du bist jetzt nicht mehr mit " + getOfflinePlayerByUUID(targetId) + "befreundet.");
        });
    }

    public static CompletableFuture<List<String>> getUUIDFriendList(UUID playerUUD) {
        return CompletableFuture.supplyAsync(() -> {
            String friendsString = Main.getMySQLHandler().getRowAsync("SELECT friends FROM players WHERE uuid = ? LIMIT 1;", playerUUD.toString())
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
           uuidList.add( convertStringToUUID(string));
        });

        uuidList.forEach(uuid -> {
            for (OfflinePlayer offlinePlayer : Main.getInstance().getServer().getOfflinePlayers()) {
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

    public List<UUID> getFriendListOfUuid(UUID uuid) {
        String friendList = Main.getMySQLHandler().getRow("SELECT friends FROM players WHERE uuid = ? LIMIT 1;", uuid.toString()).get("friends").toString();
        return Arrays.stream(friendList.split(", "))
                .map(UUID::fromString)
                .collect(Collectors.toList());
    }

    public static OfflinePlayer getOfflinePlayerByUUID(UUID uuid) {
        return Main.getInstance().getServer().getOfflinePlayer(uuid.toString());
    }
    private static UUID convertStringToUUID(String string) {
        return UUID.fromString(string);
    }
}
