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

package se.hyperver.hyperverse.world;

import org.bukkit.WorldType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

public enum WorldFeatures {
    NORMAL(WorldType.NORMAL),
    FLATLAND(WorldType.FLAT),
    AMPLIFIED(WorldType.AMPLIFIED),
    BUFFET(WorldType.BUFFET);
    
    private WorldType bukkitType;
    
    WorldFeatures(@NotNull final WorldType bukkitType) {
        this.bukkitType = Objects.requireNonNull(bukkitType);
    }

    @NotNull public WorldType getBukkitType() {
        return this.bukkitType;
    }

    @NotNull public static Optional<WorldFeatures> fromName(@NotNull final String name) {
        Objects.requireNonNull(name);
        for (final WorldFeatures worldFeatures : values()) {
            if (worldFeatures.name().equalsIgnoreCase(name)) {
                return Optional.of(worldFeatures);
            }
        }
        return Optional.empty();
    }

    @Nullable public static WorldFeatures fromBukkitType(@NotNull final WorldType bukkitType) {
        Objects.requireNonNull(bukkitType);
        for (final WorldFeatures worldFeatures : values()) {
            if (worldFeatures.getBukkitType() == bukkitType) {
                return worldFeatures;
            }
        }
        return null;
    }

}
