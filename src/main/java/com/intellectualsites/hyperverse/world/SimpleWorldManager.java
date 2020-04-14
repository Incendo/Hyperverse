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

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.intellectualsites.hyperverse.Hyperverse;
import com.intellectualsites.hyperverse.configuration.Messages;
import com.intellectualsites.hyperverse.modules.HyperWorldFactory;
import com.intellectualsites.hyperverse.util.MessageUtil;
import org.bukkit.Bukkit;
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

@Singleton public class SimpleWorldManager implements WorldManager {

    private final Map<UUID, HyperWorld> worldMap = Maps.newHashMap();
    private final Map<String, UUID> uuidMap = Maps.newHashMap();

    private final Hyperverse hyperverse;
    private final HyperWorldFactory hyperWorldFactory;

    @Inject public SimpleWorldManager(final Hyperverse hyperverse, final HyperWorldFactory hyperWorldFactory) {
        this.hyperverse = Objects.requireNonNull(hyperverse);
        this.hyperWorldFactory = Objects.requireNonNull(hyperWorldFactory);
    }

    @Override public void loadWorlds() {
        // Find all files in the worlds folder and load them
        final Path worldsPath = this.hyperverse.getDataFolder().toPath()
            .resolve("worlds");
        if (!Files.exists(worldsPath)) {
            try {
                Files.createDirectories(worldsPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (Files.exists(worldsPath) && Files.isDirectory(worldsPath)) {
            MessageUtil.sendMessage(Bukkit.getConsoleSender(), Messages.messageWorldsLoading, "%path%",
                worldsPath.toString());
            try {
                Files.list(worldsPath)
                    .forEach(path -> {
                        final WorldConfiguration worldConfiguration = WorldConfiguration.fromFile(path);
                        if (worldConfiguration == null) {
                            this.hyperverse.getLogger().warning(String.format("Failed to parse world file: %s",
                                path.getFileName().toString()));
                        } else {
                            final HyperWorld hyperWorld = hyperWorldFactory.create(UUID.randomUUID(), worldConfiguration);
                            this.registerWorld(hyperWorld);
                        }
                    });
            } catch (IOException e) {
                hyperverse.getLogger().severe("Failed to load world configurations");
                e.printStackTrace();
            }
        }
        // Also loop over all other worlds to see if anything was loaded sneakily
        for (final World world : Bukkit.getWorlds()) {
            if (!this.worldMap.containsKey(world.getUID())) {
                this.importWorld(world, false, "");
            }
        }
        MessageUtil.sendMessage(Bukkit.getConsoleSender(), Messages.messageWorldLoaded, "%num%",
            Integer.toString(this.worldMap.size()));
    }

    @Override public WorldImportResult importWorld(@NotNull final World world,
        final boolean vanilla, @Nullable final String generator) {
        if (this.worldMap.containsKey(world.getUID())) {
            return WorldManager.WorldImportResult.ALREADY_IMPORTED;
        }
        final WorldConfiguration worldConfiguration = WorldConfiguration.fromWorld(world);
        if (!vanilla) {
            final String worldGenerator = worldConfiguration.getGenerator();
            if (generator == null && worldGenerator == null) {
                return WorldManager.WorldImportResult.GENERATOR_NOT_FOUND;
            } else if (generator != null) {
                if (worldGenerator == null || worldGenerator.isEmpty() ||
                    !generator.equalsIgnoreCase(worldGenerator)) {
                    return WorldManager.WorldImportResult.GENERATOR_NOT_FOUND;
                }
            }
        }
        final HyperWorld hyperWorld = hyperWorldFactory.create(world.getUID(), worldConfiguration);
        this.addWorld(hyperWorld);
        return WorldManager.WorldImportResult.SUCCESS;
    }

    @Override public boolean addWorld(@NotNull final HyperWorld hyperWorld) {
        this.registerWorld(hyperWorld);
        // Create configuration file
        final Path path = this.hyperverse.getDataFolder().toPath()
            .resolve("worlds").resolve(String.format("%s.json", hyperWorld.getConfiguration().getName()));
        return hyperWorld.getConfiguration().writeToFile(path);
    }

    @Override public void registerWorld(@NotNull final HyperWorld hyperWorld) {
        Objects.requireNonNull(hyperWorld);
        if (this.worldMap.containsKey(hyperWorld.getWorldUUID())) {
            throw new IllegalArgumentException(String.format("World %s already exists",
                hyperWorld.getConfiguration().getName()));
        }
        this.worldMap.put(hyperWorld.getWorldUUID(), hyperWorld);
        this.uuidMap.put(hyperWorld.getConfiguration().getName(), hyperWorld.getWorldUUID());
    }

    @Override @NotNull public Collection<HyperWorld> getWorlds() {
        return Collections.unmodifiableCollection(this.worldMap.values());
    }

    @Override @Nullable public HyperWorld getWorld(@NotNull final String name) {
        final UUID uuid = this.uuidMap.get(Objects.requireNonNull(name));
        if (uuid == null) {
            return null;
        }
        return this.worldMap.get(uuid);
    }

    @Override @Nullable public HyperWorld getWorld(@NotNull final UUID uuid) {
        return this.worldMap.get(Objects.requireNonNull(uuid));
    }

}
