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

import org.bukkit.WorldType;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

public enum WorldFeatures {
    NORMAL(WorldType.NORMAL, "normal"),
    FLATLAND(WorldType.FLAT, "flat", "flatland", "flatworld"),
    AMPLIFIED(WorldType.AMPLIFIED, "amplified"),
    BUFFET("BUFFET", "buffet");

    private static final IllegalStateException INCOMPATIBLE_VERSION = new IllegalStateException(
            "WorldType is unavailable on this version.");
    private final WorldType bukkitType;
    private final Collection<String> names;

    WorldFeatures(
            final @NonNull WorldType bukkitType,
            final @NonNull String... names
    ) {
        this.bukkitType = Objects.requireNonNull(bukkitType);
        this.names = Arrays.asList(names);
    }

    WorldFeatures(
            final @NonNull String enumName,
            final @NonNull String... names
    ) {
        WorldType worldType;
        try {
            worldType = WorldType.valueOf(enumName);
        } catch (IllegalArgumentException ex) {
            worldType = null;
        }
        this.bukkitType = worldType;
        this.names = Arrays.asList(names);
    }

    /**
     * Get a {@link WorldFeatures} instance from its name
     *
     * @param name The name
     * @return Optional that contains the {@link WorldFeatures} instance, if found
     */
    public static @NonNull Optional<WorldFeatures> fromName(final @NonNull String name) {
        Objects.requireNonNull(name);
        for (final WorldFeatures worldFeatures : values()) {
            if (worldFeatures.names.contains(name.toLowerCase(Locale.ENGLISH)) && worldFeatures.bukkitType != null) {
                return Optional.of(worldFeatures);
            }
        }
        return Optional.empty();
    }

    /**
     * Create a {@link WorldFeatures} from a Bukkit {@link WorldType}
     *
     * @param bukkitType Bukkit {@link WorldType}
     * @return A {@link WorldFeatures} instance, or {@code null} if there's no Hyperverse equivalent
     */
    public static @Nullable WorldFeatures fromBukkitType(final @NonNull WorldType bukkitType) {
        Objects.requireNonNull(bukkitType);
        for (final WorldFeatures worldFeatures : values()) {
            if (worldFeatures.getBukkitType() == bukkitType) {
                return worldFeatures;
            }
        }
        return null;
    }

    /**
     * Get the Bukkit enum world type for this world feature.
     *
     * @return Returns a never null {@link WorldType}.
     * @throws IllegalStateException Thrown if the bukkit world type is not available.
     *                               This method will always throw an {@link IllegalStateException}
     *                               if {@link #isAvailable()} returns false.
     */
    public @NonNull WorldType getBukkitType() throws IllegalStateException {
        if (this.bukkitType == null) {
            throw INCOMPATIBLE_VERSION;
        }
        return this.bukkitType;
    }

    /**
     * Whether the feature type is available on the server
     *
     * @return {@code true} if the feature is available, else {@code false}
     */
    public boolean isAvailable() {
        return this.bukkitType != null;
    }

}
