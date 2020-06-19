package dev.magicmq.duels.controllers.game;

import com.grinderwolf.swm.api.SlimePlugin;
import com.grinderwolf.swm.api.exceptions.InvalidWorldException;
import com.grinderwolf.swm.api.exceptions.WorldAlreadyExistsException;
import com.grinderwolf.swm.api.exceptions.WorldLoadedException;
import com.grinderwolf.swm.api.exceptions.WorldTooBigException;
import com.grinderwolf.swm.api.loaders.SlimeLoader;
import com.grinderwolf.swm.api.world.SlimeWorld;
import com.grinderwolf.swm.api.world.properties.SlimeProperties;
import com.grinderwolf.swm.api.world.properties.SlimePropertyMap;
import dev.magicmq.duels.Duels;
import dev.magicmq.duels.config.PluginConfig;
import dev.magicmq.duels.controllers.player.DuelsPlayer;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import dev.magicmq.duels.utils.LoadWorldTask;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

public class DuelController {

    private static DuelController instance;

    private SlimePlugin swm;
    private SlimeLoader loader;
    private List<SlimeWorld> templates;

    private HashSet<Duel> activeGames;

    private DuelController() {
        swm = (SlimePlugin) Bukkit.getPluginManager().getPlugin("SlimeWorldManager");
        loader = swm.getLoader("mysql");

        templates = new ArrayList<>();
        File[] worlds = new File(Duels.get().getDataFolder(), "template_worlds").listFiles();
        Bukkit.getScheduler().runTaskAsynchronously(Duels.get(), () -> {
            try {
                for (int i = 0; i < worlds.length; i++) {
                    if (worlds[i].isDirectory())
                        swm.importWorld(worlds[i], "template_" + i, loader);
                }
                loadTemplateWorlds(worlds.length);
            } catch (IOException | WorldTooBigException | WorldLoadedException | InvalidWorldException e) {
                Duels.get().getLogger().log(Level.SEVERE, "Error when importing template world:");
                e.printStackTrace();
            } catch (WorldAlreadyExistsException ignored) {
                loadTemplateWorlds(worlds.length);
            }
        });

        Bukkit.getScheduler().scheduleSyncRepeatingTask(Duels.get(), () -> {
            for (Duel game : activeGames) {
                game.tick();
            }
        }, 20L, 20L);

        activeGames = new HashSet<>();
    }

    private void loadTemplateWorlds(int length) {
        for (int i = 0; i < length; i++) {
            SlimePropertyMap properties = new SlimePropertyMap();
            properties.setString(SlimeProperties.DIFFICULTY, "normal");
            properties.setBoolean(SlimeProperties.ALLOW_ANIMALS, false);
            properties.setBoolean(SlimeProperties.ALLOW_MONSTERS, false);
            properties.setInt(SlimeProperties.SPAWN_X, 0);
            properties.setInt(SlimeProperties.SPAWN_Y, 64);
            properties.setInt(SlimeProperties.SPAWN_Z, 0);
            new LoadWorldTask(swm, loader, "template_" + i, properties, world -> {
                swm.generateWorld(world);
                DuelController.this.templates.add(world);
            }).runTaskAsynchronously(Duels.get());
        }
    }

    public void beginGame(HashSet<DuelsPlayer> players, DuelType type) {
        UUID gameUUID = UUID.randomUUID();
        Random random = new Random();
        SlimeWorld template;
        if (templates.size() > 1) {
            template = templates.get(random.nextInt(templates.size()));
        } else {
            template = templates.get(0);
        }
        SlimeWorld gameWorld = template.clone(gameUUID.toString());
        swm.generateWorld(gameWorld);
        World world = Bukkit.getWorld(gameUUID.toString());
        world.getWorldBorder().setCenter(new Location(world, 0, 64, 0));
        world.getWorldBorder().setSize(PluginConfig.getWorldBorderSize());
        //TODO Other pre-game tasks, like timer, teleport players, etc.
        for (DuelsPlayer player : players) {

        }
        activeGames.add(new Duel(gameUUID, type, world, gameWorld, players));
    }

    public void endGame(Duel duel) {
        for (DuelsPlayer player : duel.getPlayers()) {
            player.asBukkitPlayer().setGameMode(GameMode.SURVIVAL);
            player.asBukkitPlayer().teleport(PluginConfig.getSpawnLocation());
        }
        Bukkit.unloadWorld(duel.getWorld(), false);
        activeGames.remove(duel);
    }

    public HashSet<Duel> getActiveGames() {
        return activeGames;
    }

    public static DuelController get() {
        if (instance == null) {
            instance = new DuelController();
        }
        return instance;
    }

}
