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

package se.hyperver.hyperverse;

import com.google.inject.Injector;
import org.jetbrains.annotations.NotNull;
import se.hyperver.hyperverse.configuration.HyperConfiguration;
import se.hyperver.hyperverse.database.HyperDatabase;
import se.hyperver.hyperverse.exception.HyperWorldCreationException;
import se.hyperver.hyperverse.features.PluginFeatureManager;
import se.hyperver.hyperverse.modules.HyperWorldFactory;
import se.hyperver.hyperverse.service.Service;
import se.hyperver.hyperverse.world.HyperWorld;
import se.hyperver.hyperverse.world.WorldConfiguration;
import se.hyperver.hyperverse.world.WorldManager;

/**
 * Get an instance of the API using
 * {@link Hyperverse#getApi()}
 */
public interface HyperverseAPI {

    /**
     * Get the {@link WorldManager} implementation
     * used throughout Hyperverse
     *
     * @return World manager instance
     */
    @NotNull WorldManager getWorldManager();

    /**
     * Get the {@link Injector} instance
     * used throughout Hyperverse. This
     * can be used to create new instances
     * of various Hyperverse classes
     *
     * @return Injector instance.
     */
    @NotNull Injector getInjector();

    /**
     * Get the {@link HyperDatabase} implementation
     * used throughout Hyperverse
     *
     * @return Database instance
     */
    @NotNull HyperDatabase getDatabase();

    /**
     * Get the {@link HyperConfiguration} implementation
     * used throughout Hyperverse
     *
     * @return Configuration instance
     */
    @NotNull HyperConfiguration getConfiguration();

    /**
     * Get a factory class that creates
     * {@link se.hyperver.hyperverse.world.HyperWorld worlds}
     *
     * @return World factory
     */
    @NotNull HyperWorldFactory getWorldFactory();

    /**
     * Attempt to create a new world from a given configuration.
     * This will only succeed if there is no world with the
     * given name present in the system, the name is allowed
     * and the specified generator exists and is loaded.
     *
     * @param configuration The configuration from which the
     *                      world will be created
     * @return The created world
     * @throws HyperWorldCreationException If the world cannot
     *                                     be created from the given configuration
     */
    @NotNull HyperWorld createWorld(@NotNull final WorldConfiguration configuration)
        throws HyperWorldCreationException;

    /**
     * Gets the plugin feature manager. This can be used to register third party plugin
     * hooks directly into Hyperverse
     *
     * @return The plugin feature manager
     */
    @NotNull PluginFeatureManager getPluginFeatureManager();

    /**
     * Register a {@link Service} implementation
     *
     * @param clazz          Service class
     * @param implementation Service implementation
     * @param <T>            Service type
     */
    <T extends Service> void registerService(@NotNull Class<T> clazz, @NotNull T implementation);

    /**
     * Get the {@link Service} implementation for a service class
     *
     * @param clazz Service class
     * @param <T>   Service type
     * @return Service implementation
     */
    @NotNull <T extends Service> T getService(@NotNull Class<T> clazz);

}
