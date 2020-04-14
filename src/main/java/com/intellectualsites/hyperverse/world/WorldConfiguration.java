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
import com.intellectualsites.hyperverse.util.SeedUtil;
import lombok.Builder;
import lombok.Getter;
import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

@Builder @Getter public class WorldConfiguration {

    private static final Gson gson
        = new GsonBuilder().setPrettyPrinting().create();

    private String name;
    @Builder.Default private WorldType type = WorldType.OVER_WORLD;
    @Builder.Default private String settings = "";
    @Builder.Default private long seed = SeedUtil.randomSeed();
    @Builder.Default private boolean generateStructures = true;
    @Builder.Default private String generator = "";

    @NotNull public static WorldConfiguration fromWorld(@NotNull final World world) {
        Objects.requireNonNull(world);
        final WorldConfigurationBuilder worldConfigurationBuilder = builder();
        worldConfigurationBuilder.name(world.getName());
        worldConfigurationBuilder.type(WorldType.fromBukkit(world.getEnvironment()));
        worldConfigurationBuilder.seed(world.getSeed());
        worldConfigurationBuilder.generateStructures(world.canGenerateStructures());
        // Try to retrieve the generator
        try {
            final ChunkGenerator chunkGenerator = GeneratorUtil.getGenerator(world.getName());
            if (chunkGenerator != null) {
                final JavaPlugin plugin = GeneratorUtil.matchGenerator(chunkGenerator);
                if (plugin != null) {
                    worldConfigurationBuilder.generator(plugin.getName());
                }
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return worldConfigurationBuilder.build();
    }

    public boolean writeToFile(@NotNull final Path path) {
        try (final BufferedWriter bufferedWriter = Files.newBufferedWriter(Objects.requireNonNull(path));
             final JsonWriter jsonWriter = new JsonWriter(bufferedWriter)) {
             gson.toJson(this, WorldConfiguration.class, jsonWriter);
             return true;
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return false;
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

    @Override public String toString() {
        return "WorldConfiguration{" + "name='" + name + '\'' + ", type=" + type + ", settings='"
            + settings + '\'' + ", seed=" + seed + ", generateStructures=" + generateStructures
            + ", generator='" + generator + '\'' + '}';
    }

}
