package de.MoritzMCC.friendrequests;

import de.MoritzMCC.database.SQLManager;
import de.MoritzMCC.friendsSysteme.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public class FriendrequestHandler {

    private static final HashMap<String, UUID> openRequests = new HashMap<>(); // UUID.UUIDWho got asked and UUID who got asked

    public static void removeOpenRequest(UUID playerUUID, UUID requestedFriendUUID) {
        String request = playerUUID.toString() + "." + requestedFriendUUID.toString();
        openRequests.remove(request);
    }

    public static void acceptFriendRequest(UUID playerId, UUID requestedFriendUUID) {
        removeOpenRequest(playerId, requestedFriendUUID);
        SQLManager.addFriend(playerId, requestedFriendUUID.toString());
        SQLManager.addFriend(requestedFriendUUID, playerId.toString());
    }

    public static void sendFriendRequest(Player player, Player requestedFriend) {
        TextComponent message = new TextComponent(ChatColor.DARK_AQUA + player.getName() + " wants to be your friend");
        TextComponent accept = new TextComponent(ChatColor.GREEN + "[accept] ");
        TextComponent decline = new TextComponent(ChatColor.RED + "[decline]");

        accept.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/friendrequest  accept " + player.getName()));
        decline.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/friendrequest decline " + player.getName()));

        message.addExtra(accept);
        message.addExtra(decline);
        requestedFriend.spigot().sendMessage(message);
    }

    public static HashMap<String, UUID> getOpenRequests() {
        return openRequests;
    }

    public static void createFriendRequest(UUID playerUUID, UUID requestedFriendUUID) {
        OfflinePlayer offlinePlayer =  Main.getInstance().getServer().getOfflinePlayer(requestedFriendUUID);
        Player player = Bukkit.getPlayer(playerUUID);
        if (offlinePlayer == null) {
            player.sendMessage(ChatColor.RED + "Error: Your friend has never joined this server!.");
            return;
        }

        openRequests.put(playerUUID.toString() + "." + requestedFriendUUID.toString(), requestedFriendUUID);

        if (offlinePlayer.isOnline()) {
            sendFriendRequest(player, offlinePlayer.getPlayer());
        }
    }

    public static Player getPlayerWhoSendRequest(Player targetPlayer) {
        for (String request : openRequests.keySet()) {
            String[] s = request.split("\\.");
            if (Objects.equals(s[1], targetPlayer.getUniqueId().toString())) {
                return Bukkit.getPlayer(UUID.fromString(s[0]));
            }
        }
        return null;
    }
}
