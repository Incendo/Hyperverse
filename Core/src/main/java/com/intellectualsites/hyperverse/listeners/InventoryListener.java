package com.intellectualsites.hyperverse.listeners;

import com.google.inject.Inject;
import com.intellectualsites.hyperverse.Hyperverse;
import com.intellectualsites.hyperverse.database.HyperDatabase;
import com.intellectualsites.hyperverse.database.PersistentInventory;
import com.intellectualsites.hyperverse.world.HyperWorld;
import com.intellectualsites.hyperverse.world.WorldManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;

public class InventoryListener implements Listener {

    private final HyperDatabase hyperDatabase;
    private final WorldManager worldManager;

    @Inject public InventoryListener(@NotNull Hyperverse hyperverse,
                                     @NotNull final WorldManager worldManager,
                                     @NotNull HyperDatabase hyperDatabase) {
        this.hyperDatabase = hyperDatabase;
        this.worldManager = worldManager;
        Bukkit.getPluginManager().registerEvents(this, Objects.requireNonNull(hyperverse));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerWorldChange(PlayerChangedWorldEvent event) {
        World world = event.getFrom();
        World current = event.getPlayer().getWorld();
        HyperWorld hyperWorld = worldManager.getWorld(world);
        HyperWorld currentHyperWorld = worldManager.getWorld(current);
        //Store old inventory.
        this.hyperDatabase.storeInventory(
                        new PersistentInventory(hyperWorld == null ? world.getUID().toString() : hyperWorld.getWorldUUID().toString(),
                event.getPlayer().getInventory()), true, false);
        Optional<PersistentInventory> optional =
                this.hyperDatabase.getInventory(event.getPlayer().getUniqueId(), currentHyperWorld == null ? current.getUID().toString() : currentHyperWorld.getWorldUUID().toString());
        //Set inventory to the new one.
        optional.ifPresent(persistentInventory -> persistentInventory.setToPlayer(event.getPlayer()));
        if (!optional.isPresent()) {
            event.getPlayer().getInventory().clear(); //Fresh inventory.
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event) {
       this.hyperDatabase.storeInventory(PersistentInventory.fromPlayer(event.getPlayer()), false, true); //Store inventory data from the last world.
    }

}
