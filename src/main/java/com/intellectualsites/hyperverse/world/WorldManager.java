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

package com.intellectualsites.hyperverse.world;

import lombok.Getter;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

public interface WorldManager {

    /**
     * Attempt to import a world that has already been loaded by bukkit
     *
     * @param world Loaded world
     * @param vanilla Whether or not the world is a vanilla world
     * @param generator The generator name. If this is null, the generator
     *                  will be guessed from the chunk generator
     * @return The result of the import
     */
    WorldImportResult importWorld(@NotNull World world, boolean vanilla,
        @Nullable String generator);

    void loadWorlds();

    boolean addWorld(@NotNull HyperWorld hyperWorld);

    void registerWorld(@NotNull HyperWorld hyperWorld);

    @NotNull Collection<HyperWorld> getWorlds();

    @Nullable HyperWorld getWorld(@NotNull String name);

    @Nullable HyperWorld getWorld(@NotNull UUID uuid);

    enum WorldImportResult {
        SUCCESS("Success"),
        ALREADY_IMPORTED("The world was already imported"),
        GENERATOR_NOT_FOUND("The specified generator could not be found");

        WorldImportResult(@NotNull final String description) {
            this.description = Objects.requireNonNull(description);
        }

        @Getter final String description;
    }

}
