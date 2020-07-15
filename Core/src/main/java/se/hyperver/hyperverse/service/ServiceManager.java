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

package se.hyperver.hyperverse.service;

import com.google.inject.Singleton;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * Represents a service manager for services which Hyperverse will use internally.
 * @see Service
 */
@Singleton
public interface ServiceManager {

    /**
     * Register a provider for a given service class.
     *
     * @param serviceClass The class to bind the service to.
     * @param provider A not-null service provider.
     */
    <T extends Service> void registerService(@NotNull final Class<T> serviceClass, @NotNull T provider);

    /**
     * Get the service provider for a given service class.
     *
     * @param serviceClass The service class.
     * @param <T>          The type of service.
     * @return Returns a never-null service provider for the passed class.
     * @throws IllegalStateException Thrown if a provider instance was not found
     *                               for the given class.
     */
    @NotNull <T extends Service> T getService(@NotNull Class<T> serviceClass)
        throws IllegalStateException;

    /**
     * Get a copy of all service providers registered to this service manager.
     *
     * @return Returns a shallow cloned {@link Collection} of {@link Service}s which have
     * been registered to this manager.
     */
    @NotNull Collection<Service> getRegisteredServices();

}
