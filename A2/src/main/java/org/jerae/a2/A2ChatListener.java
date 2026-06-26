package org.jerae.a2;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class A2ChatListener implements Listener {

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        String originalMessage = PlainTextComponentSerializer.plainText().serialize(event.originalMessage());

        Component processedComponent = A2Api.processColors(originalMessage, event.getPlayer());

        event.message(processedComponent);
    }
}
