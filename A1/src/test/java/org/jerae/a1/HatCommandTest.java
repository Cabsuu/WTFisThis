package org.jerae.a1;

import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.PluginManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class HatCommandTest {

    private A1 plugin;
    private Commands commands;
    private Player player;
    private PlayerInventory inventory;
    private Command command;
    private World world;

    @BeforeEach
    public void setup() {
        plugin = mock(A1.class);
        commands = new Commands(plugin);

        player = mock(Player.class);
        inventory = mock(PlayerInventory.class);
        command = mock(Command.class);
        world = mock(World.class);

        when(player.getInventory()).thenReturn(inventory);
        when(player.getWorld()).thenReturn(world);
        when(command.getName()).thenReturn("hat");
        when(player.hasPermission("a1.hat")).thenReturn(true);

        ConfigManager configManager = mock(ConfigManager.class);
        org.bukkit.configuration.file.FileConfiguration messages = mock(org.bukkit.configuration.file.FileConfiguration.class);
        when(plugin.getConfigManager()).thenReturn(configManager);
        when(configManager.getMessages()).thenReturn(messages);
    }

    @Test
    public void testHatEquipStackedItem() {
        // Mock ItemStack to bypass Bukkit Registry / Material issues in unit tests
        ItemStack dirt = mock(ItemStack.class);
        Material mDirt = mock(Material.class);
        when(dirt.getType()).thenReturn(mDirt);
        when(mDirt.isAir()).thenReturn(false);
        when(mDirt.isItem()).thenReturn(true);
        when(dirt.getAmount()).thenReturn(64);
        ItemStack dirtClone = mock(ItemStack.class);
        when(dirt.clone()).thenReturn(dirtClone);

        when(inventory.getItemInMainHand()).thenReturn(dirt);
        when(inventory.getHelmet()).thenReturn(null);

        boolean result = commands.onCommand(player, command, "hat", new String[]{});

        assertTrue(result);

        verify(inventory).setHelmet(dirtClone);
        verify(inventory).setItemInMainHand(dirt);
    }

    @Test
    public void testHatRemove() {
        ItemStack helmet = mock(ItemStack.class);
        Material mHelmet = mock(Material.class);
        when(helmet.getType()).thenReturn(mHelmet);
        when(mHelmet.isAir()).thenReturn(false);

        when(inventory.getHelmet()).thenReturn(helmet);
        when(inventory.addItem(any(ItemStack.class))).thenReturn(new HashMap<>());

        boolean result = commands.onCommand(player, command, "hat", new String[]{"remove"});

        assertTrue(result);
        verify(inventory).setHelmet(null);
        verify(inventory).addItem(helmet);
    }

    @Test
    public void testHatSwap() {
        ItemStack helmet = mock(ItemStack.class);
        Material mHelmet = mock(Material.class);
        when(helmet.getType()).thenReturn(mHelmet);
        when(mHelmet.isAir()).thenReturn(false);
        when(mHelmet.isItem()).thenReturn(true);

        ItemStack sword = mock(ItemStack.class);
        Material mSword = mock(Material.class);
        when(sword.getType()).thenReturn(mSword);
        when(mSword.isAir()).thenReturn(false);
        when(mSword.isItem()).thenReturn(true);
        when(sword.getAmount()).thenReturn(1);
        ItemStack swordClone = mock(ItemStack.class);
        when(sword.clone()).thenReturn(swordClone);

        when(inventory.getHelmet()).thenReturn(helmet);
        when(inventory.getItemInMainHand()).thenReturn(sword);
        when(inventory.addItem(any(ItemStack.class))).thenReturn(new HashMap<>());

        boolean result = commands.onCommand(player, command, "hat", new String[]{});

        assertTrue(result);
        verify(inventory).setHelmet(swordClone);
        verify(inventory).setItemInMainHand(sword);
        verify(inventory).addItem(helmet);
    }
}
