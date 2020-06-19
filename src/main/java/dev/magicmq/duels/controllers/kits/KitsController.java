package dev.magicmq.duels.controllers.kits;

import com.google.common.collect.Lists;
import dev.magicmq.duels.Duels;
import dev.magicmq.duels.config.PluginConfig;
import dev.magicmq.duels.controllers.player.DuelsPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import dev.magicmq.duels.utils.ItemUtils;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class KitsController {

    private static KitsController instance;

    private File configFile;
    private FileConfiguration config;

    private Inventory kitsInventory;
    private List<Kit> kits;

    private KitsController() {
        configFile = new File(Duels.get().getDataFolder(), "kits.yml");
        config = YamlConfiguration.loadConfiguration(configFile);

        kits = new ArrayList<>();
        for (String key : config.getKeys(false)) {
            try {
                double cost = config.getDouble(key + ".cost");
                String permission = config.getString(key + ".permission");
                ItemStack[] armor = ItemUtils.itemStackArrayFromBase64(config.getString(key + ".armor"));
                ItemStack[] inventory = ItemUtils.itemStackArrayFromBase64(config.getString(key + ".inventory"));
                kits.add(new Kit(key, cost, permission, armor, inventory));
            } catch (IOException e) {
                Duels.get().getLogger().log(Level.SEVERE, "There was an error when loading a kit from the kits.yml file! See this error:");
                e.printStackTrace();
            }
        }

        ConfigurationSection config = PluginConfig.getKitGui();
        kitsInventory = Bukkit.createInventory(null, config.getInt("settings.slots"), ChatColor.translateAlternateColorCodes('&', config.getString("settings.name")));
        List<Integer> fillerSlots = Lists.newArrayList(config.getString("settings.filler-slots").split(",")).stream().map(Integer::parseInt).collect(Collectors.toList());
        ItemStack fillerItem = ItemUtils.parseGUIItem(config.getConfigurationSection("filler"));
        for (int slot : fillerSlots) {
            kitsInventory.setItem(slot, fillerItem.clone());
        }
        for (String key : config.getKeys(false)) {
            if (key.equals("settings") || key.equals("filler"))
                continue;
            kitsInventory.setItem(config.getInt(key + ".slot"), ItemUtils.parseGUIItem(config.getConfigurationSection(key)));
        }
    }

    public void openKitInventory(Player player) {
        player.openInventory(kitsInventory);
    }

    public boolean isKitsInventory(Inventory inventory) {
        return inventory.getName().equals(kitsInventory.getName());
    }

    public void processClick(DuelsPlayer player, String action, ClickType click) {
        if (action.equals("close")) {
            player.asBukkitPlayer().closeInventory();
        }
        Kit kit = getKitByName(action);
        if (kit != null) {
            if (click == ClickType.RIGHT) {
                if (doesHaveAccessToKit(player, kit)) {
                    player.asBukkitPlayer().closeInventory();
                    player.asBukkitPlayer().sendMessage(PluginConfig.getMessage("already-purchased"));
                } else {
                    purchaseKit(player, kit);
                    player.asBukkitPlayer().closeInventory();
                }
            }
        }
    }

    public void purchaseKit(DuelsPlayer player, Kit kit) {
        if (kit.getCost() > 0) {
            if (Duels.get().getEconomy().getBalance(player.asBukkitPlayer()) - kit.getCost() >= 0) {
                Duels.get().getEconomy().withdrawPlayer(player.asBukkitPlayer(), kit.getCost());
                player.addUnlockedKit(kit.getName());
                player.asBukkitPlayer().sendMessage(PluginConfig.getMessage("purchase-not-enough-money")
                        .replace("%kit%", kit.getName()));
            } else {
                player.asBukkitPlayer().sendMessage(PluginConfig.getMessage("purchase-not-enough-money")
                        .replace("%cost%", new DecimalFormat("###,###,###,###.##").format(kit.getCost())));
            }
        } else {
            player.asBukkitPlayer().sendMessage(PluginConfig.getMessage("not-purchasable"));
        }
    }

    public void createNewKit(Player player, String name, double cost, String permission) {
        if (getKitByName(name) != null) {
            player.sendMessage(ChatColor.RED + "This kit already exists! Delete it first with /duelskit delete " + name + " if you wish to remake it.");
            return;
        }

        ItemStack[] inventory = player.getInventory().getContents();
        ItemStack[] armor = player.getInventory().getArmorContents();

        String[] base64 = ItemUtils.playerInventoryToBase64(player.getInventory());
        config.set(name + ".cost", cost);
        config.set(name + ".permission", permission);
        config.set(name + ".armor", base64[1]);
        config.set(name + ".inventory", base64[0]);
        try {
            config.save(configFile);
            kits.add(new Kit(name, cost, permission, armor, inventory));
            player.sendMessage(ChatColor.GREEN + "Kit " + name + " was successfully registered.");
        } catch (IOException e) {
            player.sendMessage(ChatColor.RED + "There was an error when saving the kits.yml file! See the console for details.");
            e.printStackTrace();
        }
    }

    public void deleteKit(Player player, String name) {
        if (getKitByName(name) == null) {
            player.sendMessage(ChatColor.RED + "No kit was found with the name: " + name);
            return;
        }
        config.set(name + ".inventory", null);
        config.set(name + ".armor", null);
        try {
            config.save(configFile);
            kits.remove(name);
            player.sendMessage(ChatColor.GREEN + "Kit " + name + " was successfully deleted.");
        } catch (IOException e) {
            player.sendMessage(ChatColor.RED + "There was an error when deleting this kit from the kits.yml file! See the console for details.");
            e.printStackTrace();
        }
    }

    public boolean doesHaveAccessToKit(DuelsPlayer player, Kit kit) {
        if (player.asBukkitPlayer().hasPermission(kit.getPermission()))
            return true;
        if (player.getUnlockedKits().contains(kit.getName()))
            return true;

        if (kit.getCost() == -1) {
            return false;
        } else {
            return kit.getCost() == 0;
        }
    }

    public Kit getKitByName(String name) {
        for (Kit kit : kits) {
            if (kit.getName().equals(name))
                return kit;
        }
        return null;
    }

    public static KitsController get() {
        if (instance == null) {
            instance = new KitsController();
        }
        return instance;
    }
}
