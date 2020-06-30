package dev.magicmq.duels.controllers.kits;

import dev.magicmq.duels.Duels;
import dev.magicmq.duels.config.PluginConfig;
import dev.magicmq.duels.controllers.player.DuelsPlayer;
import io.github.bananapuncher714.nbteditor.NBTEditor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import dev.magicmq.duels.utils.ItemUtils;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.logging.Level;

public class KitsController {

    private static KitsController instance;

    private File configFile;
    private FileConfiguration config;

    private SortedSet<Kit> kits;
    private Inventory kitsInventory;
    private Kit defaultKit;

    private KitsController() {
        configFile = new File(Duels.get().getDataFolder(), "kits.yml");
        config = YamlConfiguration.loadConfiguration(configFile);

        kits = new TreeSet<>();
        for (String key : config.getKeys(false)) {
            try {
                double cost = config.getDouble(key + ".cost");
                String permission = config.getString(key + ".permission");
                ItemStack[] armor = ItemUtils.itemStackArrayFromBase64(config.getString(key + ".armor"));
                ItemStack[] inventory = ItemUtils.itemStackArrayFromBase64(config.getString(key + ".inventory"));
                ItemStack guiItem = ItemUtils.base64ToItem(config.getString(key + ".gui-representation"));
                kits.add(new Kit(key, cost, permission, armor, inventory, guiItem));
            } catch (IOException e) {
                Duels.get().getLogger().log(Level.SEVERE, "There was an error when loading a kit from the kits.yml file! See this error:");
                e.printStackTrace();
            }
        }

        kitsInventory = Bukkit.createInventory(null, 54, PluginConfig.getKitsGuiName());
        for (Kit kit : kits) {
            kitsInventory.addItem(kit.getGuiRepresentation().clone());
        }

        defaultKit = getKitByName(PluginConfig.getDefaultKit());
    }

    public void openKitsInventory(Player player) {
        player.openInventory(kitsInventory);
    }

    public boolean isKitsInventory(Inventory inventory) {
        return inventory.getName().equals(kitsInventory.getName());
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
                        player.sendMessage(PluginConfig.getMessage("no-kit-access"));
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
        if (kit.getCost() > 0) {
            if (Duels.get().getEconomy().getBalance(player.asBukkitPlayer()) - kit.getCost() >= 0) {
                Duels.get().getEconomy().withdrawPlayer(player.asBukkitPlayer(), kit.getCost());
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
            player.sendMessage(PluginConfig.getMessage("not-purchasable"));
            return false;
        }
    }

    public void createNewKit(Player player, String name, double cost, String permission) {
        if (getKitByName(name) != null) {
            player.sendMessage(ChatColor.RED + "This kit already exists! Delete it first with /duelskit delete " + name + " if you wish to remake it.");
            return;
        }

        ItemStack[] inventory = player.getInventory().getContents().clone();
        ItemStack guiItem = inventory[17];
        inventory[17] = null;
        ItemStack[] armor = player.getInventory().getArmorContents();

        guiItem = NBTEditor.set(guiItem, name, "action");

        String[] base64 = ItemUtils.playerInventoryToBase64(player.getInventory());
        String guiBase64 = ItemUtils.itemStackToBase64(guiItem);
        config.set(name + ".cost", cost);
        config.set(name + ".permission", permission);
        config.set(name + ".armor", base64[1]);
        config.set(name + ".inventory", base64[0]);
        config.set(name + ".gui-representation", guiBase64);
        try {
            config.save(configFile);
            kits.add(new Kit(name, cost, permission, armor, inventory, guiItem));
            kitsInventory.addItem(guiItem);
            player.sendMessage(ChatColor.GREEN + "Kit " + name + " was successfully registered.");
        } catch (IOException e) {
            player.sendMessage(ChatColor.RED + "There was an error when saving the kits.yml file! See the console for details.");
            e.printStackTrace();
        }
    }

    public void deleteKit(Player player, String name) {
        Kit kit = getKitByName(name);
        if (kit == null) {
            player.sendMessage(ChatColor.RED + "No kit was found with the name: " + name);
            return;
        }
        config.set(name, null);
        try {
            config.save(configFile);
            kits.remove(kit);
            kitsInventory.remove(kit.getGuiRepresentation());
            player.sendMessage(ChatColor.GREEN + "Kit " + name + " was successfully deleted.");
        } catch (IOException e) {
            player.sendMessage(ChatColor.RED + "There was an error when deleting this kit from the kits.yml file! See the console for details.");
            e.printStackTrace();
        }
    }

    public boolean doesHaveAccessToKit(DuelsPlayer player, Kit kit) {
        if (kit.equals(defaultKit))
            return true;
        if (player.asBukkitPlayer().hasPermission(kit.getPermission()))
            return true;
        if (player.getUnlockedKits().contains(kit.getName()))
            return true;
        return false;
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

    public static KitsController get() {
        if (instance == null) {
            instance = new KitsController();
        }
        return instance;
    }
}
