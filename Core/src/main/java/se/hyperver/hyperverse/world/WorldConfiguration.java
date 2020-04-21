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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import se.hyperver.hyperverse.util.GeneratorUtil;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Configuration of a {@link HyperWorld}
 */
public class WorldConfiguration {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    // Immutable Properties
    private String name;
    private WorldType type;
    private String settings;
    private long seed;
    private boolean generateStructures;
    private String generator;
    private String generatorArg;
    // Mutable properties
    private boolean loaded = true;
    private Map<String, String> flags;

    WorldConfiguration(final String name, final WorldType type, final String settings, final long seed,
        final boolean generateStructures, final String generator, final String generatorArg) {
        this.name = name;
        this.type = type;
        this.settings = settings;
        this.seed = seed;
        this.generateStructures = generateStructures;
        this.generator = generator;
        this.generatorArg = generatorArg;
        this.flags = new HashMap<>();
    }

    /**
     * Get a new {@link WorldConfigurationBuilder} instance
     *
     * @return New builder instance
     */
    public static WorldConfigurationBuilder builder() {
        return new WorldConfigurationBuilder();
    }

    /**
     * Create a world configuration from a {@link World} instance. The generator name
     * will be inferred from the {@link ChunkGenerator} instance
     *
     * @param world World instance
     * @return The constructed configuration instance
     */
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

    /**
     * Construct a configuration instance from a given file
     *
     * @param path File path
     * @return Constructed configuration instance
     */
    @Nullable public static WorldConfiguration fromFile(@NotNull final Path path) {
        if (!Files.exists(path)) {
            return null;
        }
        try (final BufferedReader bufferedReader = Files.newBufferedReader(path)) {
            return gson.fromJson(gson.newJsonReader(bufferedReader), WorldConfiguration.class);
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get the world name
     *
     * @return World name
     */
    @NotNull public String getName() {
        return this.name;
    }

    /**
     * Get the world type
     *
     * @return World type
     */
    @NotNull public WorldType getType() {
        return this.type;
    }

    /**
     * Get the world setting string
     * This is primarily used by the flat world generator
     *
     * @return Setting string
     */
    @NotNull public String getSettings() {
        return this.settings;
    }

    /**
     * Get the world seed
     *
     * @return World seed
     */
    public long getSeed() {
        return this.seed;
    }

    /**
     * Check if the world should generate structured
     *
     * @return Structure generation status
     */
    public boolean isGenerateStructures() {
        return this.generateStructures;
    }

    /**
     * Get the world generator name
     *
     * @return World generator name
     */
    public String getGenerator() {
        return this.generator;
    }

    /**
     * Get the world generator ID
     *
     * @return World generator ID
     */
    public String getGeneratorArg() {
        return this.generatorArg;
    }

    /**
     * Get an internal map containing all
     * configured flags
     *
     * @return All configured world flags
     */
    @NotNull public Map<String, String> getFlags() {
        if (this.flags == null) {
            this.flags = new HashMap<>();
        }
        return this.flags;
    }

    /**
     * Update a flag value internally
     *
     * @param flag Flag name
     * @param flagValue Flag value
     */
    public void setFlagValue(@NotNull final String flag, @Nullable final String flagValue) {
        if (flagValue == null) {
            this.flags.remove(flag);
        } else {
            this.flags.put(flag, flagValue);
        }
    }

    /**
     * Check whether a world is supposed to be loaded
     *
     * @return World load status
     */
    public boolean isLoaded() {
        return this.loaded;
    }

    /**
     * Set whether a world is supposed to be loaded
     *
     * @param loaded World load status
     */
    public void setLoaded(final boolean loaded) {
        this.loaded = loaded;
    }

    /**
     * Write this configuration to a file
     *
     * @param path File to write to
     */
    public void writeToFile(@NotNull final Path path) {
        if (!Files.exists(path)) {
            try {
                Files.createFile(path);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
        try (final BufferedWriter bufferedWriter = Files
            .newBufferedWriter(Objects.requireNonNull(path))) {
            gson.toJson(this, WorldConfiguration.class, gson.newJsonWriter(bufferedWriter));
            return;
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return;
    }

    @Override public String toString() {
        return "WorldConfiguration{" + "name='" + name + '\'' + ", type=" + type + ", settings='"
            + settings + '\'' + ", seed=" + seed + ", generateStructures=" + generateStructures
            + ", generator='" + generator + '\'' + ", generatorArg='" + generatorArg + '\'' + '}';
    }
}
