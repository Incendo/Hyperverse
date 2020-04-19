package com.intellectualsites.hyperverse.listeners;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.intellectualsites.hyperverse.database.HyperDatabase;
import com.intellectualsites.hyperverse.database.PersistentInventory;
import com.intellectualsites.hyperverse.world.HyperWorld;
import com.intellectualsites.hyperverse.world.WorldManager;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

@Singleton
public class InventoryListener implements Listener {

    private final HyperDatabase hyperDatabase;

    @Inject public InventoryListener(@NotNull HyperDatabase hyperDatabase) {
        this.hyperDatabase = hyperDatabase;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerWorldChange(final PlayerChangedWorldEvent event) {
        final World world = event.getFrom();
        final World current = event.getPlayer().getWorld();
        //Store old inventory.
        this.hyperDatabase.storeInventory(
                        new PersistentInventory(world.getName(),
                event.getPlayer().getInventory()), true, false);
        final Optional<PersistentInventory> optional = this.hyperDatabase.getInventory(event.getPlayer().getUniqueId(),current.getName());
        event.getPlayer().getInventory().clear(); //Fresh inventory.
        //Set inventory to the new one.
        optional.ifPresent(persistentInventory -> persistentInventory.setToPlayer(event.getPlayer()));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoin(final PlayerJoinEvent event) {
        this.hyperDatabase.getInventories(event.getPlayer().getUniqueId()); //Load inventories into memory.
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerQuit(final PlayerQuitEvent event) {
       this.hyperDatabase.storeInventory(PersistentInventory.fromPlayer(event.getPlayer()), false, true); //Store inventory data from the last world.
        this.hyperDatabase.clearInventories(event.getPlayer().getUniqueId());
    }

}
