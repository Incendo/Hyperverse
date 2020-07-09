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

package se.hyperver.hyperverse.util;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.NotNull;

/**
 * A {@link Service} used to find safe teleportation locations
 */
public interface SafeTeleportService extends Service {

    /**
     * Find a safe target location. Assumes the location chunk is loaded
     *
     * @param target Target location
     * @return Safe teleportation location
     */
    @NotNull Location findSafeLocation(@NotNull Location target);

    /**
     * Get the default service implementation
     *
     * @return Default implementation
     */
    @NotNull static SafeTeleportService defaultService() {
        return new DefaultSafeTeleportService();
    }

    /**
     * Default {@link SafeTeleportService} implementation that just scans
     * for safe locations in a vertical column
     */
    class DefaultSafeTeleportService implements SafeTeleportService {

        @Override @NotNull public Location findSafeLocation(@NotNull final Location location) {
            Block locationBlock = location.getBlock();
            do {
                if (locationBlock.getRelative(BlockFace.DOWN).getType().isSolid()) {
                    return locationBlock.getLocation();
                }
            } while (locationBlock.getY() > 0
                && (locationBlock = locationBlock.getRelative(BlockFace.DOWN)).getType() != Material.VOID_AIR);
            return location;
        }

    }

}
