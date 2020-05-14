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
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

/**
 * Version specific NMS utility methods
 */
public interface NMS {

    @Nullable Location getOrCreateNetherPortal(@NotNull final Entity entity,
        @NotNull final Location origin);

    @Nullable Location getDimensionSpawn(@NotNull final Location origin);

    @Nullable Location findBedRespawn(@NotNull final Location spawnLocation);

    void writePlayerData(@NotNull final Player player, @NotNull final Path file);

    void readPlayerData(@NotNull final Player player, @NotNull final Path file,
        @NotNull final Runnable whenDone);

}
