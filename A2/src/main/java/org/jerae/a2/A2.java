package org.jerae.a2;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public final class A2 extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        String message = PlainTextComponentSerializer.plainText().serialize(event.message());

        boolean hasColor = player.hasPermission("a2.chat.color");
        boolean hasFormat = player.hasPermission("a2.chat.format");
        boolean hasObfuscated = player.hasPermission("a2.chat.obfuscated");
        boolean hasRgb = player.hasPermission("a2.chat.rgb");
        boolean hasGradient = player.hasPermission("a2.chat.gradient");

        event.message(A2API.format(message, hasColor, hasFormat, hasObfuscated, hasRgb, hasGradient));
    }
}
