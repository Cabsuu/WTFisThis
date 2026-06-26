package org.jerae.a3;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public class A3Api {

    public static String setPlaceholders(Player player, String text) {
        if (text == null) return null;

        String result = text;

        if (player != null) {
            result = result.replace("%player_username%", player.getName());

            Component displayNameComp = player.displayName();
            String displayName = displayNameComp != null ?
                PlainTextComponentSerializer.plainText().serialize(displayNameComp) : player.getName();

            result = result.replace("%player_displayname%", displayName);
        }

        if (result.contains("%a1_version%")) {
            Plugin a1Plugin = Bukkit.getPluginManager().getPlugin("A1");
            String a1Version = a1Plugin != null ? a1Plugin.getPluginMeta().getVersion() : "Unknown";
            result = result.replace("%a1_version%", a1Version);
        }

        return result;
    }
}
