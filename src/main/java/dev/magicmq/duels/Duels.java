package dev.magicmq.duels;

import dev.magicmq.duels.commands.DuelsCommand;
import dev.magicmq.duels.commands.DuelsKitCommand;
import dev.magicmq.duels.config.PluginConfig;
import dev.magicmq.duels.controllers.kits.KitsController;
import dev.magicmq.duels.controllers.QueueController;
import dev.magicmq.duels.controllers.game.DuelController;
import dev.magicmq.duels.controllers.player.DuelsPlayer;
import dev.magicmq.duels.controllers.player.PlayerController;
import dev.magicmq.duels.storage.SQLStorage;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import dev.magicmq.duels.utils.VoidGenerator;

import java.sql.SQLException;
import java.util.logging.Level;

/**
 * @author magicmq
 */
public class Duels extends JavaPlugin {

    private static Duels instance;

    private Economy economy;

    @Override
    public void onEnable() {
        instance = this;

        getConfig().options().copyDefaults(true);
        saveDefaultConfig();
        reloadConfig();

        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        economy = rsp.getProvider();

        SQLStorage.get();
        PlayerController.get();
        DuelController.get();
        QueueController.get();
        KitsController.get();

        Bukkit.getPluginManager().registerEvents(new PluginListener(), this);

        getCommand("duels").setExecutor(new DuelsCommand());
        getCommand("duelskit").setExecutor(new DuelsKitCommand());

        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
            for (DuelsPlayer player : PlayerController.get().getPlayers()) {
                SQLStorage.get().savePlayer(player);
            }
        }, PluginConfig.getAutosaveInterval() * 60L * 20L, PluginConfig.getAutosaveInterval() * 60L * 20L);
    }

    @Override
    public void onDisable() {
        Bukkit.getScheduler().cancelTasks(this);

        try {
            SQLStorage.get().shutdown();
        } catch (SQLException e) {
            getLogger().log(Level.SEVERE, "There was an error when saving a player's data to the Duels SQL table during shutdown! See this error:");
            e.printStackTrace();
        }
    }

    @Override
    public ChunkGenerator getDefaultWorldGenerator(String name, String id) {
        return new VoidGenerator();
    }

    public void reload() {
        reloadConfig();
        PluginConfig.reload();
    }

    public Economy getEconomy() {
        return economy;
    }

    public static Duels get() {
        return instance;
    }
}
