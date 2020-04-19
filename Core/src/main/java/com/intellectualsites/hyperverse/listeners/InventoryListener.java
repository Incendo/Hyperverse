//
// Hyperverse - A Minecraft world management plugin
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program. If not, see <http://www.gnu.org/licenses/>.
//

package com.intellectualsites.hyperverse.listeners;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.intellectualsites.hyperverse.database.HyperDatabase;
import com.intellectualsites.hyperverse.database.PersistentInventory;
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
        optional.ifPresent(PersistentInventory::toInventory);
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
