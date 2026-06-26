package org.jerae.a3;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jerae.a2.A2API;

public class A3API {

    public static Component parse(Player player, String message) {
        String processed = message;

        if (processed.contains("%player_username%")) {
            processed = processed.replace("%player_username%", player.getName());
        }

        if (processed.contains("%player_displayname%")) {
            String legacyDisplayName = LegacyComponentSerializer.legacySection().serialize(player.displayName());
            // Need to convert section symbol to ampersand so A2 API can convert it cleanly
            legacyDisplayName = legacyDisplayName.replace('§', '&');
            processed = processed.replace("%player_displayname%", legacyDisplayName);
        }

        if (processed.contains("%a1_version%")) {
            Plugin a1 = Bukkit.getPluginManager().getPlugin("A1");
            String version = a1 != null ? a1.getPluginMeta().getVersion() : "Unknown";
            processed = processed.replace("%a1_version%", version);
        }

        // Apply colors globally using A2 API. We assume maximum features allowed here as requested.
        return A2API.format(processed, true, true, true, true, true);
    }
}
