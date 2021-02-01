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

package se.hyperver.hyperverse.world;

import org.bukkit.World;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

/**
 * World type, mirroring Bukkit's {@link World.Environment}
 */
public enum WorldType {
    OVER_WORLD(World.Environment.NORMAL, Arrays.asList("overworld", "over_world", "normal")),
    NETHER(World.Environment.NETHER, Arrays.asList("the_nether", "nether", "hell")),
    END(World.Environment.THE_END, Arrays.asList("end", "the_end"));

    private final World.Environment bukkitType;
    private final Collection<String> aliases;

    WorldType(
            final World.@NonNull Environment bukkitType,
            final @NonNull Collection<@NonNull String> aliases
    ) {
        this.bukkitType = bukkitType;
        this.aliases = aliases;
    }

    /**
     * Attempt to map a string to a world type
     *
     * @param string String to match
     * @return Optional containing the type, if found
     */
    public static @NonNull Optional<@NonNull WorldType> fromString(final @NonNull String string) {
        final String normalized = Objects.requireNonNull(string.toLowerCase());
        for (final WorldType worldType : values()) {
            if (worldType.getAliases().contains(normalized)) {
                return Optional.of(worldType);
            }
        }
        return Optional.empty();
    }

    /**
     * Get the world type from a bukkit environment
     *
     * @param environment Bukkit environment
     * @return Equivalent Hyperverse world type
     */
    public static @NonNull WorldType fromBukkit(final World.@NonNull Environment environment) {
        for (final WorldType worldType : values()) {
            if (worldType.getBukkitType() == environment) {
                return worldType;
            }
        }
        return WorldType.OVER_WORLD;
    }

    /**
     * Get the bukkit equivalent
     *
     * @return Bukkit equivalent
     */
    public World.@NonNull Environment getBukkitType() {
        return this.bukkitType;
    }

    /**
     * Get all aliases of the type name
     *
     * @return Name aliases
     */
    public @NonNull Collection<@NonNull String> getAliases() {
        return this.aliases;
    }

}
