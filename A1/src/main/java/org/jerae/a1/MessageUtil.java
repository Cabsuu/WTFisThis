package org.jerae.a1;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jerae.a3.A3API;

public class MessageUtil {

    public static Component parseTextholders(A1 plugin, Player player, Component input) {
        FileConfiguration textholdersConfig = plugin.getConfigManager().getTextholders();

        return input.replaceText(TextReplacementConfig.builder()
            .match("\\{([a-zA-Z0-9_-]+)\\}")
            .replacement((matchResult, builder) -> {
                String tag = matchResult.group(1);

                if (!textholdersConfig.contains(tag)) {
                    return builder;
                }

                if (!player.hasPermission("a1.textholder." + tag)) {
                    return builder;
                }

                ConfigurationSection section = textholdersConfig.getConfigurationSection(tag);
                if (section == null) {
                    return builder;
                }

                String textRaw = section.getString("text", "");
                Component replacement = A3API.parse(player, textRaw);

                if (section.getBoolean("click-event.enabled", false)) {
                    String type = section.getString("click-event.type", "").toUpperCase();
                    String lineRaw = section.getString("click-event.line", "");
                    String lineStr = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(A3API.parse(player, lineRaw));
                    if (type.equals("RUN_COMMAND")) replacement = replacement.clickEvent(ClickEvent.runCommand(lineStr));
                    else if (type.equals("COPY_TO_CLIPBOARD")) replacement = replacement.clickEvent(ClickEvent.copyToClipboard(lineStr));
                    else if (type.equals("SUGGEST_COMMAND")) replacement = replacement.clickEvent(ClickEvent.suggestCommand(lineStr));
                    else if (type.equals("OPEN_URL")) replacement = replacement.clickEvent(ClickEvent.openUrl(lineStr));
                }

                if (section.getBoolean("hover-event.enabled", false)) {
                    String type = section.getString("hover-event.type", "").toUpperCase();
                    String lineRaw = section.getString("hover-event.line", "");
                    Component lineComp = A3API.parse(player, lineRaw);
                    switch (type) {
                        case "SHOW_TEXT":
                            replacement = replacement.hoverEvent(HoverEvent.showText(lineComp));
                            break;
                        case "SHOW_ITEM":
                            if (lineRaw.equalsIgnoreCase("mainhand")) {
                                org.bukkit.inventory.ItemStack mainHand = player.getInventory().getItemInMainHand();
                                if (mainHand != null && !mainHand.getType().isAir()) {
                                    replacement = replacement.hoverEvent(mainHand.asHoverEvent());
                                }
                            } else if (lineRaw.equalsIgnoreCase("offhand")) {
                                org.bukkit.inventory.ItemStack offHand = player.getInventory().getItemInOffHand();
                                if (offHand != null && !offHand.getType().isAir()) {
                                    replacement = replacement.hoverEvent(offHand.asHoverEvent());
                                }
                            } else {
                                try {
                                    if (lineRaw.contains("\"id\":\"")) {
                                        String id = lineRaw.split("\"id\":\"")[1].split("\"")[0];
                                        String countStr = "1";
                                        if (lineRaw.contains("\"count\":\"")) {
                                            countStr = lineRaw.split("\"count\":\"")[1].split("\"")[0];
                                        }
                                        int count = Integer.parseInt(countStr);
                                        replacement = replacement.hoverEvent(HoverEvent.showItem(net.kyori.adventure.key.Key.key(id), count));
                                    }
                                } catch (Exception ignored) {
                                }
                            }
                            break;
                    }
                }

                return replacement;
            })
            .build());
    }

    public static void sendMessage(A1 plugin, Player player, String path) {
        String msg = plugin.getConfigManager().getMessages().getString(path);
        if (msg != null && !msg.isEmpty()) {
            Component parsed = A3API.parse(player, msg);
            parsed = parseTextholders(plugin, player, parsed);
            player.sendMessage(parsed);
        }
    }

    public static void sendMessageWithTarget(A1 plugin, Player player, Player target, String path) {
        String msg = plugin.getConfigManager().getMessages().getString(path);
        if (msg != null && !msg.isEmpty()) {
            msg = msg.replace("<player>", target.getName());
            Component parsed = A3API.parse(player, msg);
            parsed = parseTextholders(plugin, player, parsed);
            player.sendMessage(parsed);
        }
    }
}
