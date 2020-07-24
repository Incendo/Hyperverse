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

package se.hyperver.hyperverse.database;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

public final class PersistentLocation {

    private final String uuid;
    private final String world;
    private final double x;
    private final double y;
    private final double z;
    private final LocationType locationType;

    public PersistentLocation(@NotNull final String uuid, @NotNull final String world,
        final double x, final double y, final double z, @NotNull final LocationType locationType) {
        this.uuid = Objects.requireNonNull(uuid);
        this.world = Objects.requireNonNull(world);
        this.locationType = Objects.requireNonNull(locationType);
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static PersistentLocation fromLocation(@NotNull final UUID owner,
        @NotNull final Location location, @NotNull final LocationType locationType) {
        return new PersistentLocation(owner.toString(), Objects.requireNonNull(location.getWorld()).getName(),
            location.getX(), location.getY(), location.getZ(), locationType);
    }

    @NotNull public String getUuid() {
        return this.uuid;
    }

    @NotNull public String getWorld() {
        return this.world;
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public double getZ() {
        return this.z;
    }

    public LocationType getLocationType() {
        return this.locationType;
    }

    @NotNull public Location toLocation() {
        return new Location(Bukkit.getWorld(world), x, y, z);
    }

}
