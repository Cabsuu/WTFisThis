package org.jerae.a1;

import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jerae.a2.A2Api;

public class A1PlayerListener implements Listener {

    private final A1 plugin;

    public A1PlayerListener(A1 plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        String rawNick = plugin.getNickDataManager().getNickname(event.getPlayer().getUniqueId());
        if (rawNick != null) {
            // Apply nick with current player permissions, not console
            Component formattedNick = A2Api.processColors(rawNick, event.getPlayer(), "a1.nick");
            event.getPlayer().displayName(formattedNick);
        }
    }
}
