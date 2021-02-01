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

package se.hyperver.hyperverse.world;

import org.bukkit.World;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Objects;

/**
 * Manages {@link HyperWorld worlds}
 */
public interface WorldManager {

    /**
     * Attempt to import a world that has already been loaded by bukkit
     *
     * @param world     Loaded world
     * @param vanilla   Whether or not the world is a vanilla world
     * @param generator The generator name. If this is null, the generator
     *                  will be guessed from the chunk generator
     * @return The result of the import
     */
    WorldImportResult importWorld(@NonNull World world, boolean vanilla, @Nullable String generator);

    /**
     * Load all pre-configured worlds. This will not create the worlds,
     * just load them into the system
     */
    void loadWorlds();

    /**
     * Trigger the creation of all non-existent, but loaded worlds
     */
    void createWorlds();

    /**
     * Add the world to the manager and create the configuration file
     *
     * @param hyperWorld World to add
     */
    void addWorld(@NonNull HyperWorld hyperWorld);

    /**
     * Register the world internally
     *
     * @param hyperWorld World to register
     */
    void registerWorld(@NonNull HyperWorld hyperWorld);

    /**
     * Get all registered worlds
     *
     * @return Immutable view of all recognized worlds
     */
    @NonNull Collection<HyperWorld> getWorlds();

    /**
     * Get a world using its name
     *
     * @param name World name
     * @return World, if it exists
     */
    @Nullable HyperWorld getWorld(@NonNull String name);

    /**
     * Get a world from a Bukkit world
     *
     * @param world Bukkit world
     * @return World, if it exists
     */
    @Nullable HyperWorld getWorld(@NonNull World world);

    /**
     * Make a world ignored, this means that it won't
     * be registered by the world manager when
     * it is initialized
     *
     * @param world World to ignore
     */
    void ignoreWorld(@NonNull String world);

    /**
     * Check whether or not a world is ignored
     *
     * @param name World name
     * @return True if the world is ignored
     * @see #ignoreWorld(String) To ignore a world
     */
    boolean shouldIgnore(@NonNull String name);

    /**
     * Get the directory containing world configurations
     *
     * @return Path to configurations
     */
    @NonNull Path getWorldDirectory();

    /**
     * Remove a world. This will not delete the world,
     * just remove it from the internal maps
     *
     * @param hyperWorld World to remove
     */
    void unregisterWorld(@NonNull HyperWorld hyperWorld);

    /**
     * Result of attempts to import worlds
     */
    enum WorldImportResult {
        SUCCESS("Success"),
        ALREADY_IMPORTED("The world was already imported"),
        GENERATOR_NOT_FOUND("The specified generator could not be found");

        private final String description;

        WorldImportResult(final @NonNull String description) {
            this.description = Objects.requireNonNull(description);
        }

        /**
         * Get a human readable description explaining
         * the result
         *
         * @return Result description
         */
        @NonNull
        public String getDescription() {
            return this.description;
        }
    }

}
