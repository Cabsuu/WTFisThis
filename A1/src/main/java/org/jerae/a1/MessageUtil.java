package org.jerae.a1;

import org.bukkit.entity.Player;
import org.jerae.a3.A3API;

public class MessageUtil {

    public static void sendMessage(A1 plugin, Player player, String path) {
        String msg = plugin.getConfigManager().getMessages().getString(path);
        if (msg != null && !msg.isEmpty()) {
            player.sendMessage(A3API.parse(player, msg));
        }
    }
}
