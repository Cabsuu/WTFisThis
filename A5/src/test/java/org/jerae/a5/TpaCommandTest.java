package org.jerae.a5;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.bukkit.Server;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import java.util.UUID;
import org.bukkit.configuration.file.FileConfiguration;

import static org.mockito.ArgumentMatchers.anyString;

public class TpaCommandTest {

    private A5 plugin;
    private Player requester;
    private Player target;
    private Command command;
    private FileConfiguration config;
    private Server server;
    private TeleportCommands commands;
    private TpaManager tpaManager;

    @BeforeEach
    public void setup() {
        plugin = Mockito.mock(A5.class);
        requester = Mockito.mock(Player.class);
        target = Mockito.mock(Player.class);
        command = Mockito.mock(Command.class);
        config = Mockito.mock(FileConfiguration.class);
        server = Mockito.mock(Server.class);

        tpaManager = new TpaManager(plugin);
        when(plugin.getTpaManager()).thenReturn(tpaManager);
        when(plugin.getServer()).thenReturn(server);

        commands = new TeleportCommands(plugin);

        when(requester.getName()).thenReturn("Requester");
        when(target.getName()).thenReturn("Target");

        UUID reqId = UUID.randomUUID();
        UUID tgtId = UUID.randomUUID();

        when(requester.getUniqueId()).thenReturn(reqId);
        when(requester.hasPermission(anyString())).thenReturn(true);
        when(requester.isOnline()).thenReturn(true);

        when(target.getUniqueId()).thenReturn(tgtId);
        when(target.hasPermission(anyString())).thenReturn(true);
        when(target.isOnline()).thenReturn(true);

        when(server.getPlayer("Target")).thenReturn(target);
        when(server.getPlayer(reqId)).thenReturn(requester);
    }

    @Test
    public void testTpaCommand() {
        when(command.getName()).thenReturn("tpa");
        boolean result = commands.onCommand(requester, command, "tpa", new String[]{"Target"});

        assertTrue(result);

        TpaManager.TpaRequest request = tpaManager.getRequest(target.getUniqueId());
        assertNotNull(request);
        assertEquals(requester.getUniqueId(), request.getRequester());
        assertEquals(TpaManager.RequestType.TPA, request.getType());

        tpaManager.removeRequest(target.getUniqueId());
        assertNull(tpaManager.getRequest(target.getUniqueId()));
    }
}
