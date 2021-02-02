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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import se.hyperver.hyperverse.util.GeneratorUtil;

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
public final class WorldConfiguration implements Cloneable {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private String name;
    private WorldType type;
    private WorldFeatures worldFeatures;
    private String settings;
    private long seed;
    private boolean generateStructures;
    private String generator;
    private String generatorArg;
    // Mutable properties
    private boolean loaded = true;
    private Map<String, String> flags;

    WorldConfiguration(
            final @NonNull String name,
            final @NonNull WorldType type,
            final @NonNull WorldFeatures worldFeatures,
            final @NonNull String settings,
            final long seed,
            final boolean generateStructures,
            final @NonNull String generator,
            final @NonNull String generatorArg
    ) {
        this.name = name;
        this.type = type;
        this.settings = settings;
        this.seed = seed;
        this.generateStructures = generateStructures;
        this.generator = generator;
        this.generatorArg = generatorArg;
        this.worldFeatures = worldFeatures;
        this.flags = new HashMap<>();
    }

    /**
     * Get a new {@link WorldConfigurationBuilder} instance
     *
     * @return New builder instance
     */
    public static @NonNull WorldConfigurationBuilder builder() {
        return new WorldConfigurationBuilder();
    }

    /**
     * Create a world configuration from a {@link World} instance. The generator name
     * will be inferred from the {@link ChunkGenerator} instance
     *
     * @param world World instance
     * @return The constructed configuration instance
     */
    @SuppressWarnings("deprecation")
    public static @NonNull WorldConfiguration fromWorld(final @NonNull World world) {
        Objects.requireNonNull(world);
        final WorldConfigurationBuilder worldConfigurationBuilder = builder();
        worldConfigurationBuilder.setName(world.getName());
        if (world.getWorldType() != null) {
            worldConfigurationBuilder.setWorldFeatures(Objects.requireNonNull(WorldFeatures
                    .fromBukkitType(world.getWorldType())));
        }
        worldConfigurationBuilder.setType(WorldType.fromBukkit(world.getEnvironment()));
        worldConfigurationBuilder.setSeed(world.getSeed());
        worldConfigurationBuilder.setGenerateStructures(world.canGenerateStructures()
                ? WorldStructureSetting.GENERATE_STRUCTURES : WorldStructureSetting.NO_STRUCTURES);
        // Try to retrieve the generator
        try {
            if (Bukkit.getAllowNether() && world.getName().equalsIgnoreCase(Bukkit.getWorlds().get(0).getName() + "_nether")) {
                worldConfigurationBuilder.setGenerator("");
            } else if (Bukkit.getAllowNether() && world.getName().equalsIgnoreCase(Bukkit
                    .getWorlds()
                    .get(0)
                    .getName() + "_the_end")) {
                worldConfigurationBuilder.setGenerator("");
            } else {
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
    public static @Nullable WorldConfiguration fromFile(final @NonNull Path path) {
        if (!Files.exists(path)) {
            return null;
        }
        try (final BufferedReader bufferedReader = Files.newBufferedReader(path)) {
            return GSON.fromJson(GSON.newJsonReader(bufferedReader), WorldConfiguration.class);
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Create a copy of the world configuration
     *
     * @return Copy of this configuration
     */
    public @NonNull WorldConfiguration copy() {
        try {
            return this.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected @NonNull WorldConfiguration clone() throws CloneNotSupportedException {
        final WorldConfiguration other = (WorldConfiguration) super.clone();
        other.name = this.name;
        other.type = this.type;
        other.settings = this.settings;
        other.seed = this.seed;
        other.generateStructures = this.generateStructures;
        other.generator = this.generator;
        other.generatorArg = this.generatorArg;
        other.worldFeatures = this.worldFeatures;
        other.flags = new HashMap<>(this.flags);
        return other;
    }

    /**
     * Get the world features
     *
     * @return World features enum
     */
    public @NonNull WorldFeatures getWorldFeatures() {
        return this.worldFeatures;
    }

    /**
     * Get the world name
     *
     * @return World name
     */
    public @NonNull String getName() {
        return this.name;
    }

    /**
     * Get the world type
     *
     * @return World type
     */
    public @NonNull WorldType getType() {
        return this.type;
    }

    /**
     * Get the world setting string
     * This is primarily used by the flat world generator
     *
     * @return Setting string
     */
    public @NonNull String getSettings() {
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
     * Set the seed
     *
     * @param seed New seed
     */
    public void setSeed(final long seed) {
        this.seed = seed;
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
    public @NonNull String getGenerator() {
        return this.generator;
    }

    /**
     * Set the world generator
     *
     * @param generator New generator
     */
    public void setGenerator(final @NonNull String generator) {
        this.generator = generator;
    }

    /**
     * Get the world generator ID
     *
     * @return World generator ID
     */
    public @NonNull String getGeneratorArg() {
        return this.generatorArg;
    }

    /**
     * Get an internal map containing all
     * configured flags
     *
     * @return All configured world flags
     */
    public @NonNull Map<@NonNull String, @NonNull String> getFlags() {
        if (this.flags == null) {
            this.flags = new HashMap<>();
        }
        return this.flags;
    }

    /**
     * Update a flag value internally
     *
     * @param flag      Flag name
     * @param flagValue Flag value
     */
    public void setFlagValue(
            final @NonNull String flag,
            final @Nullable String flagValue
    ) {
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
    public void writeToFile(final @NonNull Path path) {
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
            GSON.toJson(this, WorldConfiguration.class, GSON.newJsonWriter(bufferedWriter));
            return;
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public @NonNull String toString() {
        return "WorldConfiguration{" + "name='" + this.name + '\'' + ", type=" + this.type + ", settings='"
                + this.settings + '\'' + ", seed=" + this.seed + ", generateStructures=" + this.generateStructures
                + ", generator='" + this.generator + '\'' + ", generatorArg='" + this.generatorArg + '\'' + '}';
    }

}
