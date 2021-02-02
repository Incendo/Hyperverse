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

package se.hyperver.hyperverse.database;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Objects;
import java.util.UUID;

public final class PersistentLocation {

    private final String uuid;
    private final String world;
    private final double x;
    private final double y;
    private final double z;
    private final LocationType locationType;

    public PersistentLocation(
            final @NonNull String uuid,
            final @NonNull String world,
            final double x,
            final double y,
            final double z,
            final @NonNull LocationType locationType
    ) {
        this.uuid = Objects.requireNonNull(uuid);
        this.world = Objects.requireNonNull(world);
        this.locationType = Objects.requireNonNull(locationType);
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static @NonNull PersistentLocation fromLocation(
            final @NonNull UUID owner,
            final @NonNull Location location,
            final @NonNull LocationType locationType
    ) {
        return new PersistentLocation(owner.toString(), Objects.requireNonNull(location.getWorld()).getName(),
                location.getX(), location.getY(), location.getZ(), locationType
        );
    }

    public @NonNull String getUuid() {
        return this.uuid;
    }

    public @NonNull String getWorld() {
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

    public @NonNull LocationType getLocationType() {
        return this.locationType;
    }

    public @NonNull Location toLocation() {
        return new Location(Bukkit.getWorld(this.world), this.x, this.y, this.z);
    }

}
