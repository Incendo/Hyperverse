//
// Hyperverse - A minecraft world management plugin
// Copyright © 2020 Alexander Söderberg (sauilitired@gmail.com)
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

import com.intellectualsites.hyperverse.configuration.HyperConfiguration;
import com.intellectualsites.hyperverse.database.HyperDatabase;
import com.intellectualsites.hyperverse.database.PersistentLocation;
import com.intellectualsites.hyperverse.flags.implementation.GamemodeFlag;
import com.intellectualsites.hyperverse.flags.implementation.LocalRespawnFlag;
import com.intellectualsites.hyperverse.world.HyperWorld;
import com.intellectualsites.hyperverse.world.WorldManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import javax.inject.Inject;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class PlayerListener implements Listener {

    private final WorldManager worldManager;
    private final HyperDatabase hyperDatabase;
    private final HyperConfiguration hyperConfiguration;

    @Inject public PlayerListener(final WorldManager worldManager,
        final HyperDatabase hyperDatabase, final HyperConfiguration hyperConfiguration) {
        this.worldManager = worldManager;
        this.hyperDatabase = hyperDatabase;
        this.hyperConfiguration = hyperConfiguration;
    }

    @EventHandler
    public void onPlayerLogin(final AsyncPlayerPreLoginEvent event) {
        if (hyperConfiguration.shouldPersistLocations()) {
            try {
                this.hyperDatabase.getLocations(event.getUniqueId()).get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    @EventHandler
    public void onTeleport(final PlayerTeleportEvent event) {
        if (hyperConfiguration.shouldPersistLocations()) {
            final Location from = event.getFrom();
            final Location to = event.getTo();
            if (Objects.equals(from.getWorld(), Objects.requireNonNull(to).getWorld())) {
                // The player was moving inside of the world, so we don't
                // need to update the location
                return;
            }
            // The player moved between two different worlds, so we
            // need to update
            final UUID uuid = event.getPlayer().getUniqueId();
            this.hyperDatabase.storeLocation(PersistentLocation.fromLocation(uuid, from), true, false);
            this.hyperDatabase.storeLocation(PersistentLocation.fromLocation(uuid, to), true, false);
        }
    }

    @EventHandler
    public void onPlayerQuit(final PlayerQuitEvent event) {
        if (hyperConfiguration.shouldPersistLocations()) {
            // Persist the locations when the player quits
            final UUID uuid = event.getPlayer().getUniqueId();
            this.hyperDatabase.storeLocation(
                PersistentLocation.fromLocation(uuid, event.getPlayer().getLocation()), false, true);
            this.hyperDatabase.clearLocations(uuid);
        }
    }

    @EventHandler
    public void onWorldChange(final PlayerChangedWorldEvent event) {
        final Player player = event.getPlayer();
        final HyperWorld hyperWorld = this.worldManager.getWorld(player.getWorld());
        if (hyperWorld == null) {
            return;
        }
        player.setGameMode(hyperWorld.getFlag(GamemodeFlag.class));
    }

    @EventHandler
    public void onRespawn(final PlayerRespawnEvent event) {
        final Player player = event.getPlayer();
        final HyperWorld hyperWorld = this.worldManager.getWorld(player.getWorld());
        if (hyperWorld == null) {
            return;
        }
        if (hyperWorld.getFlag(LocalRespawnFlag.class)) {
            event.setRespawnLocation(Objects.requireNonNull(hyperWorld.getSpawn()));
        }
    }

}
