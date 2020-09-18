package dev.magicmq.duels.utils;

import com.grinderwolf.swm.api.SlimePlugin;
import com.grinderwolf.swm.api.exceptions.CorruptedWorldException;
import com.grinderwolf.swm.api.exceptions.NewerFormatException;
import com.grinderwolf.swm.api.exceptions.UnknownWorldException;
import com.grinderwolf.swm.api.exceptions.WorldInUseException;
import com.grinderwolf.swm.api.loaders.SlimeLoader;
import com.grinderwolf.swm.api.world.SlimeWorld;
import com.grinderwolf.swm.api.world.properties.SlimePropertyMap;
import dev.magicmq.duels.Duels;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.util.logging.Level;

public class LoadWorldTask extends BukkitRunnable {

    private SlimePlugin swm;
    private SlimeLoader loader;
    private String worldName;
    private SlimePropertyMap properties;
    private Callback<SlimeWorld> callback;

    public LoadWorldTask(SlimePlugin swm, SlimeLoader loader, String worldName, SlimePropertyMap properties, Callback<SlimeWorld> callback) {
        this.swm = swm;
        this.loader = loader;
        this.worldName = worldName;
        this.properties = properties;
        this.callback = callback;
    }

    @Override
    public void run() {
        try {
            SlimeWorld world = swm.loadWorld(loader, worldName, true, properties);
            Bukkit.getScheduler().runTask(Duels.get(), () -> callback.callback(world));
        } catch (UnknownWorldException | IOException | CorruptedWorldException | NewerFormatException | WorldInUseException e) {
            Duels.get().getLogger().log(Level.SEVERE, "(GFT) Error when loading world \"" + worldName + "\":");
            e.printStackTrace();
        }
    }

}
