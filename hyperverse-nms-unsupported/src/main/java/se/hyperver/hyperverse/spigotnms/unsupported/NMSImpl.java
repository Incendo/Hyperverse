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

package se.hyperver.hyperverse.spigotnms.unsupported;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import se.hyperver.hyperverse.util.NMS;

import java.nio.file.Path;
import java.util.Objects;

@SuppressWarnings("unused")
public class NMSImpl implements NMS {

    @Override
    public @Nullable Location getOrCreateNetherPortal(
            final @NonNull Entity entity,
            final @NonNull Location origin
    ) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public @Nullable Location getDimensionSpawn(final @NonNull Location origin) {
        if (Objects.requireNonNull(origin.getWorld()).getEnvironment()
                == World.Environment.THE_END) {
            return new Location(origin.getWorld(), 100, 50, 0);
        }
        return origin.getWorld().getSpawnLocation();
    }

    @Override
    public void writePlayerData(final @NonNull Player player, final @NonNull Path file) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void readPlayerData(final @NonNull Player player, final @NonNull Path file, final @NonNull Runnable whenDone) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public @Nullable Location findBedRespawn(final @NonNull Location spawnLocation) {
        throw new UnsupportedOperationException("Not supported.");
    }

}
