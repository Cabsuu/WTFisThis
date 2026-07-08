package org.jerae.a1;

import org.bukkit.entity.Player;
import org.jerae.a3.A3API;

public class MessageUtil {

    public static void sendMessage(A1 plugin, Player player, String path) {
        String msg = plugin.getConfigManager().getMessages().getString(path);
        if (msg != null && !msg.isEmpty()) {
            net.kyori.adventure.text.Component parsed = A3API.parse(player, msg);
            if (plugin.getServer().getPluginManager().getPlugin("A4") != null) {
                parsed = org.jerae.a4.A4API.parseTextholders(player, parsed, false, plugin.getConfigManager().getTextholders(), plugin.getLogger());
            }
            player.sendMessage(parsed);
        }
    }

    public static void sendMessageWithTarget(A1 plugin, Player player, Player target, String path) {
        String msg = plugin.getConfigManager().getMessages().getString(path);
        if (msg != null && !msg.isEmpty()) {
            msg = msg.replace("<player>", target.getName());
            net.kyori.adventure.text.Component parsed = A3API.parse(player, msg);
            if (plugin.getServer().getPluginManager().getPlugin("A4") != null) {
                parsed = org.jerae.a4.A4API.parseTextholders(player, parsed, false, plugin.getConfigManager().getTextholders(), plugin.getLogger());
            }
            player.sendMessage(parsed);
        }
    }
}
