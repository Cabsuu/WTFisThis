package org.jerae.a1;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jerae.a2.A2API;
import org.jerae.a3.A3API;

public class Commands implements CommandExecutor {

    private final A1 plugin;

    public Commands(A1 plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (command.getName().equalsIgnoreCase("nickname") || command.getName().equalsIgnoreCase("nick")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Only players can use this command.");
                return true;
            }

            if (!player.hasPermission("a1.nick")) {
                sendMessage(player, "no-permission");
                return true;
            }

            if (args.length == 0) {
                player.sendMessage("Usage: /nickname [player] <displayName | -reset>");
                return true;
            }

            Player target = player;
            String arg;

            if (args.length == 2) {
                if (!player.hasPermission("a1.nick.others")) {
                    sendMessage(player, "no-permission");
                    return true;
                }
                target = Bukkit.getPlayer(args[0]);
                if (target == null) {
                    player.sendMessage("Player not found.");
                    return true;
                }
                arg = args[1];
            } else {
                arg = args[0];
            }

            if (arg.equalsIgnoreCase("-reset") || arg.equalsIgnoreCase("-r")) {
                plugin.getDataManager().setNickname(target.getUniqueId(), null);
                target.displayName(target.name());
                if (target.equals(player)) {
                    sendMessage(player, "nickname-reset");
                } else {
                    sendMessage(player, "nickname-reset-other");
                }
                return true;
            }

            boolean hasColor = player.hasPermission("a1.nick.color");
            boolean hasFormat = player.hasPermission("a1.nick.format");
            boolean hasObfuscated = player.hasPermission("a1.nick.obfuscated");
            boolean hasRgb = player.hasPermission("a1.nick.rgb");
            boolean hasGradient = player.hasPermission("a1.nick.gradient");

            // Format nickname via A2 API using player's permissions
            target.displayName(A2API.format(arg, hasColor, hasFormat, hasObfuscated, hasRgb, hasGradient));
            plugin.getDataManager().setNickname(target.getUniqueId(), arg);

            if (target.equals(player)) {
                sendMessage(player, "nickname-set");
            } else {
                sendMessage(player, "nickname-set-other");
            }

            return true;
        }

        if (command.getName().equalsIgnoreCase("a1")) {
            if (args.length == 0) {
                sender.sendMessage("Usage: /a1 <version|reload>");
                return true;
            }

            if (args[0].equalsIgnoreCase("version")) {
                if (sender instanceof Player player) {
                    sendMessage(player, "version-message");
                } else {
                    String msg = plugin.getConfigManager().getMessages().getString("version-message", "&bA1 Version: %a1_version%");
                    msg = msg.replace("%a1_version%", plugin.getPluginMeta().getVersion());
                    sender.sendMessage(msg);
                }
                return true;
            }

            if (args[0].equalsIgnoreCase("reload")) {
                if (!sender.hasPermission("a1.reload")) {
                    if (sender instanceof Player player) {
                        sendMessage(player, "no-permission");
                    } else {
                        sender.sendMessage("No permission.");
                    }
                    return true;
                }
                plugin.getConfigManager().reload();
                if (sender instanceof Player player) {
                    sendMessage(player, "plugin-reloaded");
                } else {
                    sender.sendMessage("Plugin successfully reloaded.");
                }
                return true;
            }
        }

        return false;
    }

    private void sendMessage(Player player, String path) {
        String msg = plugin.getConfigManager().getMessages().getString(path);
        if (msg != null && !msg.isEmpty()) {
            player.sendMessage(A3API.parse(player, msg));
        }
    }
}
