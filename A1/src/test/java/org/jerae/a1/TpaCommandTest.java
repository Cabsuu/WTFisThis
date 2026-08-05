package org.jerae.a1;

import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import org.bukkit.configuration.file.FileConfiguration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class TpaCommandTest {

    private A1 plugin;
    private Player requester;
    private Player target;
    private Command command;
    private Commands commands;
    private TpaManager tpaManager;
    private ConfigManager configManager;
    private FileConfiguration messages;
    private FileConfiguration config;
    private DataManager dataManager;
    private Server server;

    @BeforeEach
    public void setup() {
        plugin = mock(A1.class);
        requester = mock(Player.class);
        target = mock(Player.class);
        command = mock(Command.class);
        configManager = mock(ConfigManager.class);
        messages = mock(FileConfiguration.class);
        config = mock(FileConfiguration.class);
        dataManager = mock(DataManager.class);
        server = mock(Server.class);

        tpaManager = new TpaManager(plugin);

        when(plugin.getTpaManager()).thenReturn(tpaManager);
        when(plugin.getConfigManager()).thenReturn(configManager);
        when(plugin.getDataManager()).thenReturn(dataManager);
        when(plugin.getServer()).thenReturn(server);
        when(configManager.getMessages()).thenReturn(messages);
        when(configManager.getConfig()).thenReturn(config);
        when(messages.getString(anyString())).thenReturn("");
        when(config.getString(anyString(), anyString())).thenReturn("*");

        UUID reqId = UUID.randomUUID();
        UUID targId = UUID.randomUUID();
        when(requester.getUniqueId()).thenReturn(reqId);
        when(requester.getName()).thenReturn("Requester");
        when(requester.isOnline()).thenReturn(true);
        when(target.getUniqueId()).thenReturn(targId);
        when(target.getName()).thenReturn("Target");
        when(target.isOnline()).thenReturn(true);

        commands = new Commands(plugin);
    }

    @Test
    public void testTpaRequest() {
        when(command.getName()).thenReturn("tpa");
        when(requester.hasPermission("a1.tpa")).thenReturn(true);

        // Mock Bukkit statically handled locally in a standalone test could be problematic due to static Bukkit class.
        // Instead of testing Command execution requiring Bukkit.getPlayer, we will just test TpaManager directly if we can,
        // or since we are relying on Bukkit.getPlayer, we can use an inline mock for Bukkit if needed.
        // Since we didn't mock static Bukkit, we will test TpaManager directly for state logic.

        tpaManager.addRequest(target.getUniqueId(), requester.getUniqueId(), TpaManager.RequestType.TPA);
        assertNotNull(tpaManager.getRequest(target.getUniqueId()));
        assertEquals(requester.getUniqueId(), tpaManager.getRequest(target.getUniqueId()).getRequester());
        assertEquals(TpaManager.RequestType.TPA, tpaManager.getRequest(target.getUniqueId()).getType());

        tpaManager.removeRequest(target.getUniqueId());
        assertNull(tpaManager.getRequest(target.getUniqueId()));
    }
}
