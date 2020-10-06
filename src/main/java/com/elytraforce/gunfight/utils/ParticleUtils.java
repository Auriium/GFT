package com.elytraforce.gunfight.utils;

import java.util.ArrayList;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.Particle.DustOptions;

public class ParticleUtils {

	
	public static void particleCircle(Location location, Particle particle, int amount) {
    	ArrayList<Location> points = getCircle(location, 4.0, 500);
    	
    	for (Location loc:points) {
    		loc.getWorld().spawnParticle(particle, loc, 1, new DustOptions(Color.RED, 1));
    	}
    }
    
    private static ArrayList<Location> getCircle(Location center, double radius, int amount)
    {
        World world = center.getWorld();
        double increment = (2 * Math.PI) / amount;
        ArrayList<Location> locations = new ArrayList<Location>();
        for(int i = 0;i < amount; i++)
        {
            double angle = i * increment;
            double x = center.getX() + (radius * Math.cos(angle));
            double z = center.getZ() + (radius * Math.sin(angle));
            locations.add(new Location(world, x, center.getY(), z));
        }
        return locations;
    }
}
