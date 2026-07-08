package org.jerae.a1;

import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class NicknameSymbolTest {

    private A1 plugin;
    private Player player;
    private Command command;
    private Commands commands;
    private DataManager dataManager;
    private ConfigManager configManager;
    private org.bukkit.configuration.file.FileConfiguration messages;

    @BeforeEach
    public void setup() {
        plugin = mock(A1.class);
        player = mock(Player.class);
        command = mock(Command.class);
        dataManager = mock(DataManager.class);
        configManager = mock(ConfigManager.class);
        messages = mock(org.bukkit.configuration.file.FileConfiguration.class);

        when(command.getName()).thenReturn("nick");
        when(plugin.getDataManager()).thenReturn(dataManager);
        when(plugin.getConfigManager()).thenReturn(configManager);
        when(configManager.getMessages()).thenReturn(messages);
        when(messages.getString(anyString())).thenReturn("");

        when(player.hasPermission("a1.nick")).thenReturn(true);

        commands = new Commands(plugin);
    }

    @Test
    public void testSymbolWithoutPermission() {
        when(player.hasPermission("a1.nick.symbol")).thenReturn(false);

        // Attempting to use a symbol '$'
        String[] args = {"Jules$Test"};
        commands.onCommand(player, command, "nick", args);

        // displayName should not be called because it was blocked
        verify(player, never()).displayName(any());
        verify(dataManager, never()).setNickname(any(), any());
    }

    @Test
    public void testSymbolWithPermission() {
        when(player.hasPermission("a1.nick.symbol")).thenReturn(true);

        String[] args = {"Jules$Test"};
        commands.onCommand(player, command, "nick", args);

        // displayName should be called
        verify(player, times(1)).displayName(any());
        verify(dataManager, times(1)).setNickname(any(), eq("Jules$Test"));
    }

    @Test
    public void testNoSymbolWithoutPermission() {
        when(player.hasPermission("a1.nick.symbol")).thenReturn(false);

        // Formatting codes and underscores are allowed
        String[] args = {"&aJules_Test"};
        commands.onCommand(player, command, "nick", args);

        // displayName should be called
        verify(player, times(1)).displayName(any());
        verify(dataManager, times(1)).setNickname(any(), eq("&aJules_Test"));
    }
}
