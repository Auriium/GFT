package com.elytraforce.gunfight.controllers.kits;

import org.bukkit.inventory.ItemStack;

public class Kit implements Comparable<Kit> {

    private final String name;
    private final double cost;
    private final String permission;
    private final ItemStack[] armor;
    private final ItemStack[] inventory;
    private final ItemStack guiRepresentation;
    private final int guiSlot;
    private final boolean isHidden;

    public Kit(String name, double cost, String permission, ItemStack[] armor, ItemStack[] inventory, ItemStack guiRepresentation, int guiSlot, boolean isHidden) {
        this.name = name;
        this.cost = cost;
        this.permission = permission;
        this.armor = armor;
        this.inventory = inventory;
        this.guiRepresentation = guiRepresentation;
        this.guiSlot = guiSlot;
		this.isHidden = isHidden;
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

    public ItemStack getGuiRepresentation() {
        return guiRepresentation;
    }

    public int getGuiSlot() {
        return guiSlot;
    }
    
    public boolean isKitHidden() {
    	return this.isHidden;
    }

    @Override
    public int compareTo(Kit kit) {
        return name.compareTo(kit.name);
    }
}
