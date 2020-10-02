package com.elytraforce.gunfight.controllers.game;

import com.elytraforce.gunfight.Main;
import com.elytraforce.gunfight.config.PluginConfig;
import com.elytraforce.gunfight.controllers.QueueController;
import com.elytraforce.gunfight.controllers.game.Duel.Team;
import com.elytraforce.gunfight.controllers.game.gamemodes.GameType;
import com.elytraforce.gunfight.controllers.player.DuelsPlayer;
import com.elytraforce.gunfight.controllers.player.PlayerController;
import com.elytraforce.gunfight.utils.LoadWorldTask;
import com.grinderwolf.swm.api.SlimePlugin;
import com.grinderwolf.swm.api.exceptions.InvalidWorldException;
import com.grinderwolf.swm.api.exceptions.WorldAlreadyExistsException;
import com.grinderwolf.swm.api.exceptions.WorldLoadedException;
import com.grinderwolf.swm.api.exceptions.WorldTooBigException;
import com.grinderwolf.swm.api.loaders.SlimeLoader;
import com.grinderwolf.swm.api.world.SlimeWorld;
import com.grinderwolf.swm.api.world.properties.SlimeProperties;
import com.grinderwolf.swm.api.world.properties.SlimePropertyMap;

import io.github.bananapuncher714.nbteditor.NBTEditor;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.StringUtils;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.ipvp.canvas.Menu;
import org.ipvp.canvas.slot.Slot;
import org.ipvp.canvas.slot.Slot.ClickHandler;
import org.ipvp.canvas.type.ChestMenu;

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
        File[] worlds = new File(Main.get().getDataFolder(), "template_worlds").listFiles();
        Bukkit.getScheduler().runTaskAsynchronously(Main.get(), () -> {
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
                Main.get().getLogger().log(Level.SEVERE, "(GFT) Error when importing template world:");
                e.printStackTrace();
            }
        });

        allowedBlocks = new ArrayList<>();
        for (String material : PluginConfig.getAllowedBlocks()) {
            allowedBlocks.add(Material.getMaterial(material.toUpperCase()));
        }

        Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.get(), () -> {
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
    
    public void displayGUI(DuelsPlayer player) {
    	Menu menu = ChestMenu.builder(4)
                .title(ChatColor.translateAlternateColorCodes('&', "&9Spectate a match"))
                .build();
    	for (Duel duel : this.activeGames.values()) {
    		Slot slot = menu.iterator().next();
    		
    		
    		slot.setItemTemplate(slot1 -> {
    			ItemStack item = new ItemStack(Material.IRON_SWORD);
    			ItemMeta meta = item.getItemMeta();
    			List<String> lore = new ArrayList<String>();
    			String mapName = 
    			StringUtils.capitalize(duel.getMapName());
    		
    			duel.getType();
    			switch (duel.getType()) {
    				case ONE_V_ONE:
    					item.setType(Material.IRON_SWORD);
        				meta.setDisplayName(colorString("&7One vs One - &e&l" + mapName ));
        				lore.add(colorString(""));
        				lore.add(colorString("&7A casual 1 vs 1 match."));
    				case TWO_V_TWO:
    					item.setType(Material.GOLDEN_SWORD);
        				meta.setDisplayName(colorString("&7Two vs Two - &e&l" + mapName ));
        				lore.add(colorString(""));
        				lore.add(colorString("&7A casual 2 vs 2 match."));
    				case THREE_V_THREE:
    					item.setType(Material.DIAMOND_SWORD);
        				meta.setDisplayName(colorString("&9&lThree vs Three - &e&l" + mapName ));
        				lore.add(colorString(""));
        				lore.add(colorString("&7A casual 3 vs 3 match."));
    				case TWO_V_TWO_BOMB:
    					item.setType(Material.GOLDEN_SWORD);
        				meta.setDisplayName(colorString("&7Two vs Two &c&lBOMB&7 - &e&l" + mapName ));
        				lore.add(colorString(""));
        				lore.add(colorString("&7A 2 vs 2 match."));
        				lore.add(colorString("&7Objective: Defuse the Bomb!"));
    				case THREE_V_THREE_BOMB:
    					item.setType(Material.DIAMOND_SWORD);
        				meta.setDisplayName(colorString("&9&lThree vs Three &c&lBOMB&7 - &e&l" + mapName ));
        				lore.add(colorString(""));
        				lore.add(colorString("&7A 3 vs 3 match."));
        				lore.add(colorString("&7Objective: Defuse the Bomb!"));
    			}

    			lore.add(colorString(""));
    			lore.add(colorString("&7--Match Players--"));
    			for (DuelsPlayer sub : duel.getPlayers(Team.ONE)) {
    				lore.add(colorString("&c» " + sub.getName()));
    			}
    			for (DuelsPlayer sub : duel.getPlayers(Team.TWO)) {
    				lore.add(colorString("&9» " + sub.getName()));
    			}
    			lore.add(colorString(""));
    			lore.add(colorString("&eClick to Join!"));
    		
    		
    			item.addEnchantment(Enchantment.DAMAGE_UNDEAD, 1);
    			meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
    			meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

    			meta.setLore(lore);
    			item.setItemMeta(meta);
    			return item;
    		});
    		slot.setClickHandler((p, info) -> {
                // Additional functionality goes here
                duel.addSpectator(player);
            });
    			
    	}
    	
    	menu.open(player.asBukkitPlayer());
    	player.asBukkitPlayer().playSound(player.asBukkitPlayer().getLocation(), Sound.UI_BUTTON_CLICK, PluginConfig.getSoundVolume(), 1f);
    }
    
    public String colorString(String string) {
    	return ChatColor.translateAlternateColorCodes('&', string);
    }

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
            }).runTaskAsynchronously(Main.get());
        }
    }

    public void queueGame(HashSet<DuelsPlayer> players, GameType.Type type) {
        if (waitingToGenerate.size() == 0) {
            beginGame(players, type);
        } else {
            waiting.offer(new DuelWaiting(players, type));
        }
    }

    public void beginGame(HashSet<DuelsPlayer> players, GameType.Type type) {
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

        worldLoadedCallback(new DuelGenerateWaiting(gameUUID, template, players, type));
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
