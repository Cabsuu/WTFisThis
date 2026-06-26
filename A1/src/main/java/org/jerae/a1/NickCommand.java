package org.jerae.a1;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jerae.a2.A2Api;
import org.jerae.a3.A3Api;

import java.io.File;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class NickCommand implements CommandExecutor {

    private final A1 plugin;

    public NickCommand(A1 plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("a1.nick")) {
            sendConfigMessage(sender, "no-permission", null);
            return true;
        }

        if (args.length == 0) {
            return false;
        }

        Player target = null;
        String nickInput = null;

        if (args.length == 1) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("You must specify a player.");
                return true;
            }
            target = (Player) sender;
            nickInput = args[0];
        } else if (args.length >= 2) {
            if (!sender.hasPermission("a1.nick.others")) {
                sendConfigMessage(sender, "no-permission", null);
                return true;
            }
            target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage("Player not found.");
                return true;
            }
            // reassemble nick string
            StringBuilder sb = new StringBuilder();
            for (int i = 1; i < args.length; i++) {
                sb.append(args[i]);
                if (i < args.length - 1) sb.append(" ");
            }
            nickInput = sb.toString();
        }

        if (nickInput == null) return false;

        if (nickInput.equalsIgnoreCase("-reset") || nickInput.equalsIgnoreCase("-r")) {
            plugin.getNickDataManager().removeNickname(target.getUniqueId());
            target.displayName(Component.text(target.getName()));

            if (target.equals(sender)) {
                sendConfigMessage(sender, "nick-reset", target);
            } else {
                sendConfigMessage(sender, "nick-other-reset", target);
            }
            return true;
        }

        // Check if player has right to use the colors they typed.
        // We will process the colors *now* and save the un-stripped string if valid,
        // OR we can save the raw string and evaluate permissions based on their CURRENT permissions.
        // Let's strip unauthorized colors *before* saving.

        // Temporarily serialize the processed string back to plain text with only allowed color tags?
        // Actually, just save the raw input. If they lack permissions, they just see their raw input.
        // Wait, if they lack permission now, and we save raw, they could gain permission later and it suddenly colors.
        // The instructions don't strictly specify.
        // Let's save the raw input.
        plugin.getNickDataManager().setNickname(target.getUniqueId(), nickInput);

        // Process nickname color using A2 Api but with A1 permissions
        Component formattedNick = A2Api.processColors(nickInput, target, "a1.nick");
        target.displayName(formattedNick);

        if (target.equals(sender)) {
            sendConfigMessage(sender, "nick-changed", target);
        } else {
            sendConfigMessage(sender, "nick-other-changed", target);
        }

        return true;
    }

    private void sendConfigMessage(CommandSender sender, String path, Player player) {
        File messagesFile = new File(plugin.getDataFolder(), "message.yml");
        if (messagesFile.exists()) {
            FileConfiguration config = YamlConfiguration.loadConfiguration(messagesFile);
            String msg = config.getString(path);
            if (msg != null && !msg.isEmpty()) {
                if (player != null) {
                    msg = A3Api.setPlaceholders(player, msg);
                }
                Component comp = A2Api.processConfigMessage(msg);
                sender.sendMessage(comp);
            }
        }
    }
}
