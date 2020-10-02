package com.elytraforce.gunfight.controllers.kits;

import io.github.bananapuncher714.nbteditor.NBTEditor;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;
import org.ipvp.canvas.Menu;
import org.ipvp.canvas.slot.Slot;
import org.ipvp.canvas.template.ItemStackTemplate;
import org.ipvp.canvas.type.ChestMenu;

import com.elytraforce.gunfight.Main;
import com.elytraforce.gunfight.config.PluginConfig;
import com.elytraforce.gunfight.controllers.player.DuelsPlayer;
import com.elytraforce.gunfight.controllers.player.PlayerController;
import com.elytraforce.gunfight.utils.ItemUtils;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;

public class KitsController {

    private static KitsController instance;

    private final File configFile;
    private final FileConfiguration config;

    private final HashSet<Kit> kits;
    private String kitsInventoryName;
    private ItemStack kitGuiNoAccessItem;
    private final Kit defaultKit;

    
    
    private KitsController() {
        configFile = new File(Main.get().getDataFolder(), "kits.yml");
        config = YamlConfiguration.loadConfiguration(configFile);

        kits = new HashSet<>();
        for (String key : config.getKeys(false)) {
            try {
                double cost = config.getDouble(key + ".cost");
                String permission = config.getString(key + ".permission");
                ItemStack[] armor = ItemUtils.itemStackArrayFromBase64(config.getString(key + ".armor"));
                ItemStack[] inventory = ItemUtils.itemStackArrayFromBase64(config.getString(key + ".inventory"));
                ItemStack guiItem = ItemUtils.base64ToItem(config.getString(key + ".gui-representation"));
                int guiSlot = config.getInt(key + ".gui-slot");
                boolean isHidden = config.getBoolean(key + ".is-hidden");
                kits.add(new Kit(key, cost, permission, armor, inventory, guiItem, guiSlot, isHidden));
            } catch (IOException e) {
                Main.get().getLogger().log(Level.SEVERE, "There was an error when loading a kit from the kits.yml file! See this error:");
                e.printStackTrace();
            }
        }

        kitsInventoryName = PluginConfig.getKitsGuiName();
        kitGuiNoAccessItem = PluginConfig.getKitGuiNoAccessItem();

        defaultKit = getKitByName(PluginConfig.getDefaultKit());
    }

    public void openKitsInventory(Player player) {
    	
    	
    	//use canvas
    	Menu menu = ChestMenu.builder(4)
                .title(PluginConfig.getKitsGuiName())
                .build();
    	
    	for (Kit kit : kits) {
    		
    		if (kit.isKitHidden()) {
    			continue;
    		}
            	Slot slot = menu.getSlot(kit.getGuiSlot());
            	//make item template
            	slot.setItemTemplate(p -> {
            	    ItemStack item = kit.getGuiRepresentation().clone();
            	    ItemMeta itemMeta = item.getItemMeta();
            	   
            	    
            	    if (!doesHaveAccessToKit(PlayerController.get().getDuelsPlayer(player), kit)) {
            	    	item.setType(Material.GRAY_DYE);
            	    	List<String> lore = itemMeta.getLore();
            	    	lore.add("");
                        lore.add(ChatColor.translateAlternateColorCodes('&', "&c&lLOCKED"));
                        itemMeta.setLore(lore);
            	    }
            	    
            	    
            	    
            	    item.setItemMeta(itemMeta);
            	    return item;
            	});
            	//set up click handler
            	slot.setClickHandler((p, info) -> {
                    // Additional functionality goes here
                    ItemStack item = slot.getItem(p);
                    if (NBTEditor.contains(item, "action")) {
                        KitsController.get().processClick(PlayerController.get().getDuelsPlayer(p), NBTEditor.getString(item, "action"));
                    }
                });
        }
    	
        menu.open(player);
    	
        //Inventory inventory = Bukkit.createInventory(null, 54, PluginConfig.getKitsGuiName());
        //for (Kit kit : kits) {
        //    if (doesHaveAccessToKit(PlayerController.get().getDuelsPlayer(player), kit)) {
        //        inventory.setItem(kit.getGuiSlot(), kit.getGuiRepresentation().clone());
        //    } else {
        //    	//todo: replace this with Aesthetik's item factory. idk why i paid to do this when 
        //    	// i could have gotten source and done this really fast for free lmfao
        //        ItemStack item = kit.getGuiRepresentation().clone();
        //        item.setType(Material.GRAY_DYE);
        //        ItemMeta meta = item.getItemMeta();
        //        List<String> lore = meta.getLore();
        //        lore.add("");
        //        lore.add(ChatColor.translateAlternateColorCodes('&', "&c&lLOCKED"));
        //        meta.setLore(lore);
        //        item.setItemMeta(meta);
        //        inventory.setItem(kit.getGuiSlot(), item);
        //    }
        //}
        //player.openInventory(inventory);
    }

    public boolean isKitsInventory(InventoryView view) {
        return kitsInventoryName.equals(view.getTitle());
    }

