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

package se.hyperver.hyperverse.util;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.nio.file.Path;

/**
 * Version specific NMS utility methods
 */
public interface NMS {

    /**
     * Get the nether portal at the given location or create a new one
     *
     * @param entity Entity that is searching for the portal
     * @param origin Origin location
     * @return Portal location
     */
    @Nullable
    Location getOrCreateNetherPortal(
            @NonNull Entity entity,
            @NonNull Location origin
    );

    /**
     * Get the spawn for the dimension containing the given location
     *
     * @param origin Origin location
     * @return Dimension spawn
     */
    @Nullable Location getDimensionSpawn(@NonNull Location origin);

    /**
     * Find the respawn location for a location that contains a bed
     *
     * @param spawnLocation Location to search from
     * @return The bed respawn location, if found
     */
    @Nullable Location findBedRespawn(@NonNull Location spawnLocation);

    /**
     * Save {@link Player player} data to a {@link Path file}
     *
     * @param player Player that owns the data.
     * @param file   File to save the data to.
     */
    void writePlayerData(@NonNull Player player, @NonNull Path file);

    /**
     * Read the {@link Player player} data from a {@link Path file} into the given {@link Player} object
     *
     * @param player   Player to read data into.
     * @param file     File to read data from.
     * @param whenDone Runnable that runs when the reading is complete.
     */
    void readPlayerData(
            @NonNull Player player,
            @NonNull Path file,
            @NonNull Runnable whenDone
    );

}
