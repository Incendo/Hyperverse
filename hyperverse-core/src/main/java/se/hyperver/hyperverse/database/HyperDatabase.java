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

package se.hyperver.hyperverse.database;

import co.aikar.taskchain.TaskChainFactory;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.checkerframework.checker.nullness.qual.NonNull;
import se.hyperver.hyperverse.Hyperverse;

import java.util.Collection;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Class containing the database connection that
 * is used throughout the plugin
 */
public abstract class HyperDatabase {

    private final TaskChainFactory taskChainFactory;
    private final Hyperverse hyperverse;
    private final EnumMap<LocationType, Table<UUID, String, PersistentLocation>> locations;

    protected HyperDatabase(
            final @NonNull TaskChainFactory taskChainFactory,
            final @NonNull Hyperverse hyperverse
    ) {
        this.taskChainFactory = taskChainFactory;
        this.hyperverse = hyperverse;
        this.locations = new EnumMap<>(LocationType.class);
        for (final LocationType type : LocationType.values()) {
            this.locations.put(type, HashBasedTable.create());
        }
    }

    /**
     * Attempt to connect to the database
     *
     * @return True if the connection was successful, false if not
     */
    public abstract boolean attemptConnect();

    /**
     * Attempt to close the connection
     */
    public abstract void attemptClose();

    /**
     * Store the location in the database
     *
     * @param persistentLocation Location to store
     * @param updateTable        Whether or not the internal table should be updated
     * @param clear              Whether or not the internal table should be cleared
     */
    public abstract void storeLocation(
            final @NonNull PersistentLocation persistentLocation,
            final boolean updateTable,
            final boolean clear
    );

    /**
     * Remove all stored locations for a specific UUID
     *
     * @param uuid Player UUID
     */
    public void clearLocations(final @NonNull UUID uuid) {
        for (final LocationType locationType : LocationType.values()) {
            final Collection<String> keys =
                    new HashSet<>(this.locations.get(locationType).columnKeySet());
            for (final String key : keys) {
                this.locations.get(locationType).remove(uuid, key);
            }
        }
    }

    /**
     * Query for locations for a given UUID
     *
     * @param uuid Player UUID
     * @return Future that will complete with the locations
     */
    public abstract @NonNull CompletableFuture<Collection<PersistentLocation>> getLocations(final @NonNull UUID uuid);

    /**
     * Get a stored persistent location for a given UUID
     * and world
     *
     * @param uuid         Player UUID
     * @param world        World
     * @param locationType The location type
     * @return Optional containing the location, if it was stored
     */
    @NonNull
    public Optional<PersistentLocation> getLocation(
            final @NonNull UUID uuid,
            final @NonNull String world, final @NonNull LocationType locationType
    ) {
        return Optional.ofNullable(this.locations.get(locationType).get(uuid, world));
    }

    /**
     * Clear all references to a world from the database
     *
     * @param worldName World to remove
     */
    public abstract void clearWorld(final @NonNull String worldName);

    protected final @NonNull TaskChainFactory getTaskChainFactory() {
        return this.taskChainFactory;
    }

    protected final @NonNull Hyperverse getHyperverse() {
        return this.hyperverse;
    }

    protected final @NonNull EnumMap<@NonNull LocationType, @NonNull Table<UUID, String, PersistentLocation>> getLocations() {
        return this.locations;
    }

}