    public void processClick(DuelsPlayer player, String action) {
    	
    	
        if (action.equals("close")) {
            player.asBukkitPlayer().closeInventory();
        }
        Kit kit = getKitByName(action);
        if (kit != null) {
            if (player.isInGame()) {
                if (doesHaveAccessToKit(player, kit)) {
                    player.setSelectedKit(kit);
                    player.sendMessage(PluginConfig.getMessage("kit-selected")
                            .replace("%kit%", kit.getName()));
                    player.asBukkitPlayer().playSound(player.asBukkitPlayer().getLocation(), Sound.ENTITY_PLAYER_LEVELUP, PluginConfig.getSoundVolume(), 1f);
                } else {
                    if (!purchaseKit(player, kit)) {
                        //player.sendMessage(PluginConfig.getMessage("no-kit-access"));
                        player.asBukkitPlayer().playSound(player.asBukkitPlayer().getLocation(), Sound.UI_BUTTON_CLICK, PluginConfig.getSoundVolume(), 1f);
                    } else {
                        player.setSelectedKit(kit);
                        player.sendMessage(PluginConfig.getMessage("kit-selected")
                                .replace("%kit%", kit.getName()));
                    }
                }
            } else {
                if (!doesHaveAccessToKit(player, kit)) {
                	
                	
                    purchaseKit(player, kit);
                } else {
                    player.sendMessage(PluginConfig.getMessage("already-have-access"));
                    player.asBukkitPlayer().playSound(player.asBukkitPlayer().getLocation(), Sound.UI_BUTTON_CLICK, PluginConfig.getSoundVolume(), 1f);
                }
                
            }
            player.asBukkitPlayer().closeInventory();
        }
    }

    public boolean purchaseKit(DuelsPlayer player, Kit kit) {
        if (player.asBukkitPlayer().hasPermission(kit.getPermission())) {
            if (Main.get().getEconomy().getBalance(player.asBukkitPlayer()) - kit.getCost() >= 0) {
                Main.get().getEconomy().withdrawPlayer(player.asBukkitPlayer(), kit.getCost());
                player.addUnlockedKit(kit.getName());
                player.sendMessage(PluginConfig.getMessage("kit-purchase")
                        .replace("%kit%", kit.getName()));
                player.asBukkitPlayer().playSound(player.asBukkitPlayer().getLocation(), Sound.ENTITY_PLAYER_LEVELUP, PluginConfig.getSoundVolume(), 2f);
                return true;
            } else {
                player.sendMessage(PluginConfig.getMessage("purchase-not-enough-money")
                        .replace("%kit%", kit.getName())
                        .replace("%cost%", new DecimalFormat("###,###,###,###.##").format(kit.getCost())));
                return false;
            }
        } else {
            player.sendMessage(PluginConfig.getMessage("kit-no-permission"));
            return false;
        }
    }

    public void createNewKit(Player player, String name, double cost, String permission, int guiSlot, boolean hidden) {
        if (getKitByName(name) != null) {
            player.sendMessage(ChatColor.RED + "This kit already exists! Delete it first with /duelskit delete " + name + " if you wish to remake it.");
            return;
        }

        ItemStack[] inventory = player.getInventory().getContents().clone();
        ItemStack guiItem = inventory[17];
        inventory[17] = null;
        player.getInventory().remove(guiItem);
        ItemStack[] armor = player.getInventory().getArmorContents();

        guiItem = NBTEditor.set(guiItem, name, "action");

        String[] base64 = ItemUtils.playerInventoryToBase64(player.getInventory());
        String guiBase64 = ItemUtils.itemStackToBase64(guiItem);
        config.set(name + ".cost", cost);
        config.set(name + ".permission", permission);
        config.set(name + ".armor", base64[1]);
        config.set(name + ".inventory", base64[0]);
        config.set(name + ".gui-representation", guiBase64);
        config.set(name + ".gui-slot", guiSlot);
        config.set(name + ".is-hidden", hidden);
        try {
            config.save(configFile);
            kits.add(new Kit(name, cost, permission, armor, inventory, guiItem, guiSlot, hidden));
            player.getInventory().clear();
            player.sendMessage(ChatColor.GREEN + "Kit " + name + " was successfully registered.");
        } catch (IOException e) {
            player.sendMessage(ChatColor.RED + "There was an error when saving the kits.yml file! See the console for details.");
            e.printStackTrace();
        }
    }

    public void deleteKit(CommandSender sender, String name) {
        Kit kit = getKitByName(name);
        if (kit == null) {
            sender.sendMessage(ChatColor.RED + "No kit was found with the name: " + name);
            return;
        }
        config.set(name, null);
        try {
            config.save(configFile);
            kits.remove(kit);
            sender.sendMessage(ChatColor.GREEN + "Kit " + name + " was successfully deleted.");
        } catch (IOException e) {
            sender.sendMessage(ChatColor.RED + "There was an error when deleting this kit from the kits.yml file! See the console for details.");
            e.printStackTrace();
        }
    }

    public boolean doesHaveAccessToKit(DuelsPlayer player, Kit kit) {
        if (kit.equals(defaultKit))
            return true;
        return player.getUnlockedKits().contains(kit.getName());
    }

    public Kit getKitByName(String name) {
        for (Kit kit : kits) {
            if (kit.getName().equals(name))
                return kit;
        }
        return null;
    }

    public Kit getDefaultKit() {
        return defaultKit;
    }

    public Collection<Kit> getKits() {
        return kits;
    }

    public static KitsController get() {
        if (instance == null) {
            instance = new KitsController();
        }
        return instance;
    }
}
