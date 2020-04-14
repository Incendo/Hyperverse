//
// Hyperverse - A minecraft world management plugin
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

package com.github.sauilitired.hyperverse.world;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

@RequiredArgsConstructor @Getter public enum WorldType {
    OVER_WORLD(World.Environment.NORMAL, Arrays.asList("overworld", "over_world", "normal")),
    NETHER(World.Environment.NETHER, Arrays.asList("the_nether", "nether")),
    END(World.Environment.THE_END, Arrays.asList("end", "the_end", "hell"));

    private final World.Environment bukkitType;
    private final Collection<String> aliases;

    @NotNull public static Optional<WorldType> fromString(@NotNull final String string) {
        final String normalized = Objects.requireNonNull(string.toLowerCase());
        for (final WorldType worldType : values()) {
            if (worldType.getAliases().contains(normalized)) {
                return Optional.of(worldType);
            }
        }
        return Optional.empty();
    }

    @NotNull public static WorldType fromBukkit(@NotNull final World.Environment environment) {
        for (final WorldType worldType : values()) {
            if (worldType.getBukkitType() == environment) {
                return worldType;
            }
        }
        return WorldType.OVER_WORLD;
    }

}
