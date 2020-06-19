package dev.magicmq.duels.controllers.kits;

import org.bukkit.inventory.ItemStack;

public class Kit {

    private String name;
    private double cost;
    private String permission;
    private ItemStack[] armor;
    private ItemStack[] inventory;

    public Kit(String name, double cost, String permission, ItemStack[] armor, ItemStack[] inventory) {
        this.name = name;
        this.cost = cost;
        this.permission = permission;
        this.armor = armor;
        this.inventory = inventory;
    }

    public String getName() {
        return name;
    }

    public double getCost() {
        return cost;
    }

    public String getPermission() {
        return permission;
    }

    public ItemStack[] getArmor() {
        return armor;
    }

    public ItemStack[] getInventory() {
        return inventory;
    }
}
