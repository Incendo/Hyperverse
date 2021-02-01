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

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import se.hyperver.hyperverse.world.HyperWorld;

import java.util.concurrent.CompletableFuture;

/**
 * Manager responsible for teleportation related actions
 */
public interface TeleportationManager {

    /**
     * Get the spawn location for a player in a particular world. This can be a bed
     * spawn location, the world spawn location, or some other location
     *
     * @param player     Player to check the location for
     * @param hyperWorld The world in which the location exists
     * @return The spawn location
     */
    @NonNull Location getSpawnLocation(
            @NonNull Player player,
            @NonNull HyperWorld hyperWorld
    );

    /**
     * Check whether or not the player is allowed
     * to teleport to the specified location
     *
     * @param player   Player that is about to teleport
     * @param location Location the player is teleporting to
     * @return True if the player is allowed to teleport to the location
     */
    @NonNull CompletableFuture<Boolean> allowedTeleport(
            @NonNull Player player,
            @NonNull Location location
    );

    /**
     * Check whether or not the player can safely teleport
     * to the specified location
     *
     * @param player   Player that is about to teleport
     * @param location Location the player is teleporting to
     * @return True if the player is able to teleport to the location
     */
    @NonNull CompletableFuture<Boolean> canTeleport(
            @NonNull Player player,
            @NonNull Location location
    );

    /**
     * Find a safe teleportation location near the specified
     * location
     *
     * @param location Search origin
     * @return Safe location
     */
    @NonNull CompletableFuture<Location> findSafe(@NonNull Location location);

    /**
     * Teleport the player to a given location
     *
     * @param player   Player to teleport
     * @param location Location to teleport the player to
     */
    void teleportPlayer(
            @NonNull Player player,
            @NonNull Location location
    );

    /**
     * Handle nether portal teleportation
     *
     * @param entity   Entity to teleport
     * @param location Portal location
     * @return The destination location
     */
    @Nullable Location netherDestination(
            @NonNull Entity entity,
            @NonNull Location location
    );

    /**
     * Handle end portal teleportation
     *
     * @param entity Entity to teleport
     * @return The destination location
     */
    @Nullable Location endDestination(@NonNull Entity entity);

}
