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

package se.hyperver.hyperverse.teleportation;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import se.hyperver.hyperverse.flags.implementation.EndFlag;
import se.hyperver.hyperverse.flags.implementation.NetherFlag;
import se.hyperver.hyperverse.flags.implementation.WorldPermissionFlag;
import se.hyperver.hyperverse.util.NMS;
import se.hyperver.hyperverse.world.HyperWorld;
import se.hyperver.hyperverse.world.WorldManager;
import se.hyperver.hyperverse.world.WorldType;
import io.papermc.lib.PaperLib;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * {@inheritDoc}
 */
public final class SimpleTeleportationManager implements TeleportationManager {

    private final HyperWorld hyperWorld;
    private final WorldManager worldManager;
    private final NMS nms;

    @Inject public SimpleTeleportationManager(@Assisted HyperWorld hyperWorld,
        final WorldManager worldManager, final NMS nms) {
        this.hyperWorld = hyperWorld;
        this.worldManager = worldManager;
        this.nms = nms;
    }

    @Override public CompletableFuture<Boolean> allowedTeleport(@NotNull final Player player,
        @NotNull final Location location) {
        if (!this.hyperWorld.getFlag(WorldPermissionFlag.class).isEmpty()) {
            final String permission = this.hyperWorld.getFlag(WorldPermissionFlag.class);
            if (!player.hasPermission(permission)) {
                return CompletableFuture.completedFuture(false);
            }
        }
        return CompletableFuture.completedFuture(true);
    }

    @Override public CompletableFuture<Boolean> canTeleport(@NotNull final Player player,
        @NotNull final Location location) {
        return PaperLib.getChunkAtAsync(location).thenApply(chunk ->
            location.getBlock().getRelative(BlockFace.DOWN).getType().isSolid());
    }

    @Override @NotNull public CompletableFuture<Location> findSafe(@NotNull Location location) {
        return PaperLib.getChunkAtAsync(location).thenApply(chunk -> {
            Block locationBlock = location.getBlock();
            do {
                if (locationBlock.getRelative(BlockFace.DOWN).getType().isSolid()) {
                    return locationBlock.getLocation();
                }
            } while (locationBlock.getY() > 0 &&
                (locationBlock = locationBlock.getRelative(BlockFace.DOWN)) != null);
            return location;
        });
    }

    @Override public void teleportPlayer(@NotNull final Player player,
        @NotNull final Location location) {
        PaperLib.teleportAsync(player, Objects.requireNonNull(location)).thenAccept(l ->
            player.setPortalCooldown(100));
    }

    @Override @Nullable public Location netherDestination(@NotNull final Entity entity,
        @NotNull final Location location) {
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

        if (this.hyperWorld.getConfiguration().getType() != WorldType.NETHER &&
            destination.getConfiguration().getType() == WorldType.NETHER) {
            x >>= 3;
            z >>= 3;
        } else if (this.hyperWorld.getConfiguration().getType() == WorldType.NETHER &&
            destination.getConfiguration().getType() != WorldType.NETHER) {
            x <<= 3;
            z <<= 3;
        }

        return new Location(destination.getBukkitWorld(), x, y, z);
    }

    @Override @Nullable public Location endDestination(@NotNull final Entity entity) {
        final String endLinkedWorld = this.hyperWorld.getFlag(EndFlag.class);
        if (endLinkedWorld.isEmpty()) {
            return null;
        }
        final HyperWorld destination = this.worldManager.getWorld(endLinkedWorld);
        if (destination == null || !destination.isLoaded()) {
            return null;
        }
        return nms.getDimensionSpawn(Objects.requireNonNull(destination.getSpawn()));
    }

}
