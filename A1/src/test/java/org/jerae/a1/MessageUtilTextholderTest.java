package org.jerae.a1;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.jerae.a3.A3API;

import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class MessageUtilTextholderTest {

    @Mock
    private A1 plugin;

    @Mock
    private ConfigManager configManager;

    @Mock
    private Player player;

    private YamlConfiguration textholders;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        when(plugin.getConfigManager()).thenReturn(configManager);

        String yaml = "example:\n" +
                "  text: \"Hello!\"\n" +
                "  click-event:\n" +
                "    enabled: true\n" +
                "    type: \"run_command\"\n" +
                "    line: \"/test\"\n" +
                "  hover-event:\n" +
                "    enabled: true\n" +
                "    type: \"show_text\"\n" +
                "    line: \"Hover me\"\n" +
                "  use-in-chat: true\n" +
                "disabled-click:\n" +
                "  text: \"No click\"\n" +
                "  click-event:\n" +
                "    enabled: false\n" +
                "  hover-event:\n" +
                "    enabled: false\n";

        textholders = YamlConfiguration.loadConfiguration(new StringReader(yaml));
        when(configManager.getTextholders()).thenReturn(textholders);

        when(player.hasPermission(anyString())).thenReturn(true);
    }

    @Test
    public void testTextholderReplacement() {
        Component input = Component.text("This is [example] and [disabled-click] and [missing]");
        Component output = MessageUtil.parseTextholders(plugin, player, input);

        String plain = PlainTextComponentSerializer.plainText().serialize(output);
        assertEquals("This is Hello! and No click and [missing]", plain);

        // Assert click event is present in the children somehow or hover event
        // We just verified the text got replaced correctly.
    }

    @Test
    public void testNoPermission() {
        when(player.hasPermission("a1.textholder.example")).thenReturn(false);
        Component input = Component.text("This is [example]");
        Component output = MessageUtil.parseTextholders(plugin, player, input);

        String plain = PlainTextComponentSerializer.plainText().serialize(output);
        assertEquals("This is [example]", plain);
    }
}
