//
//  Hyperverse - A minecraft world management plugin
//
//  This program is free software: you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published by
//  the Free Software Foundation, either version 3 of the License, or
//  (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.
//
//  You should have received a copy of the GNU General Public License
//  along with this program. If not, see <http://www.gnu.org/licenses/>.
//

package se.hyperver.hyperverse.teleportation;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import io.papermc.lib.PaperLib;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Bed;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import se.hyperver.hyperverse.Hyperverse;
import se.hyperver.hyperverse.configuration.HyperConfiguration;
import se.hyperver.hyperverse.database.HyperDatabase;
import se.hyperver.hyperverse.database.LocationType;
import se.hyperver.hyperverse.database.PersistentLocation;
import se.hyperver.hyperverse.flags.implementation.EndFlag;
import se.hyperver.hyperverse.flags.implementation.IgnoreBedsFlag;
import se.hyperver.hyperverse.flags.implementation.NetherFlag;
import se.hyperver.hyperverse.flags.implementation.WorldPermissionFlag;
import se.hyperver.hyperverse.service.internal.SafeTeleportService;
import se.hyperver.hyperverse.util.NMS;
import se.hyperver.hyperverse.world.HyperWorld;
import se.hyperver.hyperverse.world.WorldManager;
import se.hyperver.hyperverse.world.WorldType;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * {@inheritDoc}
 */
public final class SimpleTeleportationManager implements TeleportationManager {

    private final Hyperverse hyperverse;
    private final HyperWorld hyperWorld;
    private final WorldManager worldManager;
    private final HyperConfiguration configuration;
    private final HyperDatabase hyperDatabase;
    private final NMS nms;

    @Inject
    public SimpleTeleportationManager(
            final @NonNull Hyperverse hyperverse,
            @Assisted final @NonNull HyperWorld hyperWorld,
            final @NonNull WorldManager worldManager,
            final @NonNull NMS nms,
            final @NonNull HyperConfiguration configuration,
            final @NonNull HyperDatabase hyperDatabase
    ) {
        this.hyperverse = hyperverse;
        this.hyperWorld = hyperWorld;
        this.worldManager = worldManager;
        this.nms = nms;
        this.configuration = configuration;
        this.hyperDatabase = hyperDatabase;
    }

    private static boolean hasBedNearby(final @NonNull Location location) {
        return location.getBlock().getBlockData() instanceof Bed;
    }

    @Override

    public @NonNull CompletableFuture<@NonNull Boolean> allowedTeleport(
            final @NonNull Player player,
            final @NonNull Location location
    ) {
        if (!this.hyperWorld.getFlag(WorldPermissionFlag.class).isEmpty()) {
            final String permission = this.hyperWorld.getFlag(WorldPermissionFlag.class);
            if (!player.hasPermission(permission)) {
                return CompletableFuture.completedFuture(false);
            }
        }
        return CompletableFuture.completedFuture(true);
    }

    @Override
    public @NonNull CompletableFuture<@NonNull Boolean> canTeleport(
            final @NonNull Player player,
            final @NonNull Location location
    ) {
        if (this.configuration.shouldSafeTeleport()) {
            return PaperLib.getChunkAtAsync(location).thenApply(
                    chunk -> location.getBlock().getRelative(BlockFace.DOWN).getType().isSolid());
        } else {
            return CompletableFuture.completedFuture(true);
        }
    }

    @Override
    public @NonNull CompletableFuture<@NonNull Location> findSafe(final @NonNull Location location) {
        if (this.configuration.shouldSafeTeleport()) {
            return PaperLib.getChunkAtAsync(location).thenApply(chunk ->
                    this.hyperverse.getServicePipeline().pump(location).through(SafeTeleportService.class)
                            .getResult());
        } else {
            return PaperLib.getChunkAtAsync(location).thenApply(c -> location);
        }
    }

    @Override
    public void teleportPlayer(
            final @NonNull Player player,
            final @NonNull Location location
    ) {
        PaperLib.teleportAsync(player, Objects.requireNonNull(location)).thenAccept(l ->
                player.setPortalCooldown(100));
    }

    @Override
    public @Nullable Location netherDestination(
            final @NonNull Entity entity,
            final @NonNull Location location
    ) {
        final String netherLinkedWorld = this.hyperWorld.getFlag(NetherFlag.class);
        if (netherLinkedWorld.isEmpty()) {
            return null;
        }

        final HyperWorld destination = this.worldManager.getWorld(netherLinkedWorld);
        if (destination == null || !destination.isLoaded()) {
            return null;
        }

        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();

        if (this.hyperWorld.getConfiguration().getType() != WorldType.NETHER
                && destination.getConfiguration().getType() == WorldType.NETHER) {
            x >>= 3;
            z >>= 3;
        } else if (this.hyperWorld.getConfiguration().getType() == WorldType.NETHER
                && destination.getConfiguration().getType() != WorldType.NETHER) {
            x <<= 3;
            z <<= 3;
        }

        return new Location(destination.getBukkitWorld(), x, y, z);
    }

    @Override
    public @Nullable Location endDestination(final @NonNull Entity entity) {
        final String endLinkedWorld = this.hyperWorld.getFlag(EndFlag.class);
        if (endLinkedWorld.isEmpty()) {
            return null;
        }
        final HyperWorld destination = this.worldManager.getWorld(endLinkedWorld);
        if (destination == null || !destination.isLoaded()) {
            return null;
        }
        return this.nms.getDimensionSpawn(Objects.requireNonNull(destination.getSpawn()));
    }

    @Override
    public @NonNull Location getSpawnLocation(
            final @NonNull Player player,
            final @NonNull HyperWorld hyperWorld
    ) {
        if (!this.configuration.shouldPersistLocations() || hyperWorld.getFlag(IgnoreBedsFlag.class)) {
            return Objects.requireNonNull(hyperWorld.getSpawn());
        }
        final Location spawnLocation = this.hyperDatabase.getLocation(player.getUniqueId(), hyperWorld.getConfiguration().getName(),
                LocationType.BED_SPAWN
        ).map(PersistentLocation::toLocation).orElse(null);
        if (spawnLocation != null && hasBedNearby(spawnLocation)) {
            final Location adjustedLocation = this.nms.findBedRespawn(spawnLocation);
            if (adjustedLocation != null) {
                return adjustedLocation;
            }
        }
        return Objects.requireNonNull(hyperWorld.getSpawn());
    }

}
