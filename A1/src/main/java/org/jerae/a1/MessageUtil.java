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

    public static void sendMessageWithPlaceholders(A1 plugin, Player player, String path, java.util.Map<String, String> placeholders) {
        String msg = plugin.getConfigManager().getMessages().getString(path);
        if (msg != null && !msg.isEmpty()) {
            net.kyori.adventure.text.Component parsed = A3API.parse(player, msg, placeholders);
            if (plugin.getServer().getPluginManager().getPlugin("A4") != null) {
                parsed = org.jerae.a4.A4API.parseTextholders(player, parsed, false, plugin.getConfigManager().getTextholders(), plugin.getLogger());
            }
            player.sendMessage(parsed);
        }
    }

    public static void sendMessageWithArgs(A1 plugin, Player player, String path, String target, String replacement) {
        String msg = plugin.getConfigManager().getMessages().getString(path);
        if (msg != null && !msg.isEmpty()) {
            java.util.Map<String, String> extraArgs = new java.util.HashMap<>();
            extraArgs.put(target, replacement);
            net.kyori.adventure.text.Component parsed = A3API.parse(player, msg, extraArgs);
            if (plugin.getServer().getPluginManager().getPlugin("A4") != null) {
                parsed = org.jerae.a4.A4API.parseTextholders(player, parsed, false, plugin.getConfigManager().getTextholders(), plugin.getLogger());
            }
            player.sendMessage(parsed);
        }
    }

    public static void sendMessageWithTarget(A1 plugin, Player player, Player target, String path) {
        String msg = plugin.getConfigManager().getMessages().getString(path);
        if (msg != null && !msg.isEmpty()) {
            java.util.Map<String, String> extraArgs = new java.util.HashMap<>();
            extraArgs.put("<player>", target.getName());
            net.kyori.adventure.text.Component parsed = A3API.parse(player, msg, extraArgs);
            if (plugin.getServer().getPluginManager().getPlugin("A4") != null) {
                parsed = org.jerae.a4.A4API.parseTextholders(player, parsed, false, plugin.getConfigManager().getTextholders(), plugin.getLogger());
            }
            player.sendMessage(parsed);
        }
    }
}
