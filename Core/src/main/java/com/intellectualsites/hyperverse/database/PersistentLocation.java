//
// Core - A minecraft world management plugin
// Copyright © 2020 Alexander Söderberg (sauilitired@gmail.com)
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

package com.intellectualsites.hyperverse.database;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

@DatabaseTable(tableName = "location")
public final class PersistentLocation {

    @DatabaseField(generatedId = true)
    private int id;
    @DatabaseField(uniqueCombo = true)
    private String uuid;
    @DatabaseField(uniqueCombo = true)
    private String world;
    @DatabaseField
    private double x;
    @DatabaseField
    private double y;
    @DatabaseField
    private double z;

    public PersistentLocation() {
    }

    public PersistentLocation(@NotNull final String uuid, @NotNull final String world,
        final double x, final double y, final double z) {
        this.uuid = Objects.requireNonNull(uuid);
        this.world = Objects.requireNonNull(world);
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static PersistentLocation fromLocation(@NotNull final UUID owner,
        @NotNull final Location location) {
        return new PersistentLocation(owner.toString(), Objects.requireNonNull(location.getWorld()).getName(),
            location.getX(), location.getY(), location.getZ());
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

    public void setId(final int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }

    @NotNull public Location toLocation() {
        return new Location(Bukkit.getWorld(world), x, y, z);
    }

}
