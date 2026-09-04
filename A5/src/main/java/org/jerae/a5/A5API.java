package org.jerae.a5;

import org.bukkit.entity.Player;
import java.util.function.BiConsumer;

public class A5API {

    public interface MessageSender {
        void sendMessage(Player player, String messageKey);
    }

    public interface MessageWithTargetSender {
        void sendMessage(Player player, Player target, String messageKey);
    }

    private static MessageSender messageSender;
    private static MessageWithTargetSender messageWithTargetSender;

    public static void registerMessageSenders(MessageSender sender, MessageWithTargetSender targetSender) {
        messageSender = sender;
        messageWithTargetSender = targetSender;
    }

    public static void sendMessage(Player player, String messageKey) {
        if (messageSender != null) {
            messageSender.sendMessage(player, messageKey);
        } else {
            player.sendMessage("Message key: " + messageKey);
        }
    }

    public static void sendMessageWithTarget(Player player, Player target, String messageKey) {
        if (messageWithTargetSender != null) {
            messageWithTargetSender.sendMessage(player, target, messageKey);
        } else {
            player.sendMessage("Message key with target: " + messageKey);
        }
    }
}
