package org.jerae.a5;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TeleportCommands implements CommandExecutor {

    private final A5 plugin;

    public TeleportCommands(A5 plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("tpa")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Only players can use this command.");
                return true;
            }
            if (!player.hasPermission("a5.tpa")) {
                A5API.sendMessage(player, "no-permission");
                return true;
            }
            if (args.length == 0) {
                player.sendMessage("Usage: /tpa <player>");
                return true;
            }
            Player target = plugin.getServer().getPlayer(args[0]);
            if (target == null || !target.isOnline()) {
                A5API.sendMessage(player, "player-not-found");
                return true;
            }

            plugin.getTpaManager().addRequest(target.getUniqueId(), player.getUniqueId(), TpaManager.RequestType.TPA);

            A5API.sendMessageWithTarget(player, target, "tpa-request-sent");
            A5API.sendMessageWithTarget(target, player, "tpa-request-received");

            return true;
        }

        if (command.getName().equalsIgnoreCase("tpahere")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Only players can use this command.");
                return true;
            }
            if (!player.hasPermission("a5.tpahere")) {
                A5API.sendMessage(player, "no-permission");
                return true;
            }
            if (args.length == 0) {
                player.sendMessage("Usage: /tpahere <player>");
                return true;
            }
            Player target = plugin.getServer().getPlayer(args[0]);
            if (target == null || !target.isOnline()) {
                A5API.sendMessage(player, "player-not-found");
                return true;
            }

            plugin.getTpaManager().addRequest(target.getUniqueId(), player.getUniqueId(), TpaManager.RequestType.TPAHERE);

            A5API.sendMessageWithTarget(player, target, "tpahere-request-sent");
            A5API.sendMessageWithTarget(target, player, "tpahere-request-received");

            return true;
        }

        if (command.getName().equalsIgnoreCase("tpaall")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Only players can use this command.");
                return true;
            }
            if (!player.hasPermission("a5.tpaall")) {
                A5API.sendMessage(player, "no-permission");
                return true;
            }

            for (Player target : plugin.getServer().getOnlinePlayers()) {
                if (!target.equals(player)) {
                    plugin.getTpaManager().addRequest(target.getUniqueId(), player.getUniqueId(), TpaManager.RequestType.TPAHERE);
                    A5API.sendMessageWithTarget(target, player, "tpahere-request-received");
                }
            }

            A5API.sendMessage(player, "tpaall-request-sent");
            return true;
        }

        if (command.getName().equalsIgnoreCase("tpyes")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Only players can use this command.");
                return true;
            }

            TpaManager.TpaRequest request = plugin.getTpaManager().getRequest(player.getUniqueId());
            if (request == null) {
                A5API.sendMessage(player, "no-pending-request");
                return true;
            }

            Player requester = plugin.getServer().getPlayer(request.getRequester());
            if (requester == null || !requester.isOnline()) {
                A5API.sendMessage(player, "player-not-found");
                plugin.getTpaManager().removeRequest(player.getUniqueId());
                return true;
            }

            A5API.sendMessage(player, "tpa-accepted");
            A5API.sendMessage(requester, "tpa-accepted");
            A5API.sendMessage(player, "tpa-teleporting");
            A5API.sendMessage(requester, "tpa-teleporting");

            if (request.getType() == TpaManager.RequestType.TPA) {
                requester.teleportAsync(player.getLocation());
            } else if (request.getType() == TpaManager.RequestType.TPAHERE) {
                player.teleportAsync(requester.getLocation());
            }

            plugin.getTpaManager().removeRequest(player.getUniqueId());
            return true;
        }

        if (command.getName().equalsIgnoreCase("tpno")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Only players can use this command.");
                return true;
            }

            TpaManager.TpaRequest request = plugin.getTpaManager().getRequest(player.getUniqueId());
            if (request == null) {
                A5API.sendMessage(player, "no-pending-request");
                return true;
            }

            Player requester = plugin.getServer().getPlayer(request.getRequester());
            if (requester != null && requester.isOnline()) {
                A5API.sendMessage(requester, "tpa-denied");
            }
            A5API.sendMessage(player, "tpa-denied");

            plugin.getTpaManager().removeRequest(player.getUniqueId());
            return true;
        }

        return false;
    }
}
