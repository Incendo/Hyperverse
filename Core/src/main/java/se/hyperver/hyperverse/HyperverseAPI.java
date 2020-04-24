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

}
