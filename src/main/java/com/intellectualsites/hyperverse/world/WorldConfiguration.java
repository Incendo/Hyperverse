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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.intellectualsites.hyperverse.util.GeneratorUtil;
import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class WorldConfiguration {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private String name;
    private WorldType type;
    private String settings;
    private long seed;
    private boolean generateStructures;
    private String generator;
    private String generatorArg;

    WorldConfiguration(final String name, final WorldType type, final String settings, final long seed,
        final boolean generateStructures, final String generator, final String generatorArg) {
        this.name = name;
        this.type = type;
        this.settings = settings;
        this.seed = seed;
        this.generateStructures = generateStructures;
        this.generator = generator;
        this.generatorArg = generatorArg;
    }

    public static WorldConfigurationBuilder builder() {
        return new WorldConfigurationBuilder();
    }

    @NotNull public static WorldConfiguration fromWorld(@NotNull final World world) {
        Objects.requireNonNull(world);
        final WorldConfigurationBuilder worldConfigurationBuilder = builder();
        worldConfigurationBuilder.setName(world.getName());
        worldConfigurationBuilder.setType(WorldType.fromBukkit(world.getEnvironment()));
        worldConfigurationBuilder.setSeed(world.getSeed());
        worldConfigurationBuilder.setGenerateStructures(world.canGenerateStructures());
        // Try to retrieve the generator
        try {
            ChunkGenerator chunkGenerator = GeneratorUtil.getGenerator(world.getName());
            if (chunkGenerator == null) {
                chunkGenerator = world.getGenerator();
            }
            if (chunkGenerator != null) {
                final JavaPlugin plugin = GeneratorUtil.matchGenerator(chunkGenerator);
                if (plugin != null) {
                    worldConfigurationBuilder.setGenerator(plugin.getName());
                }
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return worldConfigurationBuilder.createWorldConfiguration();
    }

    @Nullable public static WorldConfiguration fromFile(@NotNull final Path path) {
        if (!Files.exists(path)) {
            return null;
        }
        try (final BufferedReader bufferedReader = Files.newBufferedReader(path);
            final JsonReader jsonReader = new JsonReader(bufferedReader)) {
            return gson.fromJson(jsonReader, WorldConfiguration.class);
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getName() {
        return this.name;
    }

    public WorldType getType() {
        return this.type;
    }

    public String getSettings() {
        return this.settings;
    }

    public long getSeed() {
        return this.seed;
    }

    public boolean isGenerateStructures() {
        return this.generateStructures;
    }

    public String getGenerator() {
        return this.generator;
    }

    public String getGeneratorArg() {
        return this.generatorArg;
    }

    public boolean writeToFile(@NotNull final Path path) {
        if (!Files.exists(path)) {
            try {
                Files.createFile(path);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        try (final BufferedWriter bufferedWriter = Files
            .newBufferedWriter(Objects.requireNonNull(path));
            final JsonWriter jsonWriter = new JsonWriter(bufferedWriter)) {
            gson.toJson(this, WorldConfiguration.class, jsonWriter);
            return true;
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override public String toString() {
        return "WorldConfiguration{" + "name='" + name + '\'' + ", type=" + type + ", settings='"
            + settings + '\'' + ", seed=" + seed + ", generateStructures=" + generateStructures
            + ", generator='" + generator + '\'' + ", generatorArg='" + generatorArg + '\'' + '}';
    }
}
