package org.jerae.a1;

import org.bukkit.Server;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import io.papermc.paper.plugin.configuration.PluginMeta;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

public class ConfigManagerDialogTest {

    private Plugin plugin;
    private Server server;
    private PluginManager pluginManager;
    private File dataFolder;

    @BeforeEach
    public void setup() throws IOException {
        plugin = Mockito.mock(Plugin.class);
        server = Mockito.mock(Server.class);
        pluginManager = Mockito.mock(PluginManager.class);

        dataFolder = Files.createTempDirectory("a1_test_folder").toFile();

        when(plugin.getServer()).thenReturn(server);
        when(server.getPluginManager()).thenReturn(pluginManager);
        when(plugin.getDataFolder()).thenReturn(dataFolder);
        // Let's use PluginDescriptionFile
        PluginDescriptionFile desc = new PluginDescriptionFile("A1", "1.0", "org.jerae.a1.A1");
        when(plugin.getDescription()).thenReturn(desc);
        PluginMeta meta = Mockito.mock(PluginMeta.class);
        when(meta.getVersion()).thenReturn("1.0");
        when(plugin.getPluginMeta()).thenReturn(meta);
    }

    @AfterEach
    public void teardown() throws IOException {
        Files.walk(dataFolder.toPath())
                .sorted(Comparator.reverseOrder())
                .map(java.nio.file.Path::toFile)
                .forEach(File::delete);
    }

    @Test
    public void testDialogYmlLoadedWhenA4Present() {
        Plugin a4Plugin = Mockito.mock(Plugin.class);
        when(pluginManager.getPlugin("A4")).thenReturn(a4Plugin);

        ConfigManager configManager = new ConfigManager(plugin);
        FileConfiguration dialogs = configManager.getDialogs();

        assertNotNull(dialogs, "Dialogs config should not be null when A4 is present");
    }

    @Test
    public void testDialogYmlNotLoadedWhenA4Absent() {
        when(pluginManager.getPlugin("A4")).thenReturn(null);

        ConfigManager configManager = new ConfigManager(plugin);
        FileConfiguration dialogs = configManager.getDialogs();

        assertNull(dialogs, "Dialogs config should be null when A4 is absent");
    }
}
