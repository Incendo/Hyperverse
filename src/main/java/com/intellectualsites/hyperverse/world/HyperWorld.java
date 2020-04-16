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

import com.intellectualsites.hyperverse.exception.HyperWorldValidationException;
import com.intellectualsites.hyperverse.flags.FlagParseException;
import com.intellectualsites.hyperverse.flags.WorldFlag;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface HyperWorld {

    void createBukkitWorld() throws HyperWorldValidationException;

    void teleportPlayer(@NotNull Player player);

    Location getSpawn();

    java.util.UUID getWorldUUID();

    WorldConfiguration getConfiguration();

    World getBukkitWorld();

    void setBukkitWorld(@NotNull World world);

    void sendWorldInfo(@NotNull final CommandSender sender);

    void saveConfiguration();

    boolean isLoaded();

    @NotNull WorldUnloadResult deleteWorld();

    @NotNull WorldUnloadResult unloadWorld();

    @NotNull <T> T getFlag(@NotNull final Class<? extends WorldFlag<T, ?>> flagClass);

    <T> void removeFlag(@NotNull final WorldFlag<T, ?> flag);

    <T> void setFlag(@NotNull final WorldFlag<T, ?> flag, @NotNull final String value) throws
        FlagParseException;

    enum WorldUnloadResult {
        SUCCESS(""),
        FAILURE_HAS_PLAYERS("The world has players in it"),
        FAILURE_ONLY_WORLD("You cannot unload the main world"),
        FAILURE_OTHER("Unknown reason");

        private final String description;

        WorldUnloadResult(@NotNull final String description) {
            this.description = description;
        }

        @NotNull public final String getDescription() {
            return this.description;
        }
    }

}
