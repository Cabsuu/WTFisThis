package org.jerae.a1;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.inventory.meta.ItemMeta;
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
        if (command.getName().equalsIgnoreCase("nick")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Only players can use this command.");
                return true;
            }

            if (!player.hasPermission("a1.nick")) {
                MessageUtil.sendMessage(plugin, player, "no-permission");
                return true;
            }

            if (args.length == 0) {
                player.sendMessage("Usage: /nick [player] <displayName | -reset>");
                return true;
            }

            Player target = player;
            String arg;

            if (args.length == 2) {
                if (!player.hasPermission("a1.nick.others")) {
                    MessageUtil.sendMessage(plugin, player, "no-permission");
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
                    MessageUtil.sendMessage(plugin, player, "nickname-reset");
                } else {
                    MessageUtil.sendMessageWithTarget(plugin, player, target, "nickname-reset-other");
                }
                return true;
            }

            boolean hasColor = player.hasPermission("a1.nick.color");
            boolean hasFormat = player.hasPermission("a1.nick.format");
            boolean hasObfuscated = player.hasPermission("a1.nick.obfuscated");
            boolean hasRgb = player.hasPermission("a1.nick.rgb");
            boolean hasGradient = player.hasPermission("a1.nick.gradient");

            Component formatted = A2API.format(arg, hasColor, hasFormat, hasObfuscated, hasRgb, hasGradient);

            // Check for symbols
            String plainText = net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().stripTags(arg);
            // Replace old formatting codes explicitly if they weren't stripped
            plainText = plainText.replaceAll("(?i)&[0-9a-fk-or]", "");
            plainText = plainText.replaceAll("(?i)&x([0-9a-f]{6})", "");

            if (!plainText.matches("^[a-zA-Z0-9_]+$")) {
                if (!player.hasPermission("a1.nick.symbol")) {
                    MessageUtil.sendMessage(plugin, player, "no-permission");
                    return true;
                }
            }

            if (!player.hasPermission("a1.nick.bypasslimit")) {
                int limit = plugin.getConfigManager().getConfig().getInt("nick-character-limit", 16);
                if (plainText.length() > limit) {
                    player.sendMessage("Nickname is too long (limit: " + limit + ").");
                    return true;
                }
            }

            plugin.getDataManager().setNickname(target.getUniqueId(), arg);

            String finalNick = arg;
            if (!target.hasPermission("a1.nick.hideprefix")) {
                String prefix = plugin.getConfigManager().getConfig().getString("nickname-prefix", "*");
                finalNick = prefix + finalNick;
            }

            Component finalFormatted = A2API.format(finalNick, hasColor, hasFormat, hasObfuscated, hasRgb, hasGradient);
            target.displayName(finalFormatted);

            if (target.equals(player)) {
                MessageUtil.sendMessage(plugin, player, "nickname-set");
            } else {
                MessageUtil.sendMessageWithTarget(plugin, player, target, "nickname-set-other");
            }

            return true;
        }

        if (command.getName().equalsIgnoreCase("afk")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Only players can use this command.");
                return true;
            }

            if (!player.hasPermission("a1.afk")) {
                MessageUtil.sendMessage(plugin, player, "no-permission");
                return true;
            }

            if (!player.hasPermission("a1.afk.cooldownbypass")) {
                double remaining = plugin.getAfkManager().getRemainingCooldown(player, "afk");
                if (remaining > 0) {
                    MessageUtil.sendMessage(plugin, player, "afk-cooldown-msg");
                    return true;
                }
                plugin.getAfkManager().setCooldown(player, "afk");
            }

            boolean isAfk = plugin.getAfkManager().isAfk(player);
            plugin.getAfkManager().setAfk(player, !isAfk);

            if (!isAfk) {
                MessageUtil.sendMessage(plugin, player, "afk-enabled");
                plugin.broadcastAfkStatus(player, true);
            } else {
                MessageUtil.sendMessage(plugin, player, "afk-disabled");
                plugin.broadcastAfkStatus(player, false);
            }
            return true;
        }

        if (command.getName().equalsIgnoreCase("rename")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Only players can use this command.");
                return true;
            }

            if (!player.hasPermission("a1.rename")) {
                MessageUtil.sendMessage(plugin, player, "no-permission");
                return true;
            }

            ItemStack item = player.getInventory().getItemInMainHand();
            if (item.getType().isAir() || !item.getType().isItem()) {
                MessageUtil.sendMessage(plugin, player, "no-item-in-hand");
                return true;
            }

            if (args.length == 0) {
                player.sendMessage("Usage: /rename <displayName | -reset>");
                return true;
            }

            String arg = String.join(" ", args);
            ItemMeta meta = item.getItemMeta();
            if (meta == null) {
                // Highly unlikely for standard items, but just in case
                meta = Bukkit.getItemFactory().getItemMeta(item.getType());
            }

            if (arg.equalsIgnoreCase("-reset") || arg.equalsIgnoreCase("-r")) {
                if (meta != null) {
                    meta.displayName(null);
                    item.setItemMeta(meta);
                    MessageUtil.sendMessage(plugin, player, "item-rename-reset");
                }
                return true;
            }

            boolean hasColor = player.hasPermission("a1.rename.color");
            boolean hasFormat = player.hasPermission("a1.rename.format");
            boolean hasObfuscated = player.hasPermission("a1.rename.obfuscated");
            boolean hasRgb = player.hasPermission("a1.rename.rgb");
            boolean hasGradient = player.hasPermission("a1.rename.gradient");

            Component formattedName = A2API.format(arg, hasColor, hasFormat, hasObfuscated, hasRgb, hasGradient);

            // Apply italic: false so it doesn't default to italics when no explicit format is provided
            Component nonItalicName = Component.empty().decoration(TextDecoration.ITALIC, false).append(formattedName);

            meta.displayName(nonItalicName);
            item.setItemMeta(meta);

            MessageUtil.sendMessage(plugin, player, "item-renamed");
            return true;
        }

        if (command.getName().equalsIgnoreCase("hat")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Only players can use this command.");
                return true;
            }
            if (!player.hasPermission("a1.hat")) {
                MessageUtil.sendMessage(plugin, player, "no-permission");
                return true;
            }

            if (args.length == 1 && (args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("r"))) {
                ItemStack helmet = player.getInventory().getHelmet();
                if (helmet == null || helmet.getType().isAir()) {
                    MessageUtil.sendMessage(plugin, player, "no-hat-to-remove");
                    return true;
                }
                player.getInventory().setHelmet(null);
                java.util.HashMap<Integer, ItemStack> leftOvers = player.getInventory().addItem(helmet);
                if (!leftOvers.isEmpty()) {
                    for (ItemStack leftOver : leftOvers.values()) {
                        player.getWorld().dropItemNaturally(player.getLocation(), leftOver);
                    }
                }
                MessageUtil.sendMessage(plugin, player, "hat-removed");
                return true;
            }

            ItemStack mainHand = player.getInventory().getItemInMainHand();
            if (mainHand == null || mainHand.getType().isAir() || !mainHand.getType().isItem()) {
                MessageUtil.sendMessage(plugin, player, "no-item-in-hand");
                return true;
            }

            ItemStack currentHelmet = player.getInventory().getHelmet();
            ItemStack toWear = mainHand.clone();
            toWear.setAmount(1);

            mainHand.setAmount(mainHand.getAmount() - 1);
            if (mainHand.getAmount() <= 0) {
                player.getInventory().setItemInMainHand(null);
            } else {
                player.getInventory().setItemInMainHand(mainHand);
            }

            player.getInventory().setHelmet(toWear);

            if (currentHelmet != null && !currentHelmet.getType().isAir()) {
                java.util.HashMap<Integer, ItemStack> leftOvers = player.getInventory().addItem(currentHelmet);
                if (!leftOvers.isEmpty()) {
                    for (ItemStack leftOver : leftOvers.values()) {
                        player.getWorld().dropItemNaturally(player.getLocation(), leftOver);
                    }
                }
            }

            MessageUtil.sendMessage(plugin, player, "hat-equipped");
            return true;
        }

        if (command.getName().equalsIgnoreCase("tpa")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Only players can use this command.");
                return true;
            }
            if (!player.hasPermission("a1.tpa")) {
                MessageUtil.sendMessage(plugin, player, "no-permission");
                return true;
            }
            if (args.length == 0) {
                player.sendMessage("Usage: /tpa <player>");
                return true;
            }
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null || !target.isOnline()) {
                MessageUtil.sendMessage(plugin, player, "player-not-found");
                return true;
            }

            plugin.getTpaManager().addRequest(target.getUniqueId(), player.getUniqueId(), TpaManager.RequestType.TPA);

            java.util.Map<String, String> pExtra = new java.util.HashMap<>();
            pExtra.put("%target%", target.getName());
            MessageUtil.sendMessageWithPlaceholders(plugin, player, "tpa-request-sent", pExtra);

            java.util.Map<String, String> tExtra = new java.util.HashMap<>();
            tExtra.put("%player%", player.getName());
            MessageUtil.sendMessageWithPlaceholders(plugin, target, "tpa-request-received", tExtra);

            return true;
        }

        if (command.getName().equalsIgnoreCase("tpahere")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Only players can use this command.");
                return true;
            }
            if (!player.hasPermission("a1.tpahere")) {
                MessageUtil.sendMessage(plugin, player, "no-permission");
                return true;
            }
            if (args.length == 0) {
                player.sendMessage("Usage: /tpahere <player>");
                return true;
            }
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null || !target.isOnline()) {
                MessageUtil.sendMessage(plugin, player, "player-not-found");
                return true;
            }

            plugin.getTpaManager().addRequest(target.getUniqueId(), player.getUniqueId(), TpaManager.RequestType.TPAHERE);

            java.util.Map<String, String> pExtra = new java.util.HashMap<>();
            pExtra.put("%target%", target.getName());
            MessageUtil.sendMessageWithPlaceholders(plugin, player, "tpahere-request-sent", pExtra);

            java.util.Map<String, String> tExtra = new java.util.HashMap<>();
            tExtra.put("%player%", player.getName());
            MessageUtil.sendMessageWithPlaceholders(plugin, target, "tpahere-request-received", tExtra);

            return true;
        }

        if (command.getName().equalsIgnoreCase("tpaall")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Only players can use this command.");
                return true;
            }
            if (!player.hasPermission("a1.tpaall")) {
                MessageUtil.sendMessage(plugin, player, "no-permission");
                return true;
            }

            for (Player target : Bukkit.getOnlinePlayers()) {
                if (!target.equals(player)) {
                    plugin.getTpaManager().addRequest(target.getUniqueId(), player.getUniqueId(), TpaManager.RequestType.TPAHERE);
                    java.util.Map<String, String> tExtra = new java.util.HashMap<>();
                    tExtra.put("%player%", player.getName());
                    MessageUtil.sendMessageWithPlaceholders(plugin, target, "tpahere-request-received", tExtra);
                }
            }

            MessageUtil.sendMessage(plugin, player, "tpaall-request-sent");
            return true;
        }

        if (command.getName().equalsIgnoreCase("tpyes")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Only players can use this command.");
                return true;
            }

            TpaManager.TpaRequest request = plugin.getTpaManager().getRequest(player.getUniqueId());
            if (request == null) {
                MessageUtil.sendMessage(plugin, player, "no-pending-request");
                return true;
            }

            Player requester = Bukkit.getPlayer(request.getRequester());
            if (requester == null || !requester.isOnline()) {
                MessageUtil.sendMessage(plugin, player, "player-not-found");
                plugin.getTpaManager().removeRequest(player.getUniqueId());
                return true;
            }

            MessageUtil.sendMessage(plugin, player, "tpa-accepted");
            MessageUtil.sendMessage(plugin, requester, "tpa-accepted");
            MessageUtil.sendMessage(plugin, player, "tpa-teleporting");
            MessageUtil.sendMessage(plugin, requester, "tpa-teleporting");

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
                MessageUtil.sendMessage(plugin, player, "no-pending-request");
                return true;
            }

            Player requester = Bukkit.getPlayer(request.getRequester());
            if (requester != null && requester.isOnline()) {
                MessageUtil.sendMessage(plugin, requester, "tpa-denied");
            }
            MessageUtil.sendMessage(plugin, player, "tpa-denied");

            plugin.getTpaManager().removeRequest(player.getUniqueId());
            return true;
        }

        if (command.getName().equalsIgnoreCase("a1dialog")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Only players can use this command.");
                return true;
            }
            if (args.length == 0) {
                return true;
            }

            String dialogId = args[0];
            com.google.gson.JsonObject dialogs = plugin.getConfigManager().getDialogs();

            if (dialogs != null && dialogs.has(dialogId)) {
                com.google.gson.JsonElement elem = dialogs.get(dialogId);
                if (elem.isJsonObject()) {
                    org.jerae.a4.A4API.showDialog(player, dialogId, elem.getAsJsonObject(), dialogs);
                } else {
                    java.util.Map<String, String> extra = new java.util.HashMap<>();
                    extra.put("%dialog_id%", dialogId);
                    MessageUtil.sendMessageWithPlaceholders(plugin, player, "dialog-load-error", extra);
                }
            } else {
                java.util.Map<String, String> extra = new java.util.HashMap<>();
                extra.put("%dialog_id%", dialogId);
                MessageUtil.sendMessageWithPlaceholders(plugin, player, "dialog-load-error", extra);
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
                    MessageUtil.sendMessage(plugin, player, "version-message");
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
                        MessageUtil.sendMessage(plugin, player, "no-permission");
                    } else {
                        sender.sendMessage("No permission.");
                    }
                    return true;
                }
                plugin.getConfigManager().reload();
                if (sender instanceof Player player) {
                    MessageUtil.sendMessage(plugin, player, "plugin-reloaded");
                } else {
                    sender.sendMessage("Plugin successfully reloaded.");
                }
                return true;
            }
        }

        return false;
    }
}
