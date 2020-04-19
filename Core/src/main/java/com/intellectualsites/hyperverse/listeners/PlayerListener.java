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

import com.intellectualsites.hyperverse.Hyperverse;
import com.intellectualsites.hyperverse.configuration.HyperConfiguration;
import com.intellectualsites.hyperverse.configuration.Messages;
import com.intellectualsites.hyperverse.database.HyperDatabase;
import com.intellectualsites.hyperverse.database.PersistentLocation;
import com.intellectualsites.hyperverse.flags.implementation.EndFlag;
import com.intellectualsites.hyperverse.flags.implementation.GamemodeFlag;
import com.intellectualsites.hyperverse.flags.implementation.LocalRespawnFlag;
import com.intellectualsites.hyperverse.flags.implementation.NetherFlag;
import com.intellectualsites.hyperverse.flags.implementation.PveFlag;
import com.intellectualsites.hyperverse.flags.implementation.PvpFlag;
import com.intellectualsites.hyperverse.util.MessageUtil;
import com.intellectualsites.hyperverse.util.NMS;
import com.intellectualsites.hyperverse.world.HyperWorld;
import com.intellectualsites.hyperverse.world.WorldManager;
import com.intellectualsites.hyperverse.world.WorldType;
import io.papermc.lib.PaperLib;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import javax.inject.Inject;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class PlayerListener implements Listener {

    private final WorldManager worldManager;
    private final HyperDatabase hyperDatabase;
    private final HyperConfiguration hyperConfiguration;
    private final Hyperverse hyperverse;
    private final NMS nms;

    @Inject public PlayerListener(final WorldManager worldManager,
        final HyperDatabase hyperDatabase, final HyperConfiguration hyperConfiguration,
        final Hyperverse hyperverse, final NMS nms) {
        this.worldManager = worldManager;
        this.hyperDatabase = hyperDatabase;
        this.hyperConfiguration = hyperConfiguration;
        this.hyperverse = hyperverse;
        this.nms = nms;
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
        if (hyperWorld.getConfiguration().getType() == WorldType.END &&
            event.getPlayer().getLocation().getBlock().getType() == Material.END_PORTAL) {
            final Location destination = hyperWorld.getTeleportationManager()
                .endDestination(event.getPlayer());
            if (destination != null) {
                final boolean allowedEntry = hyperWorld.getTeleportationManager().allowedTeleport(event.getPlayer(), destination)
                    .getNow(false);
                if (!allowedEntry) {
                    MessageUtil.sendMessage(event.getPlayer(), Messages.messageNotPermittedEntry);
                } else {
                    event.setRespawnLocation(destination);
                }
            }
        } else if (hyperWorld.getFlag(LocalRespawnFlag.class)) {
            event.setRespawnLocation(Objects.requireNonNull(hyperWorld.getSpawn()));
        }
    }

    @EventHandler
    public void onEntityDamageEvent(final EntityDamageByEntityEvent event) {
        final HyperWorld hyperWorld = this.worldManager.getWorld(event.getEntity().getWorld());
        if (hyperWorld == null) {
            return;
        }
        final Entity first = event.getEntity();
        final Entity second = event.getDamager();
        if (first.getType() == EntityType.PLAYER || second.getType() == EntityType.PLAYER) {
            if (first.getType() == second.getType()) {
                if (!hyperWorld.getFlag(PvpFlag.class)) {
                    event.setCancelled(true);
                }
            } else {
                if (!hyperWorld.getFlag(PveFlag.class)) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerPortalEvent(final PlayerPortalEvent event) {
        final HyperWorld hyperWorld = this.worldManager.getWorld(event.getPlayer().getWorld());
        if (hyperWorld == null) {
            return;
        }
        if (event.getCause() == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL) {
            final Location destination = hyperWorld.getTeleportationManager()
                .netherDestination(event.getPlayer(), event.getFrom());
            if (destination != null) {
                final boolean allowedEntry = hyperWorld.getTeleportationManager().allowedTeleport(event.getPlayer(), destination)
                    .getNow(false);
                if (!allowedEntry) {
                    MessageUtil.sendMessage(event.getPlayer(), Messages.messageNotPermittedEntry);
                } else {
                    event.setTo(destination);
                }
            }
        } else if (event.getCause() == PlayerTeleportEvent.TeleportCause.END_PORTAL) {
            final Location destination = hyperWorld.getTeleportationManager()
                .endDestination(event.getPlayer());
            if (destination != null) {
                final boolean allowedEntry = hyperWorld.getTeleportationManager().allowedTeleport(event.getPlayer(), destination)
                    .getNow(false);
                if (!allowedEntry) {
                    MessageUtil.sendMessage(event.getPlayer(), Messages.messageNotPermittedEntry);
                } else {
                    event.setTo(destination);
                }
            }
        }
    }

    @EventHandler
    public void onEntityPortalEnter(final EntityPortalEnterEvent event) {
        if (event.getEntityType() == EntityType.PLAYER) {
            return;
        }
        final HyperWorld hyperWorld = this.worldManager.getWorld(
            Objects.requireNonNull(event.getLocation().getWorld()));
        if (hyperWorld == null) {
            return;
        }

        if (event.getEntity().hasMetadata("hv-portal-throttle")) {
            final MetadataValue value = event.getEntity().getMetadata("hv-portal-throttle").get(0);
            if (System.currentTimeMillis() - value.asLong() < 5000L) {
                return;
            }
        }

        if (event.getLocation().getBlock().getType() == Material.NETHER_PORTAL &&
            !hyperWorld.getFlag(NetherFlag.class).isEmpty()) {
            final Location destination = hyperWorld.getTeleportationManager()
                .netherDestination(event.getEntity(), event.getLocation());
            if (destination != null) {
                // Destination is the location from which we want to search, now we need to find the
                // actual portal destination
                final Location location = nms.getOrCreateNetherPortal(event.getEntity(), destination);
                if (location != null) {
                    event.getEntity().setMetadata("hv-portal-throttle", new FixedMetadataValue(this.hyperverse, System.currentTimeMillis()));
                    PaperLib.teleportAsync(event.getEntity(), location, PlayerTeleportEvent.TeleportCause.COMMAND);
                } else {
                    hyperverse.getLogger().warning(String.format("Failed to find/create a portal surrounding %s",
                        location.toString()));
                }
            }
        } else if (event.getLocation().getBlock().getType() == Material.END_PORTAL &&
                   !hyperWorld.getFlag(EndFlag.class).isEmpty()) {
            final Location destination = hyperWorld.getTeleportationManager().endDestination(event.getEntity());
            if (destination != null) {
                PaperLib.teleportAsync(event.getEntity(), destination, PlayerTeleportEvent.TeleportCause.COMMAND);
            }
        }
    }

    @EventHandler
    public void onEntityPortalEvent(final EntityPortalEvent event) {
        final HyperWorld hyperWorld = this.worldManager.getWorld(
            Objects.requireNonNull(event.getFrom().getWorld()));
        if (hyperWorld == null) {
            return;
        }
        if (event.getFrom().getBlock().getType() == Material.NETHER_PORTAL &&
            !hyperWorld.getFlag(NetherFlag.class).isEmpty()) {
            event.setCancelled(true);
        } else if (event.getFrom().getBlock().getType() == Material.END_PORTAL &&
                !hyperWorld.getFlag(EndFlag.class).isEmpty()) {
            event.setCancelled(true);
        }
    }

}
