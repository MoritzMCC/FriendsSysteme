package de.MoritzMCC.friendrequests;

import de.MoritzMCC.database.SQLManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class FriendrequestHandler {

    private static final HashMap<String, UUID> openRequests = new HashMap<>(); // UUID.UUIDWho got asked and UUID who got asked

    public static void removeOpenRequest(UUID playerUUID, UUID requestedFriendUUID) {
        String request = playerUUID.toString() + "." + requestedFriendUUID.toString();
        openRequests.remove(request);
        Bukkit.getPlayer(playerUUID).sendMessage(Bukkit.getOfflinePlayer(requestedFriendUUID).getName());
    }

    public static void acceptFriendRequest(UUID playerId, UUID requestedFriendUUID) {
        removeOpenRequest(playerId, requestedFriendUUID);
        SQLManager.addFriend(playerId, requestedFriendUUID.toString());
        SQLManager.addFriend(requestedFriendUUID, playerId.toString());
    }

    public static void sendFriendRequest(UUID playerUUID, UUID requestedFriendUUID) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerUUID);
        OfflinePlayer requester = Bukkit.getOfflinePlayer(requestedFriendUUID);

        TextComponent message = new TextComponent(ChatColor.DARK_AQUA + player.getName() + " wants to be your friend");
        TextComponent accept = new TextComponent(ChatColor.GREEN + "[accept] ");
        TextComponent decline = new TextComponent(ChatColor.RED + "[decline]");

        accept.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/friendrequest accept " + player.getName()));
        decline.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/friendrequest decline " + player.getName()));

        message.addExtra(accept);
        message.addExtra(decline);
        requester.getPlayer().spigot().sendMessage(message);
    }

    public static HashMap<String, UUID> getOpenRequests() {
        return openRequests;
    }

    public static void createFriendRequest(UUID playerUUID, UUID requestedFriendUUID) {
        OfflinePlayer requestedOfflinePlayer =  Bukkit.getOfflinePlayer(requestedFriendUUID);
        Player player = Bukkit.getPlayer(playerUUID);
        if (requestedOfflinePlayer == null) {
            player.sendMessage(ChatColor.RED + "Error: Your friend has never joined this server!.");
            return;
        }

        openRequests.put(playerUUID.toString() + "." + requestedFriendUUID.toString(), requestedFriendUUID);

        if (requestedOfflinePlayer.isOnline()) {
            sendFriendRequest(playerUUID, requestedFriendUUID);
        }
    }

    public static Player getPlayerWhoSendRequest(UUID targetPlayerUUID) {
        for (String request : openRequests.keySet()) {
            String[] s = request.split("\\.");
            if (Objects.equals(s[1], targetPlayerUUID.toString())) {
                return Bukkit.getPlayer(UUID.fromString(s[0]));
            }
        }
        return null;
    }

    public static void acceptAllFriendRequest(UUID playerUUID) {

       List<UUID> openRequestList = openRequests.values().stream().toList();

       openRequestList.forEach(openRequest -> {
           if (openRequest.equals(playerUUID)) {
               acceptFriendRequest(playerUUID, getPlayerWhoSendRequest(playerUUID).getUniqueId());
           }
       });
    }

    public static void declineAllFriendRequest(UUID playerUUID) {
        List<UUID> openRequestList = openRequests.values().stream().toList();

        openRequestList.forEach(openRequest -> {
            if (openRequest.equals(playerUUID)) {
                removeOpenRequest(playerUUID, getPlayerWhoSendRequest(playerUUID).getUniqueId());
            }
        });
    }
}
