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

package se.hyperver.hyperverse.world;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import se.hyperver.hyperverse.exception.HyperWorldValidationException;
import se.hyperver.hyperverse.flags.FlagParseException;
import se.hyperver.hyperverse.flags.WorldFlag;
import se.hyperver.hyperverse.teleportation.TeleportationManager;

import java.util.Collection;
import java.util.function.Consumer;

/**
 * World type used throughout Hyperverse
 */
public interface HyperWorld {

    /**
     * Create a Bukkit {@link World} matching the
     * configuration of this world instance
     *
     * @throws HyperWorldValidationException If the world configuration
     *         fails a validation step
     */
    void createBukkitWorld() throws HyperWorldValidationException;

    /**
     * Teleport a player to this world
     *
     * @param player Player to teleport
     */
    void teleportPlayer(@NotNull final Player player);

    /**
     * Get the world spawn location
     *
     * @return World spawn
     */
    @Nullable Location getSpawn();

    /**
     * Get the world UUID
     *
     * @return world UUID
     */
    @NotNull java.util.UUID getWorldUUID();

    /**
     * Get he world configuration
     *
     * @return World configuration
     */
    @NotNull WorldConfiguration getConfiguration();

    /**
     * Get the Bukkit world associated with this world
     *
     * @return Bukkit world instance
     */
    @Nullable World getBukkitWorld();

    /**
     * Set the Bukkit world associated with this world
     *
     * @param world Bukkit world instance
     */
    void setBukkitWorld(@NotNull World world);

    /**
     * Send world information to a command sender
     *
     * @param sender Sender that should receive the messages
     */
    void sendWorldInfo(@NotNull final CommandSender sender);

    /**
     * Save the world configuration file asynchronously
     */
    void saveConfiguration();

    /**
     * Check whether or not the world is loaded
     *
     * @return True if the world is loaded, false if not
     */
    boolean isLoaded();

    /**
     * Attempt to delete the world
     *
     * @param result Result of deletion
     */
    void deleteWorld(@NotNull final Consumer<WorldUnloadResult> result);

    /**
     * Attempt to unload the world and save it to disk.
     *
     * @return Result of unloading
     */
    @NotNull WorldUnloadResult unloadWorld();

    /**
     * Attempt to unload the world.
     *
     * @param saveWorld True if the world should be saved to disk.
     * @return Result of unloading
     */
    @NotNull WorldUnloadResult unloadWorld(final boolean saveWorld);

    /**
     * Get the value of a flag
     *
     * @param flagClass Flag class
     * @param <T> Flag value type
     * @return Flag value
     */
    @NotNull <T> T getFlag(@NotNull final Class<? extends WorldFlag<T, ?>> flagClass);

    /**
     * Remove a flag from the world
     *
     * @param flag Flag to remove
     * @param <T> Flag value type
     */
    <T> void removeFlag(@NotNull final WorldFlag<T, ?> flag);

    /**
     * Set the value of a flag in this world
     *
     * @param flag Flag to set
     * @param value Flag value
     * @param <T> Flag value type
     * @throws FlagParseException If the flag fails to parse
     */
    <T> void setFlag(@NotNull final WorldFlag<T, ?> flag, @NotNull final String value) throws
        FlagParseException;

    /**
     * Add a flag instance directly to the
     * world without any parsing
     *
     * @param flag Flag instance
     * @param <T> Flag value type
     */
    <T> void setFlagInstance(@NotNull final WorldFlag<T, ?> flag);

    /**
     * Get the teleportation manager belonging to this world
     *
     * @return The worlds' teleportation manager
     */
    @NotNull TeleportationManager getTeleportationManager();

    /**
     * Refresh the world flags
     */
    void refreshFlags();

    /**
     * Return all applicable world flags
     *
     * @return Immutable view of all flags
     */
    @NotNull Collection<WorldFlag<?, ?>> getFlags();

    /**
     * Get the world display name, if configured, otherwise
     * it just returns the world name
     *
     * @return World display name
     */
    @NotNull String getDisplayName();

    /**
     * Whether or not the spawn for this world
     * should be kept loaded
     *
     * @return {@code true} if the world spawn should be kept loaded
     */
    boolean shouldKeepSpawnLoaded();

    /**
     * Result of unloading or deleting a world
     */
    enum WorldUnloadResult {
        SUCCESS(""),
        FAILURE_HAS_PLAYERS("The world has players in it"),
        FAILURE_ONLY_WORLD("You cannot unload the main world"),
        FAILURE_OTHER("Unknown reason");

        private final String description;

        WorldUnloadResult(@NotNull final String description) {
            this.description = description;
        }

        /**
         * Description explaining the result
         *
         * @return Description of the result
         */
        @NotNull public final String getDescription() {
            return this.description;
        }
    }

}
