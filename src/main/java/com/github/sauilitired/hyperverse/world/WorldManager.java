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

import com.github.sauilitired.hyperverse.Hyperverse;
import com.google.common.collect.Maps;
import lombok.Getter;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class WorldManager {

    private final Map<UUID, HyperWorld> worldMap = Maps.newHashMap();
    private final Map<String, UUID> uuidMap = Maps.newHashMap();

    @Getter private final Hyperverse hyperverse;

    WorldManager(@NotNull final Hyperverse hyperverse) {
        this.hyperverse = Objects.requireNonNull(hyperverse);
        // Find all files in the worlds folder and load them
        final Path worldsPath = this.hyperverse.getDataFolder().toPath()
            .resolve("worlds");
        if (Files.exists(worldsPath) && Files.isDirectory(worldsPath)) {
            try {
                Files.list(worldsPath).filter(path -> path.getFileName().endsWith("json"))
                    .forEach(path -> {
                    final WorldConfiguration worldConfiguration = WorldConfiguration.fromFile(path);
                    if (worldConfiguration == null) {
                        this.hyperverse.getLogger().warning(String.format("Failed to parse world file: %s",
                            path.getFileName().toString()));
                    } else {
                        final HyperWorld hyperWorld = new HyperWorld(UUID.randomUUID(), worldConfiguration);
                        this.registerWorld(hyperWorld);
                    }
                });
            } catch (IOException e) {
                hyperverse.getLogger().severe("Failed to load world configurations");
                e.printStackTrace();
            }
        }
    }

    /**
     * Attempt to import a world that has already been loaded by bukkit
     *
     * @param world Loaded world
     * @param vanilla Whether or not the world is a vanilla world
     * @param generator The generator name. If this is null, the generator
     *                  will be guessed from the chunk generator
     * @return The result of the import
     */
    public WorldImportResult importWorld(@NotNull final World world, final boolean vanilla,
        @Nullable final String generator) {
        if (this.worldMap.containsKey(world.getUID())) {
            return WorldImportResult.ALREADY_IMPORTED;
        }
        final WorldConfiguration worldConfiguration = WorldConfiguration.fromWorld(world);
        if (!vanilla) {
            final String worldGenerator = worldConfiguration.getGenerator();
            if (generator == null && (worldGenerator == null || worldGenerator.isEmpty())) {
                return WorldImportResult.GENERATOR_NOT_FOUND;
            } else if (generator != null) {
                if (worldGenerator == null || worldGenerator.isEmpty() ||
                    !generator.equalsIgnoreCase(worldGenerator)) {
                    return WorldImportResult.GENERATOR_NOT_FOUND;
                }
            }
        }
        final HyperWorld hyperWorld = new HyperWorld(world.getUID(), worldConfiguration);
        this.addWorld(hyperWorld);
        return WorldImportResult.SUCCESS;
    }

    public boolean addWorld(@NotNull final HyperWorld hyperWorld) {
        this.registerWorld(hyperWorld);
        // Create configuration file
        final Path path = this.hyperverse.getDataFolder().toPath()
            .resolve("worlds").resolve(String.format("%s.json", hyperWorld.getConfiguration().getName()));
        return hyperWorld.getConfiguration().writeToFile(path);
    }

    public void registerWorld(@NotNull final HyperWorld hyperWorld) {
        Objects.requireNonNull(hyperWorld);
        if (this.worldMap.containsKey(hyperWorld.getWorldUUID())) {
            throw new IllegalArgumentException(String.format("World %s already exists",
                hyperWorld.getConfiguration().getName()));
        }
        this.worldMap.put(hyperWorld.getWorldUUID(), hyperWorld);
        this.uuidMap.put(hyperWorld.getConfiguration().getName(), hyperWorld.getWorldUUID());
    }

    @NotNull public Collection<HyperWorld> getWorlds() {
        return Collections.unmodifiableCollection(this.worldMap.values());
    }

    @Nullable public HyperWorld getWorld(@NotNull final String name) {
        final UUID uuid = this.uuidMap.get(Objects.requireNonNull(name));
        if (uuid == null) {
            return null;
        }
        return this.worldMap.get(uuid);
    }

    @Nullable public HyperWorld getWorld(@NotNull final UUID uuid) {
        return this.worldMap.get(Objects.requireNonNull(uuid));
    }

    public enum WorldImportResult {
        SUCCESS,
        ALREADY_IMPORTED,
        GENERATOR_NOT_FOUND
    }

}
