package com.elytraforce.gunfight.controllers.game;

import com.grinderwolf.swm.api.world.SlimeWorld;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;

public class TemplateWorld {

    private SlimeWorld world;
    private String name;
    private List<WorldlessLocation> teamOneSpawns;
    private List<WorldlessLocation> teamTwoSpawns;
    private List<WorldlessLocation> bombLocations;

    public TemplateWorld(SlimeWorld world, String name, List<String> teamOneSpawns, List<String> teamTwoSpawns, List<String> bombLocations) {
        this.world = world;
        this.name = name;
        this.teamOneSpawns = new ArrayList<>();
        teamOneSpawns.forEach(string -> {
            String[] split = string.split(":");
            TemplateWorld.this.teamOneSpawns.add(new WorldlessLocation(Double.parseDouble(split[0]), Double.parseDouble(split[1]), Double.parseDouble(split[2]), Float.parseFloat(split[3]), Float.parseFloat(split[4])));
        });
        this.teamTwoSpawns = new ArrayList<>();
        teamTwoSpawns.forEach(string -> {
            String[] split = string.split(":");
            TemplateWorld.this.teamTwoSpawns.add(new WorldlessLocation(Double.parseDouble(split[0]), Double.parseDouble(split[1]), Double.parseDouble(split[2]), Float.parseFloat(split[3]), Float.parseFloat(split[4])));
        });
        this.bombLocations = new ArrayList<>();
        bombLocations.forEach(string -> {
            String[] split = string.split(":");
            TemplateWorld.this.bombLocations.add(new WorldlessLocation(Double.parseDouble(split[0]), Double.parseDouble(split[1]), Double.parseDouble(split[2]), Float.parseFloat(split[3]), Float.parseFloat(split[4])));
        });
    }

    public SlimeWorld getWorld() {
        return world;
    }

    public String getName() {
        return name;
    }

    public List<WorldlessLocation> getTeamOneSpawns() {
        return teamOneSpawns;
    }

    public List<WorldlessLocation> getTeamTwoSpawns() {
        return teamTwoSpawns;
    }
    
    public List<WorldlessLocation> getBombLocations() {
    	return bombLocations;
    }

    static class WorldlessLocation {

        private final double x;
        private final double y;
        private final double z;
        private final float yaw;
        private final float pitch;

        public WorldlessLocation(double x, double y, double z, float yaw, float pitch) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.yaw = yaw;
            this.pitch = pitch;
        }

        public Location toBukkitLocation(World world) {
            return new Location(world, x, y, z, yaw, pitch);
        }
    }
}
