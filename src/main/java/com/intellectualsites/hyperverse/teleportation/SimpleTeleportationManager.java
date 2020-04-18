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

package com.intellectualsites.hyperverse.teleportation;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.intellectualsites.hyperverse.flags.implementation.WorldPermissionFlag;
import com.intellectualsites.hyperverse.world.HyperWorld;
import io.papermc.lib.PaperLib;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * {@inheritDoc}
 */
public final class SimpleTeleportationManager implements TeleportationManager {

    private final HyperWorld hyperWorld;

    @Inject public SimpleTeleportationManager(@Assisted HyperWorld hyperWorld) {
        this.hyperWorld = hyperWorld;
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
        PaperLib.teleportAsync(player, Objects.requireNonNull(location));
    }

}
