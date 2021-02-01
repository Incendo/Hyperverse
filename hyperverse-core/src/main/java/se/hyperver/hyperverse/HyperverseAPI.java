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

package se.hyperver.hyperverse;

import cloud.commandframework.services.ServicePipeline;
import com.google.inject.Injector;
import org.checkerframework.checker.nullness.qual.NonNull;
import se.hyperver.hyperverse.configuration.HyperConfiguration;
import se.hyperver.hyperverse.database.HyperDatabase;
import se.hyperver.hyperverse.exception.HyperWorldCreationException;
import se.hyperver.hyperverse.features.PluginFeatureManager;
import se.hyperver.hyperverse.modules.HyperWorldFactory;
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
    @NonNull WorldManager getWorldManager();

    /**
     * Get the {@link Injector} instance
     * used throughout Hyperverse. This
     * can be used to create new instances
     * of various Hyperverse classes
     *
     * @return Injector instance.
     */
    @NonNull Injector getInjector();

    /**
     * Get the {@link HyperDatabase} implementation
     * used throughout Hyperverse
     *
     * @return Database instance
     */
    @NonNull HyperDatabase getDatabase();

    /**
     * Get the {@link HyperConfiguration} implementation
     * used throughout Hyperverse
     *
     * @return Configuration instance
     */
    @NonNull HyperConfiguration getConfiguration();

    /**
     * Get a factory class that creates
     * {@link se.hyperver.hyperverse.world.HyperWorld worlds}
     *
     * @return World factory
     */
    @NonNull HyperWorldFactory getWorldFactory();

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
    @NonNull HyperWorld createWorld(final @NonNull WorldConfiguration configuration)
            throws HyperWorldCreationException;

    /**
     * Gets the plugin feature manager. This can be used to register third party plugin
     * hooks directly into Hyperverse
     *
     * @return The plugin feature manager
     */
    @NonNull PluginFeatureManager getPluginFeatureManager();

    /**
     * Get the service pipeline implementation used by Hyperverse.
     *
     * @return Service pipeline
     */
    @NonNull ServicePipeline getServicePipeline();

}
