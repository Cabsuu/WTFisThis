package org.jerae.a2;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.command.CommandSender;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class A2ApiTest {

    private CommandSender createMockSender(boolean... permissions) {
        // Mock a command sender that has specific permissions
        return (CommandSender) Proxy.newProxyInstance(
                CommandSender.class.getClassLoader(),
                new Class[]{CommandSender.class},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        if (method.getName().equals("hasPermission") && args.length == 1) {
                            String perm = (String) args[0];
                            if (perm.equals("a2.chat.color") && permissions.length > 0) return permissions[0];
                            if (perm.equals("a2.chat.format") && permissions.length > 1) return permissions[1];
                            if (perm.equals("a2.chat.obfuscated") && permissions.length > 2) return permissions[2];
                            if (perm.equals("a2.chat.rgb") && permissions.length > 3) return permissions[3];
                            if (perm.equals("a2.chat.gradient") && permissions.length > 4) return permissions[4];
                            return false;
                        }
                        return null;
                    }
                }
        );
    }

    @Test
    public void testLegacyColor() {
        CommandSender sender = createMockSender(true); // Has color
        Component comp = A2Api.processColors("&aHello", sender);
        assertEquals("Hello", PlainTextComponentSerializer.plainText().serialize(comp));
        // Note: Component equality or exact parsing test would need minimessage asserting.
        // For now, ensuring it strips legacy ampersand and retains text is good enough.
    }

    @Test
    public void testRgb() {
        CommandSender sender = createMockSender(false, false, false, true); // Has rgb
        Component comp = A2Api.processColors("&xFF00FFtest", sender);
        assertEquals("test", PlainTextComponentSerializer.plainText().serialize(comp));
    }

    @Test
    public void testGradient() {
        CommandSender sender = createMockSender(false, false, false, false, true); // Has gradient
        Component comp = A2Api.processColors("<&xFF99CC:&x66AA22>test", sender);
        assertEquals("test", PlainTextComponentSerializer.plainText().serialize(comp));
    }
}
