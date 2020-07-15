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
package se.hyperver.hyperverse.service.internal;

import com.google.inject.Singleton;
import org.jetbrains.annotations.NotNull;
import se.hyperver.hyperverse.service.Service;
import se.hyperver.hyperverse.service.ServiceManager;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

@Singleton
public class HyperServiceManager implements ServiceManager {

    private final Map<Class<? extends Service>, Service> serviceImplMap = new HashMap<>();

    public HyperServiceManager() {

    }

    @Override public <T extends Service> void registerService(@NotNull final Class<T> serviceClass, @NotNull final T service) {
        serviceImplMap.put(serviceClass, service);
    }

    @Override @NotNull
    public <T extends Service> T getService(@NotNull final Class<T> serviceClass) throws IllegalStateException {
        final Service service = serviceImplMap.get(serviceClass);
        if (service == null) {
            throw new IllegalStateException(
                "Provider was not found for service class: " + serviceClass.getCanonicalName());
        }
        return serviceClass.cast(service);
    }

    /**
     * Get all the services currently registered to this manager.
     *
     * @return Returns a {@link Collection} of services this manager has registered.
     */
    @Override @NotNull public Collection<Service> getRegisteredServices() {
        return new HashSet<>(serviceImplMap.values());
    }

    @SuppressWarnings("unchecked")
    private static <T extends Service> T castUnsafe(@NotNull final Service service) {
        return (T) service;
    }

}
