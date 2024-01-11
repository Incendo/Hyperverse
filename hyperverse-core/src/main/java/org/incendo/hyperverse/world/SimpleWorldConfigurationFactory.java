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

package org.incendo.hyperverse.world;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.incendo.hyperverse.modules.WorldConfigurationFactory;
import org.incendo.hyperverse.util.GeneratorUtil;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public final class SimpleWorldConfigurationFactory implements WorldConfigurationFactory {

    static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Server server;

    @Inject
    SimpleWorldConfigurationFactory(final @NonNull Server server) {
        this.server = server;
    }

    /**
     * Create a world configuration from a {@link World} instance. The generator name
     * will be inferred from the {@link ChunkGenerator} instance
     *
     * @param world World instance
     * @return The constructed configuration instance
     */
    @Override
    @SuppressWarnings("deprecation")
    public @NonNull WorldConfiguration fromWorld(final @NonNull World world) {
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
            if (this.server.getAllowNether()
                    && world.getName().equalsIgnoreCase(this.server.getWorlds().get(0).getName() + "_nether")) {
                worldConfigurationBuilder.setGenerator("");
            } else if (this.server.getAllowNether() && world.getName().equalsIgnoreCase(this.server
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
    @Override
    public @Nullable WorldConfiguration fromFile(final @NonNull Path path) {
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

}
