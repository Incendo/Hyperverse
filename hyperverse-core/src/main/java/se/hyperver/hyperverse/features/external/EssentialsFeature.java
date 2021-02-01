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

package se.hyperver.hyperverse.features.external;

import com.earth2me.essentials.utils.LocationUtil;
import org.bukkit.Location;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import se.hyperver.hyperverse.Hyperverse;
import se.hyperver.hyperverse.features.PluginFeature;
import se.hyperver.hyperverse.service.internal.SafeTeleportService;

import java.util.Collections;

/**
 * Feature hooking into Essentials
 */
public final class EssentialsFeature extends PluginFeature {

    @Override
    public void initializeFeature() {
        Hyperverse.getPlugin(Hyperverse.class).getLogger().info("Using Essentials to provide safe-teleportation lookup.");
        Hyperverse.getApi().getServicePipeline().registerServiceImplementation(SafeTeleportService.class,
                new EssentialsSafeTeleportService(), Collections.emptyList()
        );
    }

    private static class EssentialsSafeTeleportService implements SafeTeleportService {

        @Override
        public @Nullable Location handle(final @NonNull Location location) {
            try {
                return LocationUtil.getSafeDestination(location);
            } catch (final Exception ignored) {
            }
            return null;
        }

    }

}
