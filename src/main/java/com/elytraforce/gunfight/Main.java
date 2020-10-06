package com.elytraforce.gunfight;

import net.milkbowl.vault.economy.Economy;


import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.ipvp.canvas.MenuFunctionListener;

import com.elytraforce.gunfight.commands.DuelsCommand;
import com.elytraforce.gunfight.commands.QuitCommand;
import com.elytraforce.gunfight.config.PluginConfig;
import com.elytraforce.gunfight.controllers.QueueController;
import com.elytraforce.gunfight.controllers.game.DuelController;
import com.elytraforce.gunfight.controllers.kits.KitsController;
import com.elytraforce.gunfight.controllers.player.DuelsPlayer;
import com.elytraforce.gunfight.controllers.player.PlayerController;
import com.elytraforce.gunfight.storage.SQLStorage;
import com.elytraforce.gunfight.utils.VoidGenerator;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;

/**
 * @author Aurium, magicmq
 */
public class Main extends JavaPlugin {

    private static Main instance;

    private Economy economy;

    private boolean started;

    @Override
    public void onEnable() {
        instance = this;

        getConfig().options().copyDefaults(true);
        saveDefaultConfig();
        reloadConfig();

        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        economy = rsp.getProvider();

        File worldsFile = new File(getDataFolder(), "template_worlds");
        if (!worldsFile.exists()) {
            worldsFile.mkdir();
            getLogger().warning("*** ATTENTION ***");
            getLogger().warning("The template_worlds folder has been generated. The plugin will be disabled. Please drop at least one template world into this folder and restart or load the plugin.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        SQLStorage.get();
        PlayerController.get();
        DuelController.get();
        QueueController.get();
        KitsController.get();

        Bukkit.getPluginManager().registerEvents(new PluginListener(), this);
        Bukkit.getPluginManager().registerEvents(new BombListener(), this);
        Bukkit.getPluginManager().registerEvents(new MenuFunctionListener(), this);

        getCommand("duels").setExecutor(new DuelsCommand());
        getCommand("quit").setExecutor(new QuitCommand());

        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
            for (DuelsPlayer player : PlayerController.get().getPlayers()) {
                if (player.isInDatabase())
                    SQLStorage.get().updatePlayer(player, true);
                else
                    SQLStorage.get().insertPlayer(player, true);
            }
        }, PluginConfig.getAutosaveInterval() * 60L * 20L, PluginConfig.getAutosaveInterval() * 60L * 20L);

        started = true;
    }

    @Override
    public void onDisable() {
        if (started) {
            DuelController.get().shutdown();

            Bukkit.getScheduler().cancelTasks(this);

            SQLStorage.get().shutdown();
        }
    }

    @Override
    public ChunkGenerator getDefaultWorldGenerator(String name, String id) {
        return new VoidGenerator();
    }

    public Economy getEconomy() {
        return economy;
        
    }

    // Utility Methods

    public void connectToLobby(Player player) {
        try {
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                try (DataOutputStream dos = new DataOutputStream(baos)) {
                    dos.writeUTF("Connect");
                    dos.writeUTF(PluginConfig.getLobbyServerName());
                    player.sendPluginMessage(this, "BungeeCord", baos.toByteArray());
                }
            }
        } catch (IOException e) {
            getLogger().severe("Error when connecting player to lobby server:");
            e.printStackTrace();
        }
    }

    public static Main get() {
        return instance;
    }
}
