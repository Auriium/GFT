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
import dev.magicmq.duels.utils.LoadWorldTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class DuelController {

    private static DuelController instance;

    private SlimePlugin swm;
    private SlimeLoader loader;
    private List<TemplateWorld> templates;
    private List<Material> allowedBlocks;

    private LinkedList<DuelWaiting> waiting;
    private HashSet<DuelGenerateWaiting> waitingToGenerate;
    private ConcurrentHashMap<UUID, Duel> activeGames;

    private DuelController() {
        swm = (SlimePlugin) Bukkit.getPluginManager().getPlugin("SlimeWorldManager");
        loader = swm.getLoader("mysql");

        templates = new ArrayList<>();

        List<String> worldNames = new ArrayList<>();
        File[] worlds = new File(Duels.get().getDataFolder(), "template_worlds").listFiles();
        Bukkit.getScheduler().runTaskAsynchronously(Duels.get(), () -> {
            try {
                for (int i = 0; i < worlds.length; i++) {
                    if (worlds[i].isDirectory())
                        try {
                            swm.importWorld(worlds[i], worlds[i].getName(), loader);
                        } catch (WorldAlreadyExistsException ignored) {} finally {
                            worldNames.add(worlds[i].getName());
                        }
                }
                loadTemplateWorlds(worldNames);
            } catch (IOException | WorldTooBigException | WorldLoadedException | InvalidWorldException e) {
                Duels.get().getLogger().log(Level.SEVERE, "Error when importing template world:");
                e.printStackTrace();
            }
        });

        allowedBlocks = new ArrayList<>();
        for (String material : PluginConfig.getAllowedBlocks()) {
            allowedBlocks.add(Material.getMaterial(material.toUpperCase()));
        }

        Bukkit.getScheduler().scheduleSyncRepeatingTask(Duels.get(), () -> {
            for (Duel game : activeGames.values()) {
                game.tick();
            }
        }, 20L, 20L);

        waiting = new LinkedList<>();
        waitingToGenerate = new HashSet<>();
        activeGames = new ConcurrentHashMap<>();
    }

    public void shutdown() {
        for (Duel duel : activeGames.values()) {
            duel.closeGame();
        }
    }

    @SuppressWarnings("unchecked")
    private void loadTemplateWorlds(List<String> worldNames) {
        for (String worldName : worldNames) {
            SlimePropertyMap properties = new SlimePropertyMap();
            properties.setString(SlimeProperties.DIFFICULTY, "normal");
            properties.setBoolean(SlimeProperties.ALLOW_ANIMALS, false);
            properties.setBoolean(SlimeProperties.ALLOW_MONSTERS, false);
            properties.setInt(SlimeProperties.SPAWN_X, 0);
            properties.setInt(SlimeProperties.SPAWN_Y, 64);
            properties.setInt(SlimeProperties.SPAWN_Z, 0);
            new LoadWorldTask(swm, loader, worldName, properties, world -> {
                swm.generateWorld(world);
                DuelController.this.templates.add(new TemplateWorld(world,
                        worldName,
                        PluginConfig.getTeamOneSpawns(world.getName()),
                        PluginConfig.getTeamTwoSpawns(world.getName())));
            }).runTaskAsynchronously(Duels.get());
        }
    }

    public void queueGame(HashSet<DuelsPlayer> players, DuelType type) {
        if (waitingToGenerate.size() == 0) {
            beginGame(players, type);
        } else {
            waiting.offer(new DuelWaiting(players, type));
        }
    }

    public void beginGame(HashSet<DuelsPlayer> players, DuelType type) {
        for (DuelsPlayer player : players) {
            if (!player.asBukkitPlayer().isOnline()) {
                players.stream().filter(toMessage -> player.asBukkitPlayer().isOnline()).forEach(toMessage -> toMessage.sendMessage(PluginConfig.getMessage("left-during-delay")));
                return;
            }
        }

        UUID gameUUID = UUID.randomUUID();
        Random random = new Random();
        TemplateWorld template;
        if (templates.size() > 1) {
            template = templates.get(random.nextInt(templates.size()));
        } else {
            template = templates.get(0);
        }

        SlimeWorld gameWorld = template.getWorld().clone(gameUUID.toString());
        swm.generateWorld(gameWorld);

        waitingToGenerate.add(new DuelGenerateWaiting(gameUUID, template, players, type));
    }

    public void worldLoadedCallback(DuelGenerateWaiting game) {
        UUID gameUUID = game.getGameUniqueId();
        TemplateWorld template = game.getTemplateWorld();

        World world = Bukkit.getWorld(gameUUID.toString());
        world.getWorldBorder().setCenter(new Location(world, 0, 64, 0));
        world.getWorldBorder().setSize(PluginConfig.getWorldBorderSize());
        List<Location> teamOneSpawns = new ArrayList<>();
        template.getTeamOneSpawns().forEach(location -> teamOneSpawns.add(location.toBukkitLocation(world)));
        List<Location> teamTwoSpawns = new ArrayList<>();
        template.getTeamTwoSpawns().forEach(location -> teamTwoSpawns.add(location.toBukkitLocation(world)));

        activeGames.put(gameUUID, new Duel(gameUUID, game.getType(), world, template.getName(), game.getPlayers(), teamOneSpawns, teamTwoSpawns));

        DuelWaiting waiting = this.waiting.poll();
        if (waiting != null) {
            beginGame(waiting.getPlayers(), waiting.getType());
        }
    }

    public void endGame(Duel duel) {
        activeGames.remove(duel.getUniqueId());
    }

    public void quitGame(DuelsPlayer player) {
        if (player.isInGame()) {
            player.getCurrentGame().playerQuit(player);
        }
    }

    public DuelGenerateWaiting getDuelWaiting(World world) {
        String name = world.getName();
        for (Iterator<DuelGenerateWaiting> iterator = waitingToGenerate.iterator(); iterator.hasNext();) {
            DuelGenerateWaiting duelGenerateWaiting = iterator.next();
            if (duelGenerateWaiting.doesUniqueIdEqual(name)) {
                iterator.remove();
                return duelGenerateWaiting;
            }
        }
        return null;
    }

    public boolean isBlockAllowed(Material material) {
        return allowedBlocks.contains(material);
    }

    public static DuelController get() {
        if (instance == null) {
            instance = new DuelController();
        }
        return instance;
    }
}
